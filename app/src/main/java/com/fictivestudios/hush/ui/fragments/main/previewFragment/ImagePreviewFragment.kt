package com.fictivestudios.hush.ui.fragments.main.previewFragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.DialogDeleteItemBinding
import com.fictivestudios.hush.databinding.FragmentImagePreviewBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.Constants

class ImagePreviewFragment : BaseFragment(R.layout.fragment_image_preview), View.OnClickListener {

    private var _binding: FragmentImagePreviewBinding? = null
    val binding
        get() = _binding!!

    private val args by navArgs<ImagePreviewFragmentArgs>()
    val viewModel: ImagePreviewViewModel by viewModels()

    private var dialogDeleteItemBinding: DialogDeleteItemBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePreviewBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
        dialogDeleteItemBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        setOnBackPressedListener()
        if (args.from == "screenShot" || args.from == "securityFeature") {
            binding.imageViewDelete.show()
        } else {
            binding.imageViewDelete.gone()
        }
        if (args.imageLink != "" && args.imageLink != "null") {
            val url = Constants.IMAGE_BASE_URL + args.imageLink
            Glide.with(binding.imageViewPreview.context)
                .load(url).placeholder(R.drawable.person)
                .into(binding.imageViewPreview)

        } else {
            binding.imageViewPreview.setImageResource(R.drawable.person)
        }
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.imageViewDelete.setOnClickListener(this)
    }

    override fun setObserver() {
        viewModel.deleteScreenShotResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    binding.progress.hide()
                    showToast(data.value.message)
                    findNavController().popBackStack()
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(data.message.toString())
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

            binding.imageViewDelete.id -> {
                showDeleteAccountMessageBox()

            }

        }
    }


    private fun showDeleteAccountMessageBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogDeleteItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_delete_item,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogDeleteItemBinding!!.root)
            dialog.setCancelable(false)


            dialogDeleteItemBinding!!.buttonCancel.text = "No"
            dialogDeleteItemBinding!!.buttonDelete.text = "Yes"
            dialogDeleteItemBinding!!.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogDeleteItemBinding!!.imageViewCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogDeleteItemBinding!!.buttonDelete.setOnClickListener {
                binding.progress.show()
                val deleteItem = arrayListOf<String>()
                deleteItem.add(args.attachmentId ?: "")
                if (args.from == "screenShot") {
                    viewModel.deleteScreenShotByIds(deleteItem)
                } else {
                    viewModel.deleteSecurityFeatureByIds(deleteItem)
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setOnBackPressedListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }
}