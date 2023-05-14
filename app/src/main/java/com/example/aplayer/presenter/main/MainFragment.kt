package com.example.aplayer.presenter.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplayer.R
import com.example.aplayer.Repositories
import com.example.aplayer.databinding.FragmentMainBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.presenter.main.adapter.MainAdapter
import com.example.aplayer.utils.viewModelCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainFragment : Fragment() {
    private var mBinding: FragmentMainBinding? = null
    private val binding get() = mBinding!!
    private val adapter by lazy { MainAdapter() }
    private val viewModel by viewModelCreator { MainViewModel(Repositories.providerRepository) }
    private var indexState: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.mainRecyclerView.adapter = adapter
        getMusic()
        itemClickListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun getMusic() {
        val dispose = viewModel.getMusic()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                adapter.setList(list)
            }, { th ->
                Log.e("!@#", th.message.toString())
            }
            )
    }

    private fun itemClickListener() {
        adapter.setOnClickListener(object : MainAdapter.OnClickListener {
            override fun onClick(position: Int, list: ArrayList<Music>) {
                val bundle = Bundle()
                bundle.putParcelableArrayList("music", list)
                bundle.putInt("position", position)
                findNavController().navigate(R.id.action_mainFragment_to_playerFragment, bundle)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.layout_style -> {
                layoutStyleDialog()
                true
            }
            else -> false
        }

    }

    private fun layoutStyleDialog() {
        val choiceList = arrayOf(
            "Список",
            "Плитка"
        )
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Выберете стиль")
            .setPositiveButton("Ок") { _, _ ->
                when (indexState) {
                    0 -> {
                        binding.mainRecyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.mainRecyclerView.adapter = adapter
                    }
                    1 -> {
                        binding.mainRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 3)
                        binding.mainRecyclerView.adapter = adapter
                    }
                }
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .setSingleChoiceItems(choiceList, indexState) { _, index ->
                when (index) {
                    0 -> {
                        indexState = 0
                    }
                    1 -> {
                        indexState = 1
                    }
                }

            }
        dialog.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}