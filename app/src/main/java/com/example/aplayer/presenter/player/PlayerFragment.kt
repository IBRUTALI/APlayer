package com.example.aplayer.presenter.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.secondsToTime
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private var musicList = emptyList<Music>()
    private val playerViewModel by lazy { PlayerViewModel(activity?.application!!) }
    private val compositeDisposable = CompositeDisposable()
    private val audioDisposable = CompositeDisposable()

    private fun init() {
        playerViewModel.audioPosition.value = getPositionFromBundle()
        musicList = getMusicFromBundle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        createMediaPlayer()
        setupUI(playerViewModel.audioPosition.value!!)
        setupButtons()
        positionChangeObserver()
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

    private fun positionChangeObserver() {
        playerViewModel.audioPosition.observe(viewLifecycleOwner) { position ->
            if (position in getMusicFromBundle().indices) {
                setupUI(position)
                createMediaPlayer()
            }
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

    private fun setupButtons() {
        with(binding) {
            playerPlay.setOnClickListener {
                if (playerViewModel.musicIsPlaying()) {
                    val dispose = playerViewModel.stopMusic()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {}
                    audioDisposable.dispose()
                    compositeDisposable.add(dispose)
                    playerPlay.setImageResource(R.drawable.baseline_play_circle_filled)
                } else {
                    val dispose = playerViewModel.playMusic()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {}
                    audioDisposable.clear()
                    compositeDisposable.add(dispose)
                    seekBarObserver()
                    playerPlay.setImageResource(R.drawable.baseline_pause_circle_filled)
                }
            }

            playerNext.setOnClickListener {
                if (0 <= playerViewModel.audioPosition.value!! &&
                    playerViewModel.audioPosition.value!! < musicList.size - 1
                )
                    skipMusic()
            }

            playerPrevious.setOnClickListener {
                if (0 < playerViewModel.audioPosition.value!! &&
                    playerViewModel.audioPosition.value!! <= musicList.size - 1
                )
                    previousMusic()
            }

            playerRepeat.setOnClickListener {
                playerViewModel.repeatMusic()
                if (playerViewModel.getMediaPlayer().isLooping) {
                    playerRepeat.setImageResource(R.drawable.baseline_repeat_one)
                } else playerRepeat.setImageResource(R.drawable.baseline_repeat)
            }

            playerShuffle.setOnClickListener {
                playerViewModel.shuffleMusic()
            }
        }
    }

    private fun skipMusic() {
        playerViewModel.skipMusic()
        audioDisposable.dispose()
    }

    private fun previousMusic() {
        playerViewModel.previousMusic()
        audioDisposable.dispose()
    }

    private fun seekBarChangeListener() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerViewModel.getMediaPlayer().seekTo(progress)
                    binding.playerSeekBar.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })
    }

    private fun createMediaPlayer() {
        playerViewModel.initMediaPlayer(getMusicFromBundle()[playerViewModel.audioPosition.value!!])
        playerViewModel.getMediaPlayer().start()
        seekBarObserver()
    }

    private fun seekBarObserver() {
        Log.d("!@#", "is playing: ${playerViewModel.musicIsPlaying()}")
        val dispose = initSeekBar()
            .subscribeOn(Schedulers.computation())
            .delay(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { progress ->
                binding.playerSeekBar.progress = progress
                binding.leftDuration.text = progress.secondsToTime()
            }
        audioDisposable.add(dispose)
    }

    private fun initSeekBar(): Observable<Int> {
        binding.playerSeekBar.max = playerViewModel.getMediaPlayer().duration
        return Observable.create { subscriber ->
            while (playerViewModel.musicIsPlaying()) {
                val progress = playerViewModel.getMediaPlayer().currentPosition
                subscriber.onNext(progress)
            }
        }
    }

    private fun getMusicFromBundle(): List<Music> {
        return arguments?.getParcelableArrayList("music")!!
    }

    private fun getPositionFromBundle(): Int {
        return arguments?.getInt("position") as Int
    }

    override fun onPause() {
        super.onPause()

        compositeDisposable.dispose()
        audioDisposable.dispose()
        playerViewModel.getMediaPlayer().release()

    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}