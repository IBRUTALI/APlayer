package com.example.aplayer.presenter.main

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplayer.R
import com.example.aplayer.Repositories
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.data.settings.SettingsStorage
import com.example.aplayer.databinding.FragmentMainBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.presenter.main.adapter.AdapterState
import com.example.aplayer.presenter.main.adapter.AdapterState.*
import com.example.aplayer.presenter.main.adapter.MainAdapter
import com.example.aplayer.presenter.main.adapter.toAdapterState
import com.example.aplayer.utils.viewModelCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

const val Broadcast_PLAYING_POSITION = "play_pause"
const val ITEM_POSITION = "item_position"

class MainFragment : Fragment() {
    private var mBinding: FragmentMainBinding? = null
    private val binding get() = mBinding!!
    private lateinit var adapter: MainAdapter
    private var indexState: Int = -1
    private val storageUtil by lazy { StorageUtil(requireContext()) }
    private val settingsUtil by lazy { SettingsStorage(requireContext()) }
    private val viewModel by viewModelCreator { MainViewModel(Repositories.providerRepository) }
    private var musicList: List<Music> = emptyList()
    private var compositeDisposable = CompositeDisposable()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val position = storageUtil.loadAudioIndex()
            viewModel.setPlayingPosition(position)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (activity?.isChangingConfigurations == false) getMusic()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        val state = settingsUtil.loadListStyle()
        indexState = state
        adapter = MainAdapter(state.toAdapterState())
        switchAdapterLayouts(state)
        playingPositionObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(layoutInflater, container, false)
        registerReceiver()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun getMusic() {
        val dispose = viewModel.getMusic()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                musicList = list
                adapter.setList(list)
            }, { th ->
                Log.e("!@#", th.message.toString())
            })
        compositeDisposable.add(dispose)
    }

    private fun itemClickListener() {
        adapter.setOnClickListener(object : MainAdapter.OnClickListener {
            override fun onClick(position: Int, list: ArrayList<Music>) {
                val bundle = Bundle()
                val currentPosition = storageUtil.loadAudioIndex()
                bundle.putInt(ITEM_POSITION, currentPosition)
                storageUtil.storeAudioIndex(position)
                findNavController().navigate(R.id.action_mainFragment_to_playerFragment, bundle)
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
                switchAdapterLayouts(indexState)
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

    private fun switchAdapterLayouts(stateInt: Int) {
        when (val state = stateInt.toAdapterState()) {
            LINEAR -> {
                setAdapterLayout(state)
            }
            GRID -> {
                setAdapterLayout(state)
            }
        }
    }

    private fun setAdapterLayout(state: AdapterState) {
        adapter = MainAdapter(state)
        binding.mainRecyclerView.layoutManager =
            if (state == LINEAR) {
                LinearLayoutManager(requireContext())
            } else GridLayoutManager(requireContext(), 3)
        binding.mainRecyclerView.adapter = adapter
        adapter.setList(musicList)
        settingsUtil.storeListStyle(state)
        itemClickListener()
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(Broadcast_PLAYING_POSITION))
    }

    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(receiver)
    }

    private fun requestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_PHONE_STATE
            ) -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_PHONE_STATE
                )
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        unregisterReceiver()
        mBinding = null
    }
}