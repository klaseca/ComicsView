package com.klaseca.comicsview.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.klaseca.comicsview.MangaCatalog
import com.klaseca.comicsview.R
import com.squareup.picasso.Picasso

class MangaCatalogAdapter(var mCtx: Context, var resource:Int, private val name: List<MangaCatalog>)
    : ArrayAdapter<MangaCatalog>(mCtx, resource, name) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)
        val rowView = layoutInflater.inflate(resource, null, true)

        val mcName = rowView.findViewById(R.id.mcName) as TextView
        val mcAuthor = rowView.findViewById(R.id.mcAuthor) as TextView
        val mcStatus = rowView.findViewById(R.id.mcStatus) as TextView

        val mcImage = rowView.findViewById(R.id.mcImage) as ImageView


        val item : MangaCatalog = name[position]

        mcName.text = item.name
        mcAuthor.text = item.author
        mcStatus.text = item.status
        Picasso.get().load(item.imgUrl)
            .into(mcImage)

        return rowView
    }
}