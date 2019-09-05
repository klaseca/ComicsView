package com.klaseca.comicsview.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.klaseca.comicsview.InfoImage
import com.klaseca.comicsview.OnBackPress
import com.klaseca.comicsview.R
import com.klaseca.comicsview.adapters.ImageScrollAdapter
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ImageScrollFragment : Fragment(), OnBackPress {
    lateinit var  navController: NavController
    lateinit var rotateBtn: ImageView
    lateinit var imgItem: ImageView
    lateinit var zipPath: String
    lateinit var  viewPager: ViewPager
    lateinit var  customViewPager: CustomViewPager
    lateinit var settingPanel: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        zipPath = arguments!!.getString("zipPath") as String
        //requireActivity().setContentView(R.layout.fragment_image_scroll)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_image_scroll, container, false)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE

        settingPanel = view.findViewById(R.id.setting_panel)

        customViewPager = view.findViewById(R.id.view_pager_img)
        viewPager = view.findViewById(R.id.view_pager_img)

        loadImages(zipPath)

        rotateScreen(view)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.VISIBLE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        when(requireActivity().resources.configuration.orientation) {
            2-> {
                requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    private fun rotateScreen(v: View) {
        rotateBtn = v.findViewById(R.id.rotate)

        rotateBtn.setOnClickListener {
            Log.d("cxv", "${requireActivity().resources.configuration.orientation}")
            when(requireActivity().resources.configuration.orientation) {
                1-> {
                    requireActivity().requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
                }
                2-> {
                    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun loadImages(path: String) {

        val images = mutableListOf<InfoImage>()

        val fis = FileInputStream(path)
        val zis = ZipInputStream(fis)
        var ze: ZipEntry? = null

        while({ze = zis.nextEntry; ze}() != null) {
            when(ze!!.name.substringAfterLast('.')) {
                "jpg", "png"-> {
                    images.add(InfoImage(ze!!.name, BitmapFactory.decodeStream(zis)))
                }
            }
        }

        images.sortBy { it.name }

        val size = images.size - 1
        var lastPage = true
        var isScrolled = false
        var pageNow = 0
        var newPath = ""

        customViewPager.adapter = ImageScrollAdapter(requireContext(), images)
        customViewPager.initSettingPanel(settingPanel)

//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
//            override fun onPageScrollStateChanged(state: Int) {
//
//            }
//
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
////                if (lastPage && !isScrolled) {
////                    isScrolled = true
////                } else if (lastPage && isScrolled && positionOffset == 0.0f) {
////                    val parentPath = File(path.substringBeforeLast("/"))
////                    val nowZipName = path.substringAfterLast("/")
////
////                    run breaker@ {
////                        parentPath.listFiles()
////                            .sortedWith(compareBy({ it.isDirectory }, { it.name }))
////                            .forEach loop@{
////                                if (it.name > nowZipName) {
////                                    newPath = "$parentPath/${it.name}"
////                                    Log.d("df", "$newPath")
////                                    return@breaker
////                                } else {
////                                    Toast.makeText(requireActivity(), "Last file", Toast.LENGTH_LONG).show()
////                                }
////                            }
////                    }
////
////                    if (newPath != "") loadImages(newPath)
////                }
//            }
//
//            override fun onPageSelected(position: Int) {
//                pageNow = position
//
//                if (position == 1) {
//                    loadImages("/storage/emulated/0/Comics View/#Blessed (#Благословенная)/blessed_v1_ch3.zip")
//                }
//
////                if (size == position) {
////                    lastPage = true
////                } else if (lastPage) {
////                    lastPage = false
////                    isScrolled = false
////                }
//
//                Log.d("pn", "$pageNow")
//                Log.d("lp", "$lastPage")
//                Log.d("is", "$isScrolled")
//            }
//        })

        //bottomBar(v, images)
    }

//    fun bottomBar(v: View, img: List<InfoImage>) {
//        val bar = v.findViewById<SeekBar>(R.id.seek_bar)
//
//        bar.max = img.size
//
//        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
////                val imgItem = requireActivity().findViewById<ImageView>(R.id.img_item)
////
////                Log.d("zxc", "${img.size}")
////                Log.d("zxc", "${progress}")
////
////                imgItem.setImageBitmap(img[progress].bitmap)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                val imgItem = requireActivity().findViewById<ImageView>(R.id.img_item)
//
//                val num = seekBar.progress - 1
//
//                imgItem.setImageBitmap(img[num].bitmap)
//            }
//
//        })
//    }

    override fun onBackPressed(): Boolean {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)
        navController.popBackStack()
        return true
    }
}

class CustomViewPager(context: Context, attrs: AttributeSet): ViewPager(context, attrs) {
    lateinit var settingPanelView: LinearLayout

    var xCor = 0f
    var yCor = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                xCor = event.x
                yCor = event.y
                Log.d("ad", "$xCor")
            }
            MotionEvent.ACTION_UP -> {
                val xUp = xCor - event.x
                val yUp = yCor - event.y

                if (xUp == 0f && yUp == 0f) {
                    when(settingPanelView.visibility) {
                        View.VISIBLE -> {
                            settingPanelView.visibility = View.GONE
                        }
                        View.GONE -> {
                            settingPanelView.visibility = View.VISIBLE
                        }
                    }
                }

            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun initSettingPanel(sp: LinearLayout) {
        settingPanelView = sp
    }
}
