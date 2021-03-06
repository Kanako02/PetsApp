package jp.techacademy.kanako.takahashi.petsapp

import android.content.Context
import android.content.PeriodicSync
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_addcats.*
import java.text.SimpleDateFormat
import java.util.*

class PetListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mPetArrayList = ArrayList<Pet>()

    var onItemClick: (Pet) -> Unit = {}
    lateinit var onItemLongClickListener: (Pet) -> Boolean

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mPetArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mPetArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_pets, parent, false)
        }

        //todo カードビュー

        // ここを追加
        val cardView = convertView!!.findViewById(R.id.cardView) as? CardView
        cardView?.setOnClickListener {
            onItemClick(mPetArrayList[position])
        }
        cardView?.setOnLongClickListener{
            onItemLongClickListener(mPetArrayList[position])
        }



        val nameText = convertView!!.findViewById<View>(R.id.nameText) as TextView
        nameText.text = mPetArrayList[position].name

        val genderIc = convertView!!.findViewById<View>(R.id.genderIc) as ImageView
        if (mPetArrayList[position].gender == "オス"){
            genderIc.setImageResource(R.drawable.osu)
        }else if (mPetArrayList[position].gender == "メス"){
            genderIc.setImageResource(R.drawable.mesu)
        }

        val birthText = convertView!!.findViewById<View>(R.id.birthText) as TextView
        birthText.text = mPetArrayList[position].birth

        val oldText = convertView!!.findViewById<View>(R.id.oldText) as TextView
//        oldText.text = mPetArrayList[position].old


        val bytes = mPetArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
            imageView.setImageBitmap(image)
        } else {
            val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
            imageView.setImageResource(R.drawable.neko)
        }

        if (mPetArrayList[position].birth != "----/--/--") {
            val date_str = mPetArrayList[position].birth
            val df = SimpleDateFormat("yyyy/MM/dd")
            val dt = df.parse(date_str)
            val df2 = SimpleDateFormat("yyyyMMdd")
            val message = df2.format(dt)

            val today = SimpleDateFormat("yyyyMMdd")  //現在の日付
            val date = Date()

            val birth: Int = Integer.parseInt(message)
            val now: Int = Integer.parseInt(today.format(date))

            val old = (now - birth) / 10000
            oldText.text = old.toString()
            println("年齢:$old")
        }else {
            oldText.text = "？"
        }

        return convertView
    }

    fun setPetArrayList(petArrayList: ArrayList<Pet>) {
        mPetArrayList = petArrayList
    }
}
