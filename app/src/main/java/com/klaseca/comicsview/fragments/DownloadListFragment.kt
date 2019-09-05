package com.klaseca.comicsview.fragments

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import okhttp3.*
import okhttp3.internal.Util
import okhttp3.internal.http.RealResponseBody
import okhttp3.internal.http2.Header
import okio.Okio
import okio.Utf8
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException

class DownloadListFragment : Fragment(), OnBackPress {
    lateinit var listView : ListView
    lateinit var  navController: NavController
    val rootUrl = "https://manga-chan.me"
    val root = Environment.getExternalStorageDirectory()
    val rootDir = "Comics View"
    var dir = ""
    var pathNow = File(root, dir)
    lateinit var  url: String
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_download_list, container, false)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)

        settingToolBar(view)

        url = arguments!!.getString("pageUrl")

        listView = view.findViewById(R.id.downFiles)

        GlobalScope.launch {
            loadFiles()
        }

        return view
    }

    private fun loadFiles() {
        val list = mutableListOf<InfoDownloadFile>()
        var i = 1

        val doc = Jsoup.connect(url).userAgent(userAgent).get()
        val content = doc.select("tr > td > a")

        if (content.size >  1) {
            do {
                val fileName = content[i].text()
                val fileUrl = rootUrl + content[i].attr("href")
                list.add(InfoDownloadFile(fileName, fileUrl))

                i++
            } while (i < content.size)

            if (list.size >  0) {
                list.reverse()
                listView.setOnItemClickListener { parent, view, position, id ->
                    val item: InfoDownloadFile = list[position]

                    httpRequest(item.url, item.name)
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                progressBar.visibility = View.GONE

                listView.adapter =
                    DownloadFilesAdapter(requireContext(), R.layout.download_files_row, list)
            }
        }
    }

    private fun httpRequest(urlDownload: String, nameFile: String) {
        val request = Request.Builder().url(urlDownload)
            .header("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Host", "manga-chan.me")
            .addHeader("Referer", url)
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val location = response.priorResponse()!!.header("Location")!!
                val nameDir = arguments!!.getString("comicsName")!!

                dir = "$rootDir/$nameDir"
                pathNow = File(root, dir)
                if (!pathNow.exists()) {
                    pathNow.mkdirs()

                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Create folder '$nameFile'", Toast.LENGTH_LONG).show()
                    }
                }

                val file = File(root, "$rootDir/$nameDir/$nameFile")

                if (file.exists()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        alert(location, file, nameFile)
                    }
                } else {
                    downloadFile(location, file, nameFile)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d("err", "$e")
            }
        })
    }

    fun alert(urlFile: String, file: File, nameFile: String) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("The file has already been downloaded")

        builder.setMessage("Download again?")

        builder.setPositiveButton("Yes"){dialog, which ->
            file.delete()

            downloadFile(urlFile, file, nameFile)
        }

        builder.setNegativeButton("No"){dialog, which ->

        }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    fun downloadFile(urlFile: String, file: File, nameFile: String) {
        val request = Request.Builder().url(urlFile)
            .header("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
            .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireActivity(), "File '$nameFile' is loading", Toast.LENGTH_LONG).show()
                }

                val sink = Okio.buffer(Okio.sink(file))
                val source = response.body()!!.source()

                sink.writeAll(source)
                sink.close()
                client.connectionPool().evictAll() //chewerdsf

                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireActivity(), "File '$nameFile' was downloaded", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d("err", "$e")
            }
        })
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
