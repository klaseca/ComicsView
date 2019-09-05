package com.klaseca.comicsview.fragments


import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.klaseca.comicsview.adapters.MyListAdapter
import com.klaseca.comicsview.OnBackPress
import com.klaseca.comicsview.R
import com.klaseca.comicsview.TypeFile
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipFile.OPEN_READ

class ListFilesFragment : Fragment(), OnBackPress {
    lateinit var listView : ListView
    lateinit var mHandler: Handler
    lateinit var mRunnable:Runnable
    lateinit var  navController: NavController

    val root = Environment.getExternalStorageDirectory()
    val rootDir = "Comics View"
    var dir = "Comics View"
    var pathNow = File(root, dir)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_list_files, container, false)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)
        listView = view.findViewById(R.id.listView)

        checkRootDir()
        refreshDir(swipeRefreshLayout)
        showDir(pathNow)

        return view
    }

    private fun checkRootDir() {
        if (!pathNow.exists()) {
            pathNow.mkdirs()
            Toast.makeText(requireActivity(), "Create folder \'Comics View\'", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun showDir(path: File) {
        val list = mutableListOf<TypeFile>()

        path.listFiles()
            .sortedWith(compareBy({ it.isFile }, { it.name }))
            .forEach {
                if (it == pathNow) {
                    return@forEach
                } else if (it.isDirectory) {
                    list.add(TypeFile("${it.name}", R.drawable.folder,"dir" ))
                } else {
                    if (it.name.substringAfterLast(".") == "zip") {
                        list.add(TypeFile("${it.name}", R.drawable.zip,"file"))
                    }
                }
            }

        if (list.size == 0) {
            dir = dir.substringBeforeLast("/")
            pathNow = File(root, dir)
            navController.navigate(R.id.toEmptyDirFragment)
        } else {
            listView.setOnItemClickListener { parent, view, position, id ->
                val file: TypeFile = list[position]

                if (file.type == "dir") {
                    dir += "/${file.name}"
                    pathNow = File(root, dir)
                    showDir(pathNow)
                } else if (file.type == "file") {
//                    val dirZip = "$dir/${file.name}"
//                    val pathZip = File(root, dirZip)
//
//                    val zipFile = ZipFile(pathZip, OPEN_READ)
//                    val entries = zipFile.entries()
//
//                    while (entries.hasMoreElements()) {
//                        val entry = entries.nextElement()
//                        val imgPath = mutableListOf<String>()
//
//                        when(entry.name.substringAfterLast('.')) {
//                            "jpg", "png"-> {
//                                imgPath.add("$pathZip/${entry.name}")
//                            }
//                        }
//                    }

                    val zipPath = "$root/$dir/${file.name}"

                    val bundle = Bundle()
                    bundle.putString("zipPath", zipPath)

                    navController.navigate(R.id.toImageScrollFragment, bundle)
                }
            }

            listView.adapter =
                MyListAdapter(requireContext(), R.layout.row, list)
        }
    }

    private fun refreshDir(swipe: SwipeRefreshLayout) {
        swipe.setOnRefreshListener {
            mRunnable = Runnable {
                showDir(pathNow)
                swipe.isRefreshing = false
            }

            mHandler = Handler()
            mHandler.postDelayed(mRunnable, 500)
        }
    }

    override fun onBackPressed(): Boolean {
        return if (rootDir != dir) {
            dir = dir.substringBeforeLast("/")
            pathNow = File(root, dir)
            showDir(pathNow)
            true
        } else {
            false
        }
    }
}
