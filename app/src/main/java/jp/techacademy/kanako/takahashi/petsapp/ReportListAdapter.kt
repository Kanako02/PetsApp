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

class ReportListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mReportArrayList = ArrayList<Report>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mReportArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mReportArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_report, parent, false)
        }

        val dayText = convertView!!.findViewById<View>(R.id.dayText) as TextView
        dayText.text = mReportArrayList[position].day

        val foodText = convertView!!.findViewById<View>(R.id.foodText) as TextView
        foodText.text = mReportArrayList[position].food

        val toiletText = convertView.findViewById<View>(R.id.toiletText) as TextView
       toiletText.text = mReportArrayList[position].toilet

        val weightText = convertView.findViewById<View>(R.id.weightText) as TextView
         weightText.text = mReportArrayList[position].weight

        val memoText = convertView.findViewById<View>(R.id.memoText) as TextView
        memoText.text = mReportArrayList[position].memo


        val bytes = mReportArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val dayimageView = convertView.findViewById<View>(R.id.dayimageView) as ImageView
            dayimageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setReportArrayList(questionArrayList: ArrayList<Report>) {
        mReportArrayList = questionArrayList
    }
}