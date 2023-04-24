package com.example.aplayer.presenter.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.aplayer.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var mBinding: FragmentMainBinding? = null
    private val binding get() = mBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}