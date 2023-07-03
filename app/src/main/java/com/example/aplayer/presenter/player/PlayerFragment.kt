package com.example.aplayer.presenter.player

import android.app.ActivityManager
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
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.aplayer.Broadcast_PLAY_NEW_AUDIO
import com.example.aplayer.R
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.domain.service.PlayerService
import kotlin.properties.Delegates


class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private lateinit var musicList: ArrayList<Music>
    private var position = -1
    private val playerViewModel by viewModels<PlayerViewModel>()
    private var player: PlayerService? = null
    private var isServiceBound by Delegates.notNull<Boolean>()


    private fun init() {
        musicList = getMusicFromBundle()
        position = getPositionFromBundle()
        isServiceBound = isMyServiceRunning(PlayerService::class.java)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            player = binder.getService()
            isServiceBound = true
            //Toast.makeText(this, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupUI(position)
        playAudio()
        seekBarChangeListener()
    }

    private fun playAudio() {
        val storage = StorageUtil(requireContext())
        //Check is service is active
        if (!isServiceBound) { //TODO Bug: Music replayed when configuration changes
            storage.storeAudio(musicList)
            storage.storeAudioIndex(position)
            val playerIntent = Intent(requireContext(), PlayerService::class.java)
            activity?.startService(playerIntent)
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            storage.storeAudioIndex(position)
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            activity?.sendBroadcast(broadcastIntent)
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

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}