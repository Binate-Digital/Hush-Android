package com.fictivestudios.hush.ui.fragments.main.pinFragment

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
import com.fictivestudios.hush.data.responses.CreatePinCodeRequest
import com.fictivestudios.hush.data.responses.EditPinCodeRequest
import com.fictivestudios.hush.databinding.DialogProfileBinding
import com.fictivestudios.hush.databinding.FragmentPinBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.hideKeyboard
import com.fictivestudios.hush.utils.setSafeOnClickListener
import kotlinx.coroutines.launch

class PinFragment : BaseFragment(R.layout.fragment_pin), View.OnClickListener {

    private var _binding: FragmentPinBinding? = null
    val binding
        get() = _binding!!
    private var dialogProfileCompletedBinding: DialogProfileBinding? = null
    private var dialog: AlertDialog? = null
    private val viewModel: PinCodeViewModel by viewModels()
    private var isForEdit = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater)
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
        if (viewModel.userData?.pinVerified == 1) {
            isForEdit = true
            binding.textInputLayoutEnterOldPin.show()
            binding.textInputEditTextEnterPin.hint = "Enter new pin code"
            binding.textInputEditTextReEnterPin.hint = "Confirm new pin code"
        } else {
            isForEdit = false
            binding.textInputLayoutEnterOldPin.gone()
            binding.textInputEditTextEnterPin.hint = "Enter pin code"
            binding.textInputEditTextReEnterPin.hint = "Confirm pin code"
        }
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonSave.setSafeOnClickListener {
            savePin()
        }
    }

    override fun setObserver() {
        viewModel.pinCodeResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    requireActivity().runOnUiThread {
                        binding.progress.hide()
                        showPinCreatedDialog()
                    }
                    lifecycleScope.launch {
                        viewModel.saveUserPinType(1)
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message.toString())
                    binding.progress.hide()
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

    private fun savePin() {

        if (viewModel.userData?.pinVerified == 1) {
            if (binding.textInputEditTextEnterOldPin.text!!.toString().trim().isEmpty()) {
                showToast("Existing Pin code must not be empty.")
                return
            }
            if (binding.textInputEditTextEnterPin.text!!.toString().trim().length < 4) {
                showToast("Existing Pin code should be  4 to 16 digits")
                return
            }
        }

        if (binding.textInputEditTextEnterPin.text!!.toString().trim().isEmpty()) {
            showToast("Enter Pin code must not be empty.")
            return
        }
        if (binding.textInputEditTextEnterPin.text!!.toString().trim()
                .isEmpty() || binding.textInputEditTextEnterPin.text!!.toString().trim().length < 4
        ) {
            showToast("Enter Pin code should be  4 to 16 digits")
            return
        }

        if (binding.textInputEditTextReEnterPin.text!!.toString().trim().isEmpty()) {
            showToast("Re Enter Pin code must not be empty.")
            return
        }
        if (binding.textInputEditTextReEnterPin.text!!.toString().trim().length < 4) {
            showToast("Re Enter Pin code should be  4 to 16 digits")
            return
        }

        if (binding.textInputEditTextEnterPin.text.toString()
                .trim() != binding.textInputEditTextReEnterPin.text.toString().trim()
        ) {
            showToast("Enter Pin code and Re Enter Pin code did not matched.")
            return
        }
        hideKeyboard(requireActivity())
        if (viewModel.userData?.pinVerified == 1) {
            val data = EditPinCodeRequest(
                binding.textInputEditTextEnterOldPin.text.toString().trim(),
                binding.textInputEditTextEnterPin.text.toString().trim(),
                binding.textInputEditTextReEnterPin.text.toString().trim()
            )
            viewModel.editPinCode(data)
        } else {
            val data = CreatePinCodeRequest(
                binding.textInputEditTextEnterPin.text.toString().trim(),
                binding.textInputEditTextReEnterPin.text.toString().trim()
            )
            viewModel.createPinCode(data)
        }

        binding.progress.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
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
            val text =
                if (isForEdit) "Pin has been edit successfully." else "Pin has been added successfully."
            dialogProfileCompletedBinding!!.textViewDes.text = text

            dialogProfileCompletedBinding!!.buttonContinue.setSafeOnClickListener {
                findNavController().popBackStack()
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}