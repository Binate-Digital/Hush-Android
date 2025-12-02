package com.fictivestudios.hush.ui.fragments.main.patternFragment

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.DialogProfileBinding
import com.fictivestudios.hush.databinding.FragmentPatternBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.PatternLockView
import com.fictivestudios.hush.utils.setSafeOnClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PatternFragment : BaseFragment(R.layout.fragment_pattern), View.OnClickListener {

    private var _binding: FragmentPatternBinding? = null
    val binding
        get() = _binding!!
    private var dialogProfileCompletedBinding: DialogProfileBinding? = null
    private var dialog: AlertDialog? = null
    private var isForSave = false
    private var patternArray = arrayListOf<String>()
    private val viewModel: PatternViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatternBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogProfileCompletedBinding = null
        dialog = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        setOnBackPressedListener()
        if (viewModel.userData?.patternLock == 1) {
            binding.buttonSave.text = "Delete"
            isForSave = false
        } else {
            binding.buttonSave.text = "Save"
            isForSave = true
        }

    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonSave.setSafeOnClickListener {
            saveOrDeletePattern()
        }
        binding.buttonClear.setOnClickListener(this)
        binding.patternLockView.setOnPatternListener(object : PatternLockView.OnPatternListener {
            override fun onStarted() {
                super.onStarted()
            }

            override fun onProgress(ids: ArrayList<Int>) {
                super.onProgress(ids)
            }

            override fun onComplete(ids: ArrayList<Int>): Boolean {
                var idList = arrayListOf<String>()
                for (id in ids) {
                    idList.add(id.toString())
                }
                patternArray = idList
                binding.patternLockView.isTouched = false
                return true
            }
        })
    }

    override fun setObserver() {

        viewModel.createPatternResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()
                    binding.patternLockView.reset()
                    lifecycleScope.launch {
                        viewModel.updatePatternLock(1)
                        delay(1000)
                        requireActivity().runOnUiThread {
                            showPinCreatedDialog()
                        }
                    }

                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    binding.patternLockView.reset()
                    binding.patternLockView.isTouched = true
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.deletePatternResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()
                    showToast(it.value.message)
                    binding.patternLockView.reset()
                    lifecycleScope.launch {
                        viewModel.updatePatternLock(0)
                        delay(1000)
                        requireActivity().runOnUiThread {
                            showPinCreatedDialog()
                        }
                    }
                    findNavController().popBackStack()
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    binding.patternLockView.reset()
                    binding.patternLockView.isTouched = true
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    private fun saveOrDeletePattern() {
        if (isForSave) {
            viewModel.createPattern(patternArray)
        } else {
            viewModel.deletePinCode(patternArray)
        }

        binding.progress.show()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }
            binding.buttonClear.id -> {
                binding.patternLockView.reset()
                patternArray.clear()
                binding.patternLockView.isTouched = true
            }
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

    private fun showPinCreatedDialog() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogProfileCompletedBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_profile,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogProfileCompletedBinding!!.root)
            dialog.setCancelable(false)

            dialogProfileCompletedBinding!!.imageViewCancel.gone()
            dialogProfileCompletedBinding!!.textViewDes.text =
                "Pattern has been added successfully."

            dialogProfileCompletedBinding!!.buttonContinue.setOnClickListener {
                findNavController().popBackStack()
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}