package jp.techacademy.kanako.takahashi.petsapp

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class Petdetail : AppCompatActivity() {companion object {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
}

    private var mPictureUri: Uri? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petdetail)
        supportActionBar?.title = "今日のお世話記録"
    }
}

//日付設定、ご飯、トイレ、体重、メモ、写真を保存