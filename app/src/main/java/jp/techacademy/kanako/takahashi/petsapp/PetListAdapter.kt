package jp.techacademy.kanako.takahashi.petsapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class PetListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mPetArrayList = ArrayList<Pet>()

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

        val nameText = convertView!!.findViewById<View>(R.id.nameText) as TextView
        nameText.text = mPetArrayList[position].name

//        val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
//        val resNum = mQuestionArrayList[position].answers.size
//        resText.text = resNum.toString()

        val bytes = mPetArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setPetArrayList(petArrayList: ArrayList<Pet>) {
        mPetArrayList = petArrayList
    }
}