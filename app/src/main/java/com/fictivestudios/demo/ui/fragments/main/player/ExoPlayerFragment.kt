package com.fictivestudios.demo.ui.fragments.main.player


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.databinding.FragmentExoPlayerBinding
import kotlin.getValue


class ExoPlayerFragment : BaseFragment(R.layout.fragment_exo_player) {

    private var _binding: FragmentExoPlayerBinding? = null
    val binding
        get() = _binding!!
    private val args by navArgs<ExoPlayerFragmentArgs>()
    private var player: ExoPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExoPlayerBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }


    override fun initialize() {}


    override fun setOnClickListener() {}

    override fun setObserver() {


    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        Log.d("args.url","${args.url}")
        // Replace with your video URL
        val mediaItem = MediaItem.fromUri(Uri.parse(args.url))
        player?.setMediaItem(mediaItem)

        player?.prepare()
        player?.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

}