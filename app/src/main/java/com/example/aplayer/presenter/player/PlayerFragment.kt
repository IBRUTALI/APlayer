package com.example.aplayer.presenter.player

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentPlayerBinding

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentPlayerBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}