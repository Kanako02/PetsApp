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
import android.widget.RadioButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_addcats.*
import kotlinx.android.synthetic.main.activity_petdetail.*
import kotlinx.android.synthetic.main.activity_petdetail.progressBar
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class Petdetail : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var mDayRef: DatabaseReference
    private  lateinit var mReportRef: DatabaseReference

    private var mPictureUri: Uri? = null

    private lateinit var mPet: Pet
    private var mReport: Report? = null

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0

    private var mcheckflag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petdetail)
        supportActionBar?.title = "今日のお世話記録"

        // 渡ってきたオブジェクトを保持する
        val extras = intent.extras
        mPet = extras.get("petUid") as Pet

        mReport = extras.get("reportUid") as Report?

        println("mペット$mPet")

        var countNumber = 0   //移動
        radio.check(good.id)

        if (mReport != null) {
            today_button.text = mReport!!.day
            val bmp =
                BitmapFactory.decodeByteArray(mReport!!.imageBytes, 0, mReport!!.imageBytes.size)
            dayimageView.setImageBitmap(bmp)
            asaText.setText(mReport!!.asa)
            hiruText.setText(mReport!!.hiru)
            yoruText.setText(mReport!!.yoru)
            weightnum.setText(mReport!!.weight)
            detailMemo.setText(mReport!!.detailmemo)

            if (mReport!!.toilet == "") {
                countNumber = 0
            } else {
                toiletnum.setText(mReport!!.toilet)
                countNumber = mReport!!.toilet.toInt()
            }

            if (mReport!!.condition == "とても元気") {
                radio.check(good.id)
            } else if (mReport!!.condition == "ふつう") {
                radio.check(ave.id)
            } else {
                radio.check(bad.id)
            }

            if (mReport != null) {
                today_button.isEnabled = false
            }
        }

        today_button.setOnClickListener(mOnDateClickListener)
        dayimageView.setOnClickListener(this)
        detailButton.setOnClickListener(this)

        var toiletnum = findViewById<EditText>(R.id.toiletnum)
        val upButton = findViewById<Button>(R.id.upButton)
        val downButton = findViewById<Button>(R.id.downButton)

        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)

        upButton.setOnClickListener {
            countNumber++
            toiletnum.setText(Integer.toString(countNumber))
        }
        downButton.setOnClickListener {
            if (countNumber > 0) {
                countNumber--
                toiletnum.setText(Integer.toString(countNumber))
            }
        }

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mDayRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(mPet.petUid).child(ReportPATH)
//        mDayRef.addChildEventListener(mEventListener)
    }


    override fun onClick(v: View) {

        val extras = intent.extras
        mReport = extras.get("reportUid") as Report?

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

            if (mcheckflag) {
                Snackbar.make(v, "指定された日付はすでに登録されています", Snackbar.LENGTH_LONG).show()
                return
            }

            if (today_button.text.toString() == "----/--/--"){
                Snackbar.make(v, "日付を登録してください", Snackbar.LENGTH_LONG).show()
                return
            }

            val dataBaseReference = FirebaseDatabase.getInstance().reference

            val id = radio.checkedRadioButtonId //radiobutton
            val checkedRadioButton = findViewById<RadioButton>(id)

            val data = HashMap<String, String>()

            val day = today_button.text.toString()
            val condition = checkedRadioButton.text.toString()
            val asa = asaText.text.toString()
            val hiru = hiruText.text.toString()
            val yoru = yoruText.text.toString()
            val toilet = toiletnum.text.toString()
            val weight = weightnum.text.toString()
            val detailmemo = detailMemo.text.toString()

            data["day"] = day
            data["condition"] = condition
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
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["dayimage"] = bitmapString
            }

            if (mReport == null) { //新規作成

                val reportRef =
                    dataBaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child(mPet.petUid).child(
                        ReportPATH)
                reportRef.push().setValue(data, this)

            } else {                    //編集するとき　mPetがnull
                val reportRef =
                    dataBaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child(mPet.petUid).child(
                        ReportPATH).child(mReport!!.reportUid)

                reportRef.updateChildren(data as Map<String, Any>, this)
            }
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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

            val resizedImage =
                Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

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
                mcheckflag = false

                if (mReport == null) {
                    mReportRef =
                        mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(mPet.petUid).child(ReportPATH)

                    val query = mReportRef.orderByChild("day")
                        .equalTo(dateString)   //report以下のday;選択した日付のデータを取得

                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) { //検索結果がsnapshotに返ってくる
                            println("テスト$snapshot")

                            if (snapshot.exists()) {
                                Snackbar.make(
                                    today_button,
                                    "指定された日付はすでに登録されています",
                                    Snackbar.LENGTH_LONG).show()
                                mcheckflag = true
                            }
                        }

                        override fun onCancelled(firebaseError: DatabaseError) {}
                    })
                }

            }, mYear, mMonth, mDay)

        datePickerDialog.show()
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "登録に失敗しました", Snackbar.LENGTH_LONG)
                .show()
        }
    }
}

//日付設定、ご飯、トイレ、体重、メモ、写真を保存