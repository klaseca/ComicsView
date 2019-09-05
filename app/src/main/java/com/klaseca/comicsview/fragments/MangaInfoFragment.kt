package com.klaseca.comicsview.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.klaseca.comicsview.OnBackPress
import com.klaseca.comicsview.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_manga_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class MangaInfoFragment : Fragment(), OnBackPress {
    lateinit var  navController: NavController
    val rootUrl = "https://manga-chan.me"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_manga_info, container, false)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)

        settingToolBar(view)

        GlobalScope.launch {
            loadMangaInfo()
        }

        return view
    }

    private fun settingToolBar(v: View) {
        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.back_arr)
        toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
    }

    private fun loadMangaInfo() {
        val url = arguments!!.getString("pageUrl")

        val doc = Jsoup.connect(url).userAgent(userAgent).get()
        val content = doc.select("#dle-content")

        val imgUrl = content.select("#cover").attr("src")
        val name = content.select(".title_top_a").text()
        val type = content.select(".translation > a")[0].text()
        val author = content.select(".translation")[1].text()
        val status = content.select("tr > .item2 > h2")[2].text()
        var desc = content.select("#description").text()

        if (desc == "Прислать описание") {
            desc = "Описание отсутствует"
        } else {
            desc.substringBeforeLast('.')
        }

        val pageUrlContent = content.select(".extaraNavi > .extra_off > a")

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            mangaInfo.visibility = View.VISIBLE

            miName.text = name
            miType.text = type
            miAuthor.text = author
            miStatus.text = status
            miDesc.text = desc
            Picasso.get().load(imgUrl).into(miImage)

            miDownload.setOnClickListener {
                if (pageUrlContent.size > 1) {
                    val pageUrl = rootUrl + pageUrlContent[1].attr("href")

                    val bundle = Bundle()
                    bundle.putString("pageUrl", pageUrl)
                    bundle.putString("comicsName", name)

                    navController.navigate(R.id.toDownloadListFragment, bundle)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        navController.popBackStack()
        return true
    }
}
