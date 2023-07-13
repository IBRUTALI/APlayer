package com.example.aplayer.presenter.player

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.aplayer.Broadcast_PLAY_NEW_AUDIO
import com.example.aplayer.R
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.domain.service.*
import com.example.aplayer.presenter.main.Broadcast_PLAYING_POSITION
import kotlin.properties.Delegates

const val Broadcast_AUDIO_ACTION = "Broadcast_AUDIO_ACTION"
const val AUDIO_ACTION = "audio_action"

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private lateinit var musicList: ArrayList<Music>
    private var position = -1
    private val playerViewModel by viewModels<PlayerViewModel>(
        ownerProducer = { requireActivity() }
    )
    private var player: PlayerService? = null
    private var isServiceBound by Delegates.notNull<Boolean>()
    private val storageUtil by lazy { StorageUtil(requireContext()) }


    private fun init() {
        musicList = getMusicFromStorage()
        position = getPositionFromStorage()
        isServiceBound = isMyServiceRunning(PlayerService::class.java)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            playerViewModel.lastPosition.value = getPositionFromStorage()
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            player = binder.getService()
            isServiceBound = true
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
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(Broadcast_PLAYING_POSITION))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        playAudio()
        positionObserver()
        seekBarChangeListener()
        playPause()
        skipToNext()
        skipToPrevious()
        shuffle()
        repeat()
    }

    private fun playAudio() {
        //Check is service is active
        if (!isServiceBound) {
            val playerIntent = Intent(requireContext(), PlayerService::class.java)
            playerViewModel.lastPosition.value = position
            activity?.startService(playerIntent)
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else if (position != playerViewModel.lastPosition.value) {
            //Service is active
            //Send media with BroadcastReceiver
            playerViewModel.lastPosition.value = position
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

            if(playerName.text != music.name) playerName.text = music.name
            playerArtist.text = music.artist
            rightDuration.text = music.duration
        }

    }

    private fun playPause() {
        binding.playerPlay.setOnClickListener {
            if (storageUtil.isPlayingPosition()) {
                binding.playerPlay.setImageResource(R.drawable.baseline_play_circle_filled)
                pause()
            } else {
                binding.playerPlay.setImageResource(R.drawable.baseline_pause_circle_filled)
                resume()
            }
        }
    }

    private fun resume() {
        sendBroadcast(ACTION_PLAY)
    }

    private fun pause() {
        sendBroadcast(ACTION_PAUSE)
    }

    private fun shuffle() {
        binding.playerShuffle.setOnClickListener {
            sendBroadcast(ACTION_SHUFFLE)
        }
    }

    private fun repeat() {
        binding.playerRepeat.setOnClickListener {
            sendBroadcast(ACTION_REPEAT)
        }
    }

    private fun skipToNext() {
        binding.playerNext.setOnClickListener {
            sendBroadcast(ACTION_NEXT)
            playerViewModel.lastPosition.value = if (position == musicList.lastIndex) {
                0
            } else {
                ++position
            }
        }
    }

    private fun skipToPrevious() {
        binding.playerPrevious.setOnClickListener {
            sendBroadcast(ACTION_PREVIOUS)
            playerViewModel.lastPosition.value = if (position == 0) {
                musicList.lastIndex
            } else {
                --position
            }
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

    private fun positionObserver() {
        playerViewModel.lastPosition.observe(viewLifecycleOwner) {
            setupUI(it)
        }
    }

    private fun sendBroadcast(action: String) {
        val broadcastIntent = Intent(Broadcast_AUDIO_ACTION)
        broadcastIntent.putExtra(AUDIO_ACTION, action)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(broadcastIntent)
    }

    private fun getMusicFromStorage(): ArrayList<Music> {
        return storageUtil.loadAudio()
    }

    private fun getPositionFromStorage(): Int {
        return storageUtil.loadAudioIndex()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        mBinding = null
    }
}