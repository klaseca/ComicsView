package com.klaseca.comicsview.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.klaseca.comicsview.MangaCatalog
import com.klaseca.comicsview.OnBackPress
import com.klaseca.comicsview.R
import com.klaseca.comicsview.adapters.MangaCatalogAdapter
import kotlinx.android.synthetic.main.fragment_manga_catalog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ReadMangaFragment : Fragment(), OnBackPress {
    lateinit var listView : ListView
    lateinit var mHandler: Handler
    lateinit var mRunnable:Runnable
    lateinit var  navController: NavController
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    val rootUrl = "http://readmanga.me"
    var catalogUrl = "http://readmanga.me/list?sortType=rate&offset="
    var searchUrl = "https://manga-chan.me/?do=search&subaction=search&story="
    var listContent = 0
    var pageNow = 1
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_read_manga, container, false)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        val pageNum: TextView = view.findViewById(R.id.pageNum)
        val backBtn: Button = view.findViewById(R.id.backBtn)
        val nextBtn: Button = view.findViewById(R.id.nextBtn)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        listView = view.findViewById(R.id.manga_catalog)

        bottomPanelControl(pageNum, backBtn, nextBtn)

        GlobalScope.launch {
            loadMangaCatalog(catalogUrl + listContent)
        }

        return view
    }

    fun loadMangaCatalog(url: String) {
        val list = mutableListOf<MangaCatalog>()

        val doc = Jsoup.connect(url).userAgent(userAgent).get()
        val contentRow = doc.select(".tile")

        for (element in contentRow) {
            val name = element.select(".desc > h3 > a").text()
            val type = ""
            val author = element.select(".desc > .tile-info > .element-link").text().replace(" ", ", ")
            val status = ""
            val img = element.select(".tile > .img > a > img").attr("data-original")
            var pageUrl =  element.select(".desc > h3 > a").attr("href")

            if (!pageUrl.contains(rootUrl)) {
                pageUrl = rootUrl + pageUrl
            }

            list.add(MangaCatalog(name, type, img, author, status, pageUrl))
        }

        if (list.size > 0) {
            listView.setOnItemClickListener { parent, view, position, id ->
                val item: MangaCatalog = list[position]

                val bundle = Bundle()
                bundle.putString("pageUrl", item.pageUrl)

                navController = Navigation.findNavController(requireActivity(), R.id.nav_host)
                navController.navigate(R.id.toReadMangaInfoFragment, bundle)
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.GONE

            refreshMangaCatalog(swipeRefreshLayout)

            listView.adapter =
                MangaCatalogAdapter(requireContext(), R.layout.manga_catalog_row, list)
        }
    }

    private fun refreshMangaCatalog(swipe: SwipeRefreshLayout) {
        swipe.setOnRefreshListener {
            mRunnable = Runnable {
                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
                swipe.isRefreshing = false
            }

            mHandler = Handler()
            mHandler.postDelayed(mRunnable, 500)
        }
    }

    private fun bottomPanelControl(pageNum: TextView, backBtn: Button, nextBtn: Button) {
        pageNum.text = pageNow.toString()

        backBtn.setOnClickListener {
            if (pageNow > 1) {
                pageNow -= 1
                listContent -= 70

                pageNum.text = pageNow.toString()

                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
            }
        }

        nextBtn.setOnClickListener {
            if (pageNow < 1000) {
                pageNow += 1
                listContent += 70

                pageNum.text = pageNow.toString()

                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}
