package jp.techacademy.kanako.takahashi.petsapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class HealthActivity : AppCompatActivity() {

    private lateinit var mPet: Pet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health)

        supportActionBar?.title = "健康記録"

        val extras = intent.extras
        mPet = extras.get("petUid") as Pet
    }
}
