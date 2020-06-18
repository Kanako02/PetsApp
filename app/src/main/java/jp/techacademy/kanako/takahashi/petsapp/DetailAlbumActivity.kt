package jp.techacademy.kanako.takahashi.petsapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.GridView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailAlbumActivity: AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mGridView: GridView
    private lateinit var mDetailAlbumArrayList: ArrayList<Report>
    private lateinit var mAdapter: DetailAlbumAdapter

    private lateinit var mAlbumRef: DatabaseReference

    private lateinit var mPet: Pet

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

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

            val detailalbum = Report(reportUid, day, condition, asa, hiru, yoru, toilet, weight, detailmemo, bytes)

            mDetailAlbumArrayList.add(detailalbum)
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

        supportActionBar?.title = "ペットのアルバム"

        val extras = intent.extras
        mPet = extras.get("name") as Pet

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mGridView = findViewById(R.id.gridview)
        mAdapter = DetailAlbumAdapter(this)
        mDetailAlbumArrayList = ArrayList<Report>()
        mAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        mDetailAlbumArrayList.clear()
        mAdapter.setDetailAlbumArrayList(mDetailAlbumArrayList)
        mGridView.adapter = mAdapter

        mAlbumRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid).child(mPet.petUid).child(ReportPATH)
        println("ペット${mPet.petUid}")
        mAlbumRef.addChildEventListener(mEventListener)

    }

}