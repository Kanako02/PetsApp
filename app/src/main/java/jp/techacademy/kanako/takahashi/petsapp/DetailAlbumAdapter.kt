package jp.techacademy.kanako.takahashi.petsapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class DetailAlbumAdapter (context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mDetailAlbumArrayList = ArrayList<Report>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mDetailAlbumArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mDetailAlbumArrayList[position]
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
        albumText.text = mDetailAlbumArrayList[position].day

//        val conIc = convertView.findViewById<View>(R.id.conIc) as ImageView
//        if (mAlbumArrayList[position].condition == "とても元気"){
//            conIc.setImageResource(R.drawable.ic_good_24dp)
//        }else if (mAlbumArrayList[position].condition == "ふつう"){
//            conIc.setImageResource(R.drawable.ic_ave_24dp)
//        }else if (mAlbumArrayList[position].condition == "元気ない"){
//            conIc.setImageResource(R.drawable.ic_bad_24dp)
//        }


        val bytes = mDetailAlbumArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val albumimage = convertView.findViewById<View>(R.id.albumimage) as ImageView
            albumimage.setImageBitmap(image)
        }
        return convertView
    }

    fun setDetailAlbumArrayList(detailalbumArrayList: ArrayList<Report>) {
        mDetailAlbumArrayList = detailalbumArrayList
    }
}