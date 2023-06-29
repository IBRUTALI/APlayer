package com.example.aplayer.presenter.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.domain.service.PlayerService
import io.reactivex.disposables.CompositeDisposable


class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private lateinit var musicList: ArrayList<Music>
    private val playerViewModel by lazy { PlayerViewModel() }
    private val compositeDisposable = CompositeDisposable()
    private val audioDisposable = CompositeDisposable()
    private var player: PlayerService? = null
    private var isServiceBound = false


    private fun init() {
        musicList = getMusicFromBundle()
        isServiceBound = playerViewModel.isBounded.value!!
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            player = binder.getService()
            isServiceBound = true
            playerViewModel.isBounded.value = true
            //Toast.makeText(this, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
            playerViewModel.isBounded.value = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupUI(getPositionFromBundle())
        playAudio()
        seekBarChangeListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun playAudio() {
        //Check is service is active
        if (!isServiceBound!!) {
            val playerIntent = Intent(requireContext(), PlayerService::class.java)
            playerIntent.putParcelableArrayListExtra("Music list", musicList)
            playerIntent.putExtra("Current position", getPositionFromBundle())
            activity?.startService(playerIntent)
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }


    private fun setupUI(position: Int) {
        val music = musicList[position]
        with(binding) {
            Glide.with(requireContext())
                .load(music.artUri)
                .centerCrop()
                .placeholder(R.drawable.im_default)
                .into(playerAlbumArt)

            playerName.text = music.name
            playerArtist.text = music.artist
            rightDuration.text = music.duration
        }

    }

    private fun seekBarChangeListener() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    //playerViewModel.getMediaPlayer().seekTo(progress)
                    binding.playerSeekBar.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })
    }

    private fun getMusicFromBundle(): ArrayList<Music> {
        return arguments?.getParcelableArrayList("music")!!
    }

    private fun getPositionFromBundle(): Int {
        return arguments?.getInt("position") as Int
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.dispose()
        audioDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}