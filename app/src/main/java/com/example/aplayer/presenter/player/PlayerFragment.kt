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
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!
    private val musicList = emptyList<Music>()
    private var mediaPlayer = MediaPlayer()

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
            var length = mediaPlayer.currentPosition
            playerPlay.setOnClickListener {
                if(mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    length = mediaPlayer.currentPosition
                    playerPlay.setImageResource(R.drawable.baseline_play_circle_filled)
                }
                else {
                    mediaPlayer.prepare()
                    mediaPlayer.setOnPreparedListener {
                        mediaPlayer.seekTo(length)
                        mediaPlayer.start()
                        playerPlay.setImageResource(R.drawable.baseline_pause_circle_filled)
                    }
                }
            }


        }

    }

    private fun mediaPlayerListener() {

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
                    mediaPlayer.seekTo(progress)
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
        val musicUri = getMusicFromBundle().uri
        mediaPlayer = MediaPlayer.create(requireContext(), musicUri)
        mediaPlayer.start()
        setSeekBarProgress(0, mediaPlayer.duration)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}