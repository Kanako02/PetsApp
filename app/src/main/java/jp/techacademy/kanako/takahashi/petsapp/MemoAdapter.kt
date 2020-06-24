package jp.techacademy.kanako.takahashi.petsapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater
    var taskList = mutableListOf<Task>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_memo, parent, false)
        }
        val text1 = convertView!!.findViewById<View>(R.id.text1) as TextView
        text1.text = taskList[position].title

        val text2 = convertView!!.findViewById<View>(R.id.text2) as TextView
        text2.text = taskList[position].contents

        val text3 = convertView!!.findViewById<View>(R.id.text3) as TextView
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        text3.text = simpleDateFormat.format(date)

        return convertView
    }
}