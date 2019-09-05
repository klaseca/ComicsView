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
import java.io.File

class EmptyDirFragment : Fragment(), OnBackPress {
    lateinit var  navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_empty_dir, container, false)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        return view
    }

    override fun onBackPressed(): Boolean {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host)
        navController.popBackStack()
        return true
    }
}
