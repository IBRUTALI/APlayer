package com.example.aplayer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.aplayer.R
import com.example.aplayer.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.mainToolbar)
        setupNavigation()


    }

    private fun setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.tabsFragment
            )
        )
        binding.toolbar.mainToolbar.apply {
            setupWithNavController(navController, appBarConfiguration)
            inflateMenu(R.menu.main_menu)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                visibility = if (destination.id == R.id.splashFragment) {
                    GONE
                } else
                    VISIBLE
            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.tabsFragment) {
            finish()
        } else {
            navController.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (binding.root.findNavController().currentDestination?.id != R.id.tabsFragment)
            navController.navigateUp() || onSupportNavigateUp()
        else false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}