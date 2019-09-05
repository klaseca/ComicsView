package com.klaseca.comicsview.fragments

import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_read_manga_info.*
import kotlinx.android.synthetic.main.fragment_read_manga_info.progressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ReadMangaInfoFragment : Fragment(), OnBackPress {
    lateinit var  navController: NavController
    val rootUrl = "http://readmanga.me"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_read_manga_info, container, false)

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
        val content = doc.select(".leftContent")

        val imgUrl = content.select("img")[0].attr("src")
        val name = content.select("span.name").text()
        val type = "Манга"
        val author = content.select(".elem_illustrator > .person-link").text()
        val status = content.select(".subject-meta > p")[0].text()
        var desc = content.select(".manga-description > p").text().substringBeforeLast('.')

        val pageUrlContent = content.select(".subject-actions > a").attr("href")

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            mangaInfo.visibility = View.VISIBLE

            Picasso.get().load(imgUrl).into(rmImage)
            rmName.text = name
            rmType.text = type
            rmAuthor.text = author
            rmStatus.text = status
            rmDesc.text = desc

            rmRead.setOnClickListener {
                val pageUrl = rootUrl + pageUrlContent

                val bundle = Bundle()
                bundle.putString("pageUrl", pageUrl)
                //bundle.putString("comicsName", name)

                navController.navigate(R.id.toReadListFragment, bundle)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        navController.popBackStack()
        return true
    }

}
