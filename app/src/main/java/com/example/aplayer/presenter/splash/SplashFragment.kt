package com.example.aplayer.presenter.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentSplashBinding

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private var mBinding: FragmentSplashBinding? = null
    private val binding get() = mBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentSplashBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

}