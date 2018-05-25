package com.moovel.gpsrecorderplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.records_fragment.*

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)
    }

    override fun onBackPressed() {
        if (bottom_drawer?.isOpen() == true) {
            bottom_drawer.close()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()
}
