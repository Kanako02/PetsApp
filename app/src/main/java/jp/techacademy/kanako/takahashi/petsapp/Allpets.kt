package jp.techacademy.kanako.takahashi.petsapp

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.view.View
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_allpets.*
import kotlinx.android.synthetic.main.activity_allpets.fab
import kotlinx.android.synthetic.main.content_allpets.*

class Allpets : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mPetArrayList: ArrayList<Pet>
    private lateinit var mAdapter: PetListAdapter

    private lateinit var mPetRef: DatabaseReference

    private var mPet: Pet? = null

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

//            ReportArrayListを追加
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


            val pet = Pet(name, uid, gender, birth, old, profilemome, petUid, bytes, reportArrayList)


            mPetArrayList.add(pet)
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
        setContentView(R.layout.activity_allpets)
        setSupportActionBar(toolbar)
        // タイトルの設定
        supportActionBar?.title = "ペット一覧"

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = PetListAdapter(this)
        mPetArrayList = ArrayList<Pet>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // リストをタップしたら遷移
            val intent = Intent(applicationContext, ReportActivity::class.java)
            intent.putExtra("petUid", mPetArrayList[position])
            startActivity(intent)
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            val intent = Intent(applicationContext, Addcats::class.java)
            intent.putExtra("petUid", mPet)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val pet = parent.adapter.getItem(position) as Pet

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@Allpets)

            builder.setTitle("削除")
            builder.setMessage(pet.name + "を削除しますか")

            builder.setPositiveButton("削除") { _, _ ->

                val mPetUid =
                    mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child(pet.petUid) //変更

                mPetUid.removeValue()

                mPetArrayList.remove(pet)     //追加　
                mAdapter.notifyDataSetChanged()
            }

            builder.setNegativeButton("キャンセル", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

//        // ListViewを長押ししたときの処理
//        listView.setOnItemLongClickListener { parent, _, position, _ ->
//            showAlertDialog()
//
//            true
//        }

    }

//    private fun showAlertDialog() {
//        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogBuilder.setTitle("タイトル")
//        alertDialogBuilder.setMessage("メッセージ")
//
//        alertDialogBuilder.setNeutralButton("削除"){dialog, which ->
//        }
//
//        alertDialogBuilder.setPositiveButton("キャンセル"){_,_ ->
//        }
//
//        alertDialogBuilder.setNegativeButton("編集"){daialog, which->
//            val intent = Intent(applicationContext, Addcats::class.java);
//            intent.putExtra("petUid", mPetArrayList)
//            startActivity(intent)
//        }
//
//        // AlertDialogを作成して表示する
//        val alertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }

    override fun onResume() {
        super.onResume()

        //AdapterをListにセット
        mPetArrayList.clear()
        mAdapter.setPetArrayList(mPetArrayList)
        mListView.adapter = mAdapter


        mPetRef = mDatabaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid)
        mPetRef.addChildEventListener(mEventListener)

    }

}
