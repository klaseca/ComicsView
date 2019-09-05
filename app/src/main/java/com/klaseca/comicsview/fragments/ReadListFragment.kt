package com.klaseca.comicsview.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.klaseca.comicsview.InfoDownloadFile
import com.klaseca.comicsview.OnBackPress

import com.klaseca.comicsview.R
import com.klaseca.comicsview.adapters.DownloadFilesAdapter
import kotlinx.android.synthetic.main.fragment_download_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ReadListFragment : Fragment(), OnBackPress {
    lateinit var listView : ListView
    lateinit var  navController: NavController
    lateinit var  url: String
    val rootUrl = "http://readmanga.me"
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_read_list, container, false)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)

        settingToolBar(view)

        url = arguments!!.getString("pageUrl")

        listView = view.findViewById(R.id.readFiles)

        GlobalScope.launch {
            loadFiles()
        }

        return view
    }

    fun loadFiles() {
        val list = mutableListOf<InfoDownloadFile>()

        val doc = Jsoup.connect(url).userAgent(userAgent).get()
        val content = doc.select("#chapterSelectorSelect > option")

        for (element in content) {
            val fileName = element.text()
            val fileUrl = rootUrl + element.attr("value")

            list.add(InfoDownloadFile(fileName, fileUrl))
        }

        if (list.size >  0) {
            list.reverse()
            listView.setOnItemClickListener { parent, view, position, id ->
                val item: InfoDownloadFile = list[position]

                val bundle = Bundle()
                bundle.putString("pageUrl", item.url)

                navController.navigate(R.id.toReadImageScrollFragment, bundle)
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.GONE

            listView.adapter =
                DownloadFilesAdapter(requireContext(), R.layout.download_files_row, list)
        }
    }

    private fun settingToolBar(v: View) {
        val toolbar: Toolbar = v.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.back_arr)
        toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
    }

    override fun onBackPressed(): Boolean {
        navController.popBackStack()
        return true
    }

}
