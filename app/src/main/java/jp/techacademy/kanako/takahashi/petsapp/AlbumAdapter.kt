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

class AlbumAdapter (context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mAlbumArrayList = ArrayList<Report>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mAlbumArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mAlbumArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_album, parent, false)
        }

        val albumText = convertView!!.findViewById<View>(R.id.albumday) as TextView
        albumText.text = mAlbumArrayList[position].day

        val bytes = mAlbumArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.albumimage) as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setAlbumArrayList(questionArrayList: ArrayList<Report>) {
        mAlbumArrayList = questionArrayList
    }
}