package com.fictivestudios.demo.ui.fragments.main.registerPhoneNo

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.VerifyNumberRequest
import com.fictivestudios.demo.databinding.DialogVerifyPhoneCodeBinding
import com.fictivestudios.demo.databinding.FragmentRegisterPhoneNoBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.utils.hideKeyboard
import com.fictivestudios.demo.utils.setSafeOnClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentRegisterPhoneNo : BaseFragment(R.layout.fragment_register_phone_no),
    View.OnClickListener {

    private var _binding: FragmentRegisterPhoneNoBinding? = null
    val binding
        get() = _binding!!
    private var dialogVerificationCodeBinding: DialogVerifyPhoneCodeBinding? = null
    private var isButtonForDelete = false
    private var dialog: AlertDialog? = null
    private val viewModel: RegisterPhoneNoViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPhoneNoBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogVerificationCodeBinding = null
        dialog = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        binding.ccp.registerCarrierNumberEditText(binding.textInputEditTextPhoneNo)
        binding.ccp.setNumberAutoFormattingEnabled(true)
        if (viewModel.userData?.phoneSid.isNullOrEmpty()) {
            isButtonForDelete = false
            binding.buttonSubmit.text = "Submit"
            binding.textInputEditTextPhoneNo.isClickable = true
            binding.textInputEditTextPhoneNo.isEnabled = true
            binding.textInputEditTextPhoneNo.isFocusable = true
        } else {
            isButtonForDelete = true
            binding.buttonSubmit.text = "Delete"
            Log.d("phone", "${viewModel.userData?.phone}")
            binding.textInputEditTextPhoneNo.setText(viewModel.userData?.phone)
            binding.textInputEditTextPhoneNo.isClickable = false
            binding.textInputEditTextPhoneNo.isEnabled = false
            binding.textInputEditTextPhoneNo.isFocusable = false
        }
        setOnBackPressedListener()

    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonSubmit.setSafeOnClickListener {
            hideKeyboard(requireActivity())
            if (isButtonForDelete) {
                viewModel.deletePhoneNoApi()
            } else {
                saveNumber()
            }
            binding.progress.show()
        }
    }

    override fun setObserver() {
        viewModel.registerPhoneNoResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()
                    lifecycleScope.launch {
                        requireActivity().runOnUiThread {
                            it.value.data?.validationCode?.let { it1 ->
                                showRegisterPhoneNOCreatedDialog(
                                    it1
                                )
                            }
                        }
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
        viewModel.deletePhoneNoResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()
                    showToast(it.value.message)
                    lifecycleScope.launch {
                        viewModel.updatePhoneNoSidInUserData("")
                        delay(1500)
                        findNavController().popBackStack()
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

    private fun saveNumber() {
        val phoneNo = binding.textInputEditTextPhoneNo.text.toString().trim()
        if (phoneNo.isEmpty()) {
            showToast("Phone Number field can't be empty")
            return
        }
        viewModel.registerPhoneNoApi(
            VerifyNumberRequest(
                viewModel.userData?.name ?: "",
                binding.ccp.fullNumberWithPlus
            )
        )
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

    private fun showRegisterPhoneNOCreatedDialog(message: String) {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogVerificationCodeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_verify_phone_code,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogVerificationCodeBinding!!.root)
            dialog.setCancelable(true)

            dialogVerificationCodeBinding!!.imageViewCancel.setOnClickListener {
                dialog.dismiss()
            }


            dialogVerificationCodeBinding!!.textViewDes.text =
                "Your verification code is $message"

            dialog.show()
        }
    }
}