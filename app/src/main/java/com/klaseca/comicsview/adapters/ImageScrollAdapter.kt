package com.klaseca.comicsview.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.getSystemService
import androidx.viewpager.widget.PagerAdapter
import com.klaseca.comicsview.InfoImage
import com.klaseca.comicsview.R
import kotlinx.android.synthetic.main.img_item.view.*

class ImageScrollAdapter(var context: Context, var images: List<InfoImage>) : PagerAdapter() {

    lateinit var inflater: LayoutInflater

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as ScrollView
    }

    override fun getCount(): Int {
        return  images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val image: ImageView
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.img_item, container, false)
        image = view.findViewById(R.id.img_item)
        val imgInfo = images[position]
        image.setImageBitmap(imgInfo.bitmap)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ScrollView)
    }
}