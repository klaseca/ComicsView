package com.klaseca.comicsview.fragments

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.klaseca.comicsview.OnBackPress

import com.klaseca.comicsview.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ReadImageScrollFragment : Fragment(), OnBackPress {
    lateinit var  navController: NavController
    lateinit var rotateBtn: ImageView
    lateinit var  viewPager: ViewPager
    lateinit var  customViewPager: CustomViewPager
    lateinit var settingPanel: LinearLayout
    lateinit var  url: String
    var  lastPage: Int = 0
    val pageTag = "#page="
    var pageNum = 1
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_read_image_scroll, container, false)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE

        url = arguments!!.getString("pageUrl")

        settingPanel = view.findViewById(R.id.setting_panel)

        customViewPager = view.findViewById(R.id.view_pager_img_read)

        GlobalScope.launch {
            loadImages()
        }

        rotateScreen(view)

        return view
    }

    fun loadImages() {
        val images = mutableListOf<String>()

        //val doc = Jsoup.connect(url).userAgent(userAgent).get()
        //lastPage = doc.select(".pages-count").text().toInt()
        lastPage = 3
        //val content = doc.select("#mangaBox").toString()//.attr("src")

        images.add("http://t6.mangas.rocks/auto/25/44/15/01.jpg")
        images.add("http://t6.mangas.rocks/auto/25/44/15/02.jpg")
        images.add("http://t6.mangas.rocks/auto/25/44/15/03.jpg")
        images.add("http://t6.mangas.rocks/auto/25/44/15/04.jpg")
        //Log.d("dfd", "$content")

//        while (pageNum <= lastPage) {
//            val doc = Jsoup.connect(url + pageTag + pageNum).userAgent(userAgent).get()
//            val content = doc.select("#mangaPicture").first().attr("src")
//
//            images.add(content)
//
//            pageNum++
//        }

        GlobalScope.launch(Dispatchers.Main) {
            customViewPager.adapter = ReadImageScrollAdapter(requireContext(), images)
            customViewPager.initSettingPanel(settingPanel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.VISIBLE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        when(requireActivity().resources.configuration.orientation) {
            2-> {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    private fun rotateScreen(v: View) {
        rotateBtn = v.findViewById(R.id.rotate)

        rotateBtn.setOnClickListener {
            Log.d("cxv", "${requireActivity().resources.configuration.orientation}")
            when(requireActivity().resources.configuration.orientation) {
                1-> {
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                2-> {
                    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)
        navController.popBackStack()
        return true
    }

}

class ReadImageScrollAdapter(var context: Context, var images: List<String>) : PagerAdapter() {

    lateinit var inflater: LayoutInflater

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as ScrollView
    }

    override fun getCount(): Int {
        return  images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.img_item, container, false)
        val image = view.findViewById(R.id.img_item) as ImageView
        val imgInfo = images[position]
        Picasso.get().load(imgInfo)
            .error(R.drawable.folder)
            .into(image, object : Callback {
                override fun onSuccess() {
                }

                override fun onError(e: Exception) {
                    Log.d("xczxz", "$e")
                }
            })

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ScrollView)
    }
}
