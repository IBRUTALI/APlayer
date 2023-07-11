package com.example.aplayer.presenter.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplayer.R
import com.example.aplayer.Repositories
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.databinding.FragmentMainBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.presenter.main.adapter.MainAdapter
import com.example.aplayer.utils.viewModelCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

const val Broadcast_PLAYING_POSITION = "play_pause"

class MainFragment : Fragment() {
    private var mBinding: FragmentMainBinding? = null
    private val binding get() = mBinding!!
    private val adapter by lazy { MainAdapter() }
    private var indexState: Int = -1
    private val storageUtil by lazy { StorageUtil(requireContext()) }
    private val viewModel by viewModelCreator { MainViewModel(Repositories.providerRepository) }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val position = storageUtil.loadAudioIndex()
            viewModel.setPlayingPosition(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.mainRecyclerView.adapter = adapter
        getMusic()
        itemClickListener()
        playingPositionObserver()
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
                storageUtil.storeAudioIndex(position)
                findNavController().navigate(R.id.action_mainFragment_to_playerFragment)
            }
        })
    }

    private fun playingPositionObserver() {
        viewModel.playingPosition.observe(viewLifecycleOwner) {
            adapter.updateList(
                storageUtil.loadAudioIndex(),
                storageUtil.isPlayingPosition()
            )
        }
    }

    @Deprecated("Deprecated in Java")
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
            getString(R.string.list),
            getString(R.string.tile)
        )
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_list_style))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
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
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
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

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(Broadcast_PLAYING_POSITION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}