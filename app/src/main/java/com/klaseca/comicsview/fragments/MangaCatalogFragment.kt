package com.klaseca.comicsview.fragments


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.klaseca.comicsview.MangaCatalog
import com.klaseca.comicsview.R
import com.klaseca.comicsview.adapters.MangaCatalogAdapter
import kotlinx.android.synthetic.main.fragment_manga_catalog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import com.klaseca.comicsview.OnBackPress
import kotlinx.android.synthetic.main.activity_main.*

class MangaCatalogFragment : Fragment(), OnBackPress {

    lateinit var listView : ListView
    lateinit var mHandler: Handler
    lateinit var mRunnable:Runnable
    lateinit var  navController: NavController
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    val rootUrl = "https://manga-chan.me/"
    var catalogUrl = "https://manga-chan.me/catalog?offset="
    var searchUrl = "https://manga-chan.me/?do=search&subaction=search&story="
    var listContent = 0
    var pageNow = 1
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_manga_catalog, container, false)
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

        return  view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.mc_search, menu)
        val searchItem = menu.findItem(R.id.menu_search)

        if(searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            val editext= searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            editext.hint = "Search"

            searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE
                    getView()?.findViewById<LinearLayout>(R.id.ll)?.visibility = View.GONE
                }
                else {
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.VISIBLE

                }
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d("asd", "$newText")
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    val res: String
                    val trim = query!!.trim()
                    Log.d("asd", "$query")

                    if (trim == "") {
                        Log.d("asd", "Empty req")
                    } else if (trim.contains(" ")) {
                        val arr = trim.split(" ")
                        res = arr.joinToString("+")
                        view?.findViewById<LinearLayout>(R.id.ll)?.visibility = View.GONE
                        GlobalScope.launch {
                            loadMangaCatalog(searchUrl + res)
                        }
                    } else {
                        res = trim
                        view?.findViewById<LinearLayout>(R.id.ll)?.visibility = View.GONE
                        GlobalScope.launch {
                            loadMangaCatalog(searchUrl + res)
                        }
                    }

                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view!!.windowToken, 0)

                    return true
                }
            })
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
                view?.findViewById<LinearLayout>(R.id.ll)?.visibility = View.VISIBLE

                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

   private fun bottomPanelControl(pageNum: TextView, backBtn: Button, nextBtn: Button) {
        pageNum.text = pageNow.toString()

        backBtn.setOnClickListener {
            if (pageNow > 1) {
                pageNow -= 1
                listContent -= 20

                pageNum.text = pageNow.toString()

                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
            }
        }

        nextBtn.setOnClickListener {
            if (pageNow < 1000) {
                pageNow += 1
                listContent += 20

                pageNum.text = pageNow.toString()

                GlobalScope.launch {
                    loadMangaCatalog(catalogUrl + listContent)
                }
            }
        }
    }

    fun loadMangaCatalog(url: String) {
        val list = mutableListOf<MangaCatalog>()

        val doc = Jsoup.connect(url).userAgent(userAgent).get()
        val contentRow = doc.select(".content_row")

        for (element in contentRow) {
            val name = element.select(".manga_row1 > div > h2> a").text()
            val type = element.select(".manga_row1 > div > div > a").text()
            val author = element.select(".manga_row2 > .row3_left > .item2 > .item2 > a").text()
            val status = element.select(".manga_row3 > .row3_left > .item2").first().text()
            val img = element.select(".manga_images img").attr("src")
            var pageUrl =  element.select(".manga_row1 > div> h2 > a").attr("href")

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
                navController.navigate(R.id.toMangaInfoFragment, bundle)
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

    override fun onBackPressed(): Boolean {
        return true
    }
}
