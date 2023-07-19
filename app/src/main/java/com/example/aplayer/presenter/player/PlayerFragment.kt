package com.example.aplayer.presenter.player

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import com.example.aplayer.presenter.main.ITEM_POSITION
import com.example.aplayer.utils.millisecondsToTime
import kotlin.properties.Delegates

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private lateinit var musicList: ArrayList<Music>
    private var position = -1
    private var itemPosition = -1
    private val playerViewModel by viewModels<PlayerViewModel>(
        ownerProducer = { requireActivity() }
    )
    private var player: PlayerService? = null
    private var isServiceBound by Delegates.notNull<Boolean>()
    private val storageUtil by lazy { StorageUtil(requireContext()) }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            player = binder.getService()
            if(position != itemPosition && itemPosition != -1) {
                playerViewModel.setStartDuration(0)
                playerViewModel.launchSeekCount(storageUtil.isPlayingPosition())
            } else launchSeekCount(storageUtil.isPlayingPosition())
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    //Player service next/previous and play/pause receiver
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlaying = storageUtil.isPlayingPosition()
            setPlayPauseIcon(isPlaying)
            val pos = getPositionFromStorage()
            if (pos != playerViewModel.lastPosition.value) {
                playerViewModel.setStartDuration(0)
                playerViewModel.setLastPosition(pos)
            }
            playerViewModel.launchSeekCount(isPlaying)
        }
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
        durationObserver()
        seekBarChangeListener()
        playPause()
        skipToNext()
        skipToPrevious()
        shuffle()
        repeat()
    }

    private fun init() {
        musicList = getMusicFromStorage()
        position = getPositionFromStorage()
        itemPosition = getPositionFromBundle() ?: -1
        isServiceBound = isMyServiceRunning(PlayerService::class.java)
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

    private fun playAudio() {
        val isPlaying = storageUtil.isPlayingPosition()
        if (player == null) {
            val playerIntent = Intent(requireContext(), PlayerService::class.java)
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        //Check is service is active
        if (!isServiceBound) {
            val playerIntent = Intent(requireContext(), PlayerService::class.java)
            playerViewModel.setLastPosition(position)
            activity?.startService(playerIntent)
        } else if (position != itemPosition && itemPosition != -1) {
            //Service is active
            //Send media with BroadcastReceiver
            playerViewModel.setLastPosition(position)
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            activity?.sendBroadcast(broadcastIntent)
            launchSeekCount(storageUtil.isPlayingPosition())
        } else {
            playerViewModel.setLastPosition(position)
            setPlayPauseIcon(isPlaying)
        }
    }

    private fun setupUI(position: Int) {
        val music = musicList[position]
        with(binding) {
            if (playerName.text != music.name) {
                Glide.with(requireContext())
                    .load(music.artUri)
                    .centerCrop()
                    .placeholder(R.drawable.im_default)
                    .into(playerAlbumArt)
                playerName.text = music.name
                playerArtist.text = music.artist
                rightDuration.text = music.duration.millisecondsToTime()
                leftDuration.text = getString(R.string.timer)
                binding.playerSeekBar.progress = 0
                binding.playerSeekBar.max = music.duration?.toInt() ?: 0
            }
        }
    }

    private fun playPause() {
        binding.playerPlay.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_click))
            if (storageUtil.isPlayingPosition()) {
                player?.pauseMusic()
            } else {
                player?.resumeMusic()
            }
        }
    }

    private fun shuffle() {
        binding.playerShuffle.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_click))
        }
    }

    private fun repeat() {
        binding.playerRepeat.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_click))
            player?.repeatMusic()
        }
    }

    private fun skipToNext() {
        binding.playerNext.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_click))
            player?.skipToNext()
        }
    }

    private fun skipToPrevious() {
        binding.playerPrevious.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.image_click))
            player?.skipToPrevious()
        }
    }


    private fun seekBarChangeListener() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerViewModel.setStartDuration(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekMusic(seekBar.progress)
            }

        })
    }

    private fun launchSeekCount(isPlaying: Boolean) {
        player?.storeCurrentDuration()
        playerViewModel.setStartDuration(storageUtil.loadCurrentDuration())
        playerViewModel.launchSeekCount(isPlaying)
    }

    private fun setPlayPauseIcon(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playerPlay.setImageResource(R.drawable.baseline_pause_circle_filled)
        } else {
            binding.playerPlay.setImageResource(R.drawable.baseline_play_circle_filled)
        }
    }

    private fun positionObserver() {
        playerViewModel.lastPosition.observe(viewLifecycleOwner) {
            setupUI(it)
        }
    }

    private fun durationObserver() {
        playerViewModel.duration.observe(viewLifecycleOwner) {
            binding.playerSeekBar.progress = it
            binding.leftDuration.text = it.millisecondsToTime()
        }
    }

    private fun getMusicFromStorage(): ArrayList<Music> {
        return storageUtil.loadAudio()
    }

    private fun getPositionFromStorage(): Int {
        return storageUtil.loadAudioIndex()
    }

    private fun getPositionFromBundle(): Int? {
        return arguments?.getInt(ITEM_POSITION)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        arguments?.putInt(ITEM_POSITION, playerViewModel.lastPosition.value ?: -1)
        playerViewModel.setStartDuration(binding.playerSeekBar.progress)
        mBinding = null
    }
}