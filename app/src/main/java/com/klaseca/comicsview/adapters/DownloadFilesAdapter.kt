package com.klaseca.comicsview.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.klaseca.comicsview.InfoDownloadFile
import com.klaseca.comicsview.R
import kotlinx.android.synthetic.main.download_files_row.view.*

class DownloadFilesAdapter (var mCtx: Context, var resource:Int, private val name: List<InfoDownloadFile>)
    : ArrayAdapter<InfoDownloadFile>(mCtx, resource, name) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val viewHolder:ViewHolder
        var convertView = view

        if(convertView==null){
            val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)
            convertView = layoutInflater.inflate(resource, parent, false)

            viewHolder = ViewHolder(convertView)

            convertView.tag = viewHolder
        }else{
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.nameFile.text = name[position].name
        viewHolder.iconFile.setImageResource(R.drawable.zip)

        return convertView!!
    }

    override fun getItem(position: Int): InfoDownloadFile? {
        return name[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return name.size
    }
}

private class ViewHolder(view:View){
    var nameFile: TextView = view.findViewById(R.id.downName)
    var iconFile: ImageView = view.findViewById(R.id.downIcon)
}