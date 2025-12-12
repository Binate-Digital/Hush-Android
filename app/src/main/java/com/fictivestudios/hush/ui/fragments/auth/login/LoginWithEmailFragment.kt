package com.fictivestudios.hush.ui.fragments.auth.login

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.data.responses.LoginUserRequest
import com.fictivestudios.hush.data.responses.RecoverUserRequest
import com.fictivestudios.hush.databinding.DialogRecoverAccountBinding
import com.fictivestudios.hush.databinding.FragmentLoginWithEmailBinding
import com.fictivestudios.hush.utils.hideKeyboard
import com.fictivestudios.hush.utils.isValidEmail
import com.fictivestudios.hush.utils.setSafeOnClickListener

class LoginWithEmailFragment : BaseFragment(R.layout.fragment_login_with_email),
    View.OnClickListener {

    private var _binding: FragmentLoginWithEmailBinding? = null
    val binding
        get() = _binding!!

    val viewModel: LoginViewModel by viewModels()
    private var dialogRecoverAccountBinding: DialogRecoverAccountBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginWithEmailBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
        dialogRecoverAccountBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setOnClickListener()
    }

    override fun initialize() {viewModel.init()}

    override fun setObserver() {
        viewModel.loginUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    data.value.data?.let { loginData ->
                        binding.progress.gone()
                        showToast(data.value.message)
                        if (loginData.isDeleted == LoginUserDeleteEnum.USER_DELETED.getValue()) {
                            requireActivity().runOnUiThread { showRecoverAccountDialog(loginData._id) }

                        } else {
                            requireActivity().runOnUiThread {
                                findNavController().navigate(
                                    LoginWithEmailFragmentDirections.actionLoginWithEmailFragmentToOtpVerificationFragment(
                                        data.value.token.toString(),
                                        loginData._id, "", "email_fragment", ""
                                    )
                                )
                            }

                        }
                        enableUserTouch()
                    }
                }


                is Resource.Failure -> {
                    binding.progress.gone()
                    showToast(data.message.toString())
                    enableUserTouch()
                }

                is Resource.Loading -> {
                    binding.progress.show()
                }

                else -> {}
            }
        }

        viewModel.recoverUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    data.value.data?.let { loginData ->
                        binding.progress.gone()
                        showToast(data.value.message)
                        findNavController().navigate(
                            LoginWithEmailFragmentDirections.actionLoginWithEmailFragmentToOtpVerificationFragment(
                                data.value.token.toString(),
                                loginData._id?:"", "", "email_fragment", ""
                            )
                        )
                        enableUserTouch()
                    }
                }

                is Resource.Failure -> {
                    binding.progress.gone()
                    showToast(data.message.toString())
                    dialogRecoverAccountBinding!!.buttonDelete.isClickable = true
                    enableUserTouch()
                }

                is Resource.Loading -> {
                    binding.progress.show()
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonContinue.setSafeOnClickListener {
            hideKeyboard(requireActivity())
            login()
            setObserver()

        }
        binding.textInputLayoutEmail.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()

            }

            binding.textInputLayoutEmail.id -> {
                binding.textInputLayoutEmail.background = null

            }
        }
    }

    private fun login() {

        if (binding.textInputEditTextEmail.text.toString().trim().isEmpty()) {
            showToast("Email field can't be empty")
            return
        }
        if (!binding.textInputEditTextEmail.text.toString().isValidEmail()) {
            showToast("Please enter a valid email")
            return
        }
        val data =
            LoginUserRequest(
                binding.textInputEditTextEmail.text.toString().trim(),
                viewModel.fcmToken?:"",
                "android"
            )
        viewModel.loginUserApi(data)
        disableUserTouch()

    }

    private fun showRecoverAccountDialog(userId: String) {
        // Build and show the alert dialog
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogRecoverAccountBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_recover_account,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogRecoverAccountBinding!!.root)
            dialog.setCancelable(false)

            dialogRecoverAccountBinding!!.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogRecoverAccountBinding!!.buttonDelete.setSafeOnClickListener {
                dialogRecoverAccountBinding!!.buttonDelete.isClickable = false
                viewModel.recoverUserAccountApi(RecoverUserRequest(userId))
                disableUserTouch()
                dialog.dismiss()

            }

            dialogRecoverAccountBinding!!.imageViewBack.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}