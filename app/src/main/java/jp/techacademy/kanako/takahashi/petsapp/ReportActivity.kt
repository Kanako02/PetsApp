package jp.techacademy.kanako.takahashi.petsapp

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.ListView
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_allpets.fab

import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.app_ber.*
import kotlinx.android.synthetic.main.list_pets.*

class ReportActivity : AppCompatActivity() {


    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mReportArrayList: ArrayList<Report>
    private lateinit var mAdapter: ReportListAdapter

    private lateinit var mReportRef: DatabaseReference

    private val mReportListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

//            val petUid = dataSnapshot.key ?: ""
            val day = map["day"] ?: ""
            val toilet = map["toilet"] ?: ""
            val weight = map["weight"] ?: ""
            val memo = map["memo"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val report = Report(day, toilet, weight, memo, bytes)

            println("データスナップ:$dataSnapshot")

            mReportArrayList.add(report)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

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
        setContentView(R.layout.activity_report)
        setSupportActionBar(toolbar)
//        mToolbar = findViewById(R.id.toolbar)

        supportActionBar?.title = "猫の記録"


        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = ReportListAdapter(this)
        mReportArrayList = ArrayList<Report>()
        mAdapter.notifyDataSetChanged()

        //AdapterをListにセット
        mReportArrayList.clear()
        mAdapter.setReportArrayList(mReportArrayList)
        mListView.adapter = mAdapter


        mReportRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
        mReportRef.addChildEventListener(mReportListener)




        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

}
