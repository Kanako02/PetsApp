package jp.techacademy.kanako.takahashi.petsapp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_addcats.*
import kotlinx.android.synthetic.main.app_ber.*
import kotlinx.android.synthetic.main.content_allpets.*


class ReportActivity : AppCompatActivity() {


    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mReportArrayList: ArrayList<Report>
    private lateinit var mAdapter: ReportListAdapter

    private lateinit var mPet: Pet
    private lateinit var  mReport: Report

    private lateinit var mReportRef: DatabaseReference

    private val mReportListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val reportUid = dataSnapshot.key ?: ""
            val day = map["day"] ?: ""
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

            val report = Report(reportUid, day, asa, hiru, yoru, toilet, weight, detailmemo,  bytes)

            mReportArrayList.add(report)
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
        setContentView(R.layout.activity_report)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "お世話記録"

        // 渡ってきたオブジェクトを保持する
        val extras = intent.extras
        mPet = extras.get("petUid") as Pet

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = ReportListAdapter(this)
        mReportArrayList = ArrayList<Report>()
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val intent = Intent(applicationContext, Petdetail::class.java)
            intent.putExtra("petUid", mPet)       //fabボタンの時はpetUidを渡す
            startActivity(intent)
        }

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val intent = Intent(applicationContext, Petdetail::class.java)
            intent.putExtra("reportUid", mReportArrayList[position])      //追加
            startActivity(intent)
        }


        // ListViewを長押ししたときの処理
        listView.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val report = parent.adapter.getItem(position) as Report

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@ReportActivity)

            builder.setTitle("削除")
            builder.setMessage(report.day + "の記録を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->

                val mReportUid = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid) .child(mPet.petUid).child(
                    ReportPATH).child(mReport.reportUid)                                                                                                          //変更

                println("mレポート$mReport")

                mReportUid.removeValue()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }
    }

    override fun onResume() {
        super.onResume()

        //AdapterをListにセット
        mReportArrayList.clear()
        mAdapter.setReportArrayList(mReportArrayList)
        mListView.adapter = mAdapter



        mReportRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid) .child(mPet.petUid).child(
            ReportPATH)
        mReportRef.addChildEventListener(mReportListener)

    }

}
