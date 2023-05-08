package com.example.aplayer.presenter.player

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentPlayerBinding
import com.example.aplayer.domain.music.model.Music

class PlayerFragment : Fragment() {
    private var mBinding: FragmentPlayerBinding? = null
    private val binding get() = mBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createMediaPlayer()
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

    private fun createMediaPlayer() {
        val musicUri = getMusicFromBundle().uri
        var mediaPlayer = MediaPlayer.create(requireContext(), musicUri)
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}