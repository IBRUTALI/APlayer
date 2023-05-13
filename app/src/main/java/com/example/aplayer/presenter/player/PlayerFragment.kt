package com.example.aplayer.presenter.player

import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.Repositories
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.viewModelCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private val musicList = emptyList<Music>()
    private val playerViewModel by lazy { PlayerViewModel(activity?.application!!) }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createMediaPlayer()
        setupUI()
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

    private fun getMusicFromBundle(): Music {
        return arguments?.getSerializable("music") as Music
    }

    private fun setupUI() {
        val music = getMusicFromBundle()
        with(binding) {
            Glide.with(requireContext())
                .load(music.artUri)
                .centerCrop()
                .placeholder(R.drawable.im_default)
                .into(playerAlbumArt)

            playerName.text = music.name
            playerArtist.text = music.artist
            playerPlay.setOnClickListener {
                if(playerViewModel.musicIsPlaying()) {
                    val dispose = playerViewModel.stopMusic()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {}
                    compositeDisposable.add(dispose)
                    playerPlay.setImageResource(R.drawable.baseline_play_circle_filled)
                }
                else {
                    val dispose = playerViewModel.playMusic()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe {}
                    compositeDisposable.add(dispose)
                        playerPlay.setImageResource(R.drawable.baseline_pause_circle_filled)
                }
            }


        }

    }

    private fun setSeekBarProgress(progress: Int? = 0, maxProgress: Int) {
        binding.playerSeekBar.progress = progress!!
        binding.playerSeekBar.max = maxProgress
        Log.d("!@#", maxProgress.toString())
    }

    private fun seekBarChangeListener() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    playerViewModel.getMediaPlayer().seekTo(progress)
                    Log.d("!@#", progress.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })
    }

//    private fun setDataToList() {
//        musicList = arguments?.getSerializable("music") as List<Music>
//    }

    private fun createMediaPlayer() {
        playerViewModel.initMediaPlayer(getMusicFromBundle())
        playerViewModel.getMediaPlayer().start()
        setSeekBarProgress(0, playerViewModel.getMediaPlayer().duration)
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.dispose()
        playerViewModel.closePlayer()

    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}