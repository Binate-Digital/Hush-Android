package com.fictivestudios.hush.ui.fragments.crop

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import com.fictivestudios.hush.R
import com.fictivestudios.hush.databinding.FragmentCropBinding
import com.fictivestudios.hush.utils.compressAndSaveImage


class CropFragment(private val imageUri: Uri, val listener: ImageCrop) : DialogFragment(),
    View.OnClickListener {

    private var _binding: FragmentCropBinding? = null
    val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CropFragmentStyle)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropBinding.inflate(inflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

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
        setOnClickListener()
    }

    private fun initialize() {
        binding.cropImageView.setImageUriAsync(imageUri)
    }

    private fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.textViewCrop.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                this.dismiss()
            }

            binding.textViewCrop.id -> {
               val croppedImage = binding.cropImageView.getCroppedImage()?.let { compressAndSaveImage(requireContext(),it,20) }
                listener.getImageCropped(croppedImage)
             this.dismiss()
            }
        }
    }

    fun interface ImageCrop {
        fun getImageCropped(cropImage: Uri?)
    }
}

