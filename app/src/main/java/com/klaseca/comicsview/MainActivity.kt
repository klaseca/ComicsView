package com.klaseca.comicsview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var  navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            //setSupportActionBar(toolbar)

        navController = Navigation.findNavController(this, R.id.nav_host)
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupWithNavController(bottom_navigation, navController)
        //bottom_navigation.setupWithNavController(navController)
        //setupActionBarWithNavController(this, navController)
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.nav_host)
        val currentFragment = fragment?.childFragmentManager?.primaryNavigationFragment as? OnBackPress

        currentFragment?.onBackPressed()?.takeIf { !it }?.let{
            super.onBackPressed()
        }
    }
}
