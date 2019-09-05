package com.klaseca.comicsview.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.klaseca.comicsview.R
import com.klaseca.comicsview.TypeFile

class MyListAdapter(var mCtx: Context, var resource:Int, private val name: List<TypeFile>)
    : ArrayAdapter<TypeFile>(mCtx, resource, name) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)
        val rowView = layoutInflater.inflate(resource, null, true)

        val titleText = rowView.findViewById(R.id.textItem) as TextView
        val imageView = rowView.findViewById(R.id.iconItem) as ImageView

        val item : TypeFile = name[position]

        titleText.text = item.name
        imageView.setImageResource(item.icon)

        return rowView
    }
}