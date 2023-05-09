package com.example.aplayer.presenter.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentTabsBinding

class TabsFragment : Fragment() {
    private var mBinding: FragmentTabsBinding? = null
    private val binding get() = mBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigationBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentTabsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun setupNavigationBar() {
        val navHost = childFragmentManager.findFragmentById(R.id.tabsContainer) as NavHostFragment
        val navController = navHost.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}