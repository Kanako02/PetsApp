package jp.techacademy.kanako.takahashi.petsapp

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_addcats.*
import kotlinx.android.synthetic.main.activity_petdetail.*
import kotlinx.android.synthetic.main.activity_petdetail.progressBar
import kotlinx.android.synthetic.main.list_pets.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class Petdetail : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mPictureUri: Uri? = null

    private lateinit var mPet: Pet
    private  lateinit var mReport: Report

    var countNumber = 0

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petdetail)
        supportActionBar?.title = "今日のお世話記録"

        // 渡ってきたオブジェクトを保持する
        val extras = intent.extras
        mPet = extras.get("petUid") as Pet

        mReport = extras.get("report") as Report   //追加

        today_button.setOnClickListener(mOnDateClickListener)
        dayimageView.setOnClickListener(this)
        detailButton.setOnClickListener(this)

        var toiletnum = findViewById(R.id.toiletnum) as EditText
        val upButton = findViewById(R.id.upButton) as Button
        val downButton = findViewById(R.id.downButton) as Button


        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)


        upButton.setOnClickListener {
            countNumber++
            toiletnum.setText(Integer.toString(countNumber))
        }

        downButton.setOnClickListener {
            countNumber--
            toiletnum.setText(Integer.toString(countNumber))
        }
    }

    override fun onClick(v: View) {
        if (v === dayimageView) {
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE)

                    return
                }
            } else {
                showChooser()
            }
        } else if (v === detailButton) {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val reportRef = dataBaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid).child(mPet.petUid).child(
                ReportPATH)    //useUid-petUid-ReportPATH

            val data = HashMap<String, String>()

            val day = today_button.text.toString()
            val asa = asaText.text.toString()
            val hiru = hiruText.text.toString()
            val yoru = yoruText.text.toString()
            val toilet = toiletnum.text.toString()
            val weight = weightnum.text.toString()
            val detailmemo = detailMemo.text.toString()

            data["day"] = day
            data["asa"] = asa
            data["hiru"] = hiru
            data["yoru"] = yoru
            data["toilet"] = toilet
            data["weight"] = weight
            data["detailmemo"] = detailmemo

            // 添付画像を取得する
            val drawable = dayimageView.drawable as? BitmapDrawable

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["dayimage"] = bitmapString
            }

            reportRef.push().setValue(data, this)
            progressBar.visibility = View.VISIBLE

//            val intent = Intent(applicationContext, ReportActivity::class.java)
//            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            dayimageView.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, Petdetail.CHOOSER_REQUEST_CODE)
    }


    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format(
                    "%02d",
                    mMonth + 1) + "/" + String.format("%02d", mDay)
                today_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "登録に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}

//日付設定、ご飯、トイレ、体重、メモ、写真を保存