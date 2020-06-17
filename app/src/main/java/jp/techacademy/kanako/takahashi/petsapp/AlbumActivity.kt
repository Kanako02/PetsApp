package jp.techacademy.kanako.takahashi.petsapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.GridView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AlbumActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mGridView: GridView
    private lateinit var mAlbumArrayList: ArrayList<Pet>
    private lateinit var mAdapter: AlbumAdapter

    private lateinit var mAlbumRef: DatabaseReference

    private lateinit var mPet: Pet

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val petUid = dataSnapshot.key ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val gender = map["gender"] ?: ""
            val birth = map["birth"] ?: ""
            val old = map["old"] ?: ""
            val profilemome = map["profilememo"]?:""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val reportArrayList = ArrayList<Report>()
            val reportMap = map["report"] as Map<String, String>?
            if (reportMap != null) {
                for (key in reportMap.keys) {

                    val reportUid = dataSnapshot.key ?: ""
                    val day = map["day"] ?: ""
                    val condition = map["condition"] ?:""
                    val asa = map["asa"] ?: ""
                    val hiru = map["hiru"] ?: ""
                    val yoru = map["yoru"] ?: ""
                    val toilet = map["toilet"] ?: ""
                    val weight = map["weight"] ?: ""
                    val detailmemo = map["detailmemo"] ?: ""
                    val dayimage = map["dayimage"] ?: ""
                    val bytes =
                        if (dayimage.isNotEmpty()) {
                            Base64.decode(dayimage, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    val report = Report(reportUid, day, condition, asa, hiru, yoru, toilet, weight, detailmemo,  bytes)
                    reportArrayList.add(report)
                }
            }

            val album = Pet(name, uid, gender, birth, old, profilemome, petUid, bytes, reportArrayList)

            mAlbumArrayList.add(album)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            mAdapter.notifyDataSetChanged()

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        supportActionBar?.title = "ペットのアルバム一覧"

        val extras = intent.extras
        mPet = extras.get("petUid") as Pet

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mGridView = findViewById(R.id.gridview)
        mAdapter = AlbumAdapter(this)
        mAlbumArrayList = ArrayList<Pet>()
        mAdapter.notifyDataSetChanged()

        // ListViewをタップしたときの処理
        mGridView.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val intent = Intent(applicationContext, DetailAlbumActivity::class.java);
            intent.putExtra("petUid", mPet)
            startActivity(intent);
        }
    }

       override fun onResume() {
        super.onResume()

        mAlbumArrayList.clear()
        mAdapter.setAlbumArrayList(mAlbumArrayList)
        mGridView.adapter = mAdapter

        mAlbumRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
        mAlbumRef.addChildEventListener(mEventListener)

    }

}
