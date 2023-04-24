package com.example.aplayer.presenter.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import com.example.aplayer.R
import com.example.aplayer.databinding.ActivityMainBinding
import com.example.aplayer.presenter.splash.SplashFragment

class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initSplashScreen()
    }

    private fun initToolbar() {
        binding.mainToolbar.mainToolbar.visibility = GONE
        binding.mainToolbar.mainToolbar.inflateMenu(R.menu.main_menu)
    }

    private fun initSplashScreen() {
        supportFragmentManager.beginTransaction()
            .add(SplashFragment(), "Splash")
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}