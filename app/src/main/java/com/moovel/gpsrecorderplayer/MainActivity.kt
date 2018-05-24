package com.moovel.gpsrecorderplayer

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import android.support.v7.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)

        navController.addOnNavigatedListener { _, destination ->
            drawer_layout.setDrawerLockMode(when (destination.id) {
                R.id.records_fragment -> LOCK_MODE_UNLOCKED
                else -> LOCK_MODE_LOCKED_CLOSED
            })
        }
    }

    override fun onSupportNavigateUp() = navController.navigateUp()

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun openDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }
}
