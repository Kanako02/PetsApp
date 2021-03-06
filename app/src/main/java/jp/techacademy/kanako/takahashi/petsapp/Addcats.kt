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
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_addcats.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class Addcats : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 100
        private val CHOOSER_REQUEST_CODE = 100
    }

    private var mPictureUri: Uri? = null

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0

    private var mPet: Pet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addcats)
        supportActionBar?.title = "ペットのプロフィール"

        val extras = intent.extras
        mPet = extras.get("petUid") as Pet?

//        mPet =extras.get("uid") as Pet?

        //編集の時
        if (mPet != null) {
            nameText.setText(mPet!!.name)
            val bmp =
                BitmapFactory.decodeByteArray(mPet!!.imageBytes, 0, mPet!!.imageBytes.size)
            topimageView.setImageBitmap(bmp)

            date_button.text = mPet!!.birth
            oldtext.text = mPet!!.old
            profileMemo.setText(mPet!!.profilememo)

            if (mPet!!.gender == "オス"){
                radioGroup.check(menButton.id)
            }else if (mPet!!.gender == "メス"){
                radioGroup.check(ladyButton.id)
            }
        }else if (mPet == null) {

        }

        profileButton.setOnClickListener(this)
        topimageView.setOnClickListener(this)
        date_button.setOnClickListener(mOnDateClickListener)

        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
    }

    override fun onClick(v: View) {

        val extras = intent.extras
        mPet = extras.get("petUid") as Pet?

        if (v === topimageView) {
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE
                    )

                    return
                }
            } else {
                showChooser()
            }
        } else if (v === profileButton) {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference

            val id = radioGroup.checkedRadioButtonId //radiobutton
            val checkedRadioButton = findViewById<RadioButton>(id)

            val data = HashMap<String, String>()

            val name = nameText.text.toString()
            val gender = checkedRadioButton.text.toString()
            val birth = date_button.text.toString()
            val old = oldtext.text.toString()
            val profilememo = profileMemo.text.toString()


            if (name.isEmpty()) {
                // タイトルが入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "猫の名前を入力して下さい", Snackbar.LENGTH_LONG).show()
                return
            }

            data["name"] = name
            data["gender"] = gender
            data["birth"] = birth
            data["old"] = old
            data["profilememo"] = profilememo

            // 添付画像を取得する
            val drawable = topimageView.drawable as? BitmapDrawable

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                data["image"] = bitmapString
            }
                if (mPet == null){
                val profileRef = dataBaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                profileRef.push().setValue(data, this)
            }else{
                val profileRef = dataBaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid).child(mPet!!.petUid)
                profileRef.updateChildren(data as Map<String, Any>, this)
            }
            progressBar.visibility = View.VISIBLE

            val intent = Intent(applicationContext, Allpets::class.java)
            startActivity(intent)
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
            topimageView.setImageBitmap(resizedImage)

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

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString

                val birthday = mYear.toString() + String.format("%02d", mMonth + 1) + String.format("%02d", mDay)
                println("誕生日:$birthday")

                val df = SimpleDateFormat("yyyyMMdd")  //現在の日付
                val date = Date()
                println(df.format(date))


                val birth : Int = Integer.parseInt(birthday)
                val now : Int = Integer.parseInt(df.format(date))
                println("現在:$now")

                val old = (now - birth)/10000
                        println("年齢:$old")

                oldtext.text = old.toString()

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
