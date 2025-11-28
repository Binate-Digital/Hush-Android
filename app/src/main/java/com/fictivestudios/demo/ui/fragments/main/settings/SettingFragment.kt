package com.fictivestudios.demo.ui.fragments.main.settings

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.BuildConfig
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.databinding.DialogDeleteAccountBinding
import com.fictivestudios.demo.databinding.FragmentSettingBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.activities.MainActivity
import com.fictivestudios.demo.utils.setSafeOnClickListener
import com.fictivestudios.demo.utils.startNewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingFragment : BaseFragment(R.layout.fragment_setting), View.OnClickListener {

    private var _binding: FragmentSettingBinding? = null

    val binding
        get() = _binding!!

    val viewModel: SettingViewModel by viewModels()



    private var dialogDeleteAccountBinding: DialogDeleteAccountBinding? = null
    private var dialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogDeleteAccountBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        binding.notificationSwitch.isChecked = viewModel.userData?.notification == 1
            binding.darkModeSwitch.isChecked = viewModel.isDarkModeSelected
        val versionName: String = BuildConfig.VERSION_NAME
        binding.textViewVersion.text = "Version $versionName"
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Biometric login for Hush App")
//            .setSubtitle("Log in using your biometric credential")
//            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//            .build()
//

    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.blockUserView.setOnClickListener(this)
        binding.buttonDeleteAccount.setSafeOnClickListener { showDeleteAccountMessageBox() }
        binding.appLockView.setOnClickListener(this)
        binding.paymentSettingView.setOnClickListener(this)

        binding.notificationSwitch.setOnClickListener(this)
        binding.darkModeSwitch.setOnClickListener {
            lifecycleScope.launch {
                if(binding.darkModeSwitch.isChecked){
                    setDarkMode()
                }else{

                    setLightMode()
                }
                viewModel.saveDeviceTheme(binding.darkModeSwitch.isChecked)
            }

        }
    }

    private fun setDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    // Function to set light mode
    private fun setLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun setObserver() {
        viewModel.deleteUserResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast(it.value.message)
                    it.value.data.let {
                        enableUserTouch()


                        if (viewModel.userData?.userSocialType == "phone" || viewModel.userData?.userSocialType == "google") {
                            (requireActivity() as DashBoardActivity).signOutSocialLogin()
                        } else {
                            lifecycleScope.launch {
                                viewModel.preferenceLogout()
                                delay(1500)
                                binding.progress.hide()
                                val bundle = Bundle()
                                bundle.putString("userLogout", "logout")
                                requireActivity().startNewActivity(MainActivity::class.java, bundle)
                                requireActivity().finish()

                            }
                        }
                        enableUserTouch()
                    }
                }

                is Resource.Failure -> {
                    enableUserTouch()
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

        viewModel.notificationToggleResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast(it.value.message)
                    it.value.data.let { data ->
                        binding.progress.hide()

                        lifecycleScope.launch {
                            if (data != null) {
                                viewModel.saveLoggedInUser(data)
                            }
                        }
                    }
                }

                is Resource.Failure -> {
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

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.paymentSettingView.id -> {
                showToast("Will be implement in next phase.")
            }

            binding.notificationSwitch.id -> {
                binding.progress.show()
                viewModel.notificationToggle()
            }

            binding.appLockView.id -> {
                findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToAppLockManagerFragment())
            }

            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }


            binding.blockUserView.id -> {
                findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToBlockUserFragment())
            }
        }
    }

    private fun showDeleteAccountMessageBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogDeleteAccountBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_delete_account,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogDeleteAccountBinding!!.root)
            dialog.setCancelable(false)


            dialogDeleteAccountBinding!!.buttonCancel.text = "No"
            dialogDeleteAccountBinding!!.buttonDelete.text = "Yes"
            dialogDeleteAccountBinding!!.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogDeleteAccountBinding!!.imageViewCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogDeleteAccountBinding!!.buttonDelete.setOnClickListener {
                viewModel.deleteUserByToken()
                binding.progress.show()
                dialog.dismiss()
                disableUserTouch()

            }

            dialog.show()
        }
    }
}