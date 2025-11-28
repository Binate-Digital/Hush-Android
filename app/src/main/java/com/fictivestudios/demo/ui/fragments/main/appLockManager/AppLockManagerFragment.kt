package com.fictivestudios.demo.ui.fragments.main.appLockManager

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.LockTypeRequest
import com.fictivestudios.demo.databinding.FragmentAppLockManagerBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AppLockManagerFragment : BaseFragment(R.layout.fragment_app_lock_manager),
    View.OnClickListener {

    private var _binding: FragmentAppLockManagerBinding? = null
    val binding
        get() = _binding!!

    val viewModel: AppLockViewModel by viewModels()

    private var selectedSecurity = LockEnumType.NONE
    private var isPinEnabled = false
    private var isPatternEnabled = false
    private var isFingerPrintEnabled = false

    private var isFingerPrintEnrolled = false
    private lateinit var biometricManager: BiometricManager
    private val REQUEST_CODE = 1213

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppLockManagerBinding.inflate(inflater)
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

    override fun initialize() {
        biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MY_APP_TAG", "BIOMETRIC_SUCCESS")
                binding.cardViewFingerPrint.gone()
                binding.fingerPrintView.show()
                isFingerPrintEnrolled = true
            }


            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_NO_HARDWARE")
                binding.cardViewFingerPrint.gone()
                binding.fingerPrintView.gone()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("MY_APP_TAG", "BIOMETRIC_ERROR_HW_UNAVAILABLE")
                binding.cardViewFingerPrint.gone()
                binding.fingerPrintView.gone()

            }


            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                isFingerPrintEnrolled = false
                Log.d("MY_APP_TAG", "initialize: BIOMETRIC_ERROR_NONE_ENROLLED")
                binding.cardViewFingerPrint.show()
                binding.fingerPrintView.show()
                // Prompts the user to create credentials that your app accepts.

            }
        }
        setOnBackPressedListener()
        binding.pinSwitch.isChecked = viewModel.userData?.pinLock == 1
        binding.fingerPrintSwitch.isChecked = viewModel.userData?.fingerprintLock == 1
        binding.patternSwitch.isChecked = viewModel.userData?.patternLock == 1

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            Log.d("FingerData", data.toString())
            showToast("Finger Print Added Successfully.")
        } else {
            showToast(resultCode.toString())
        }
    }

    override fun setObserver() {
        viewModel.lockTypeResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        when (selectedSecurity) {
                            LockEnumType.IS_FOR_PATTERN -> {
                                val value = if (isPatternEnabled) 1 else 0
                                if (isPatternEnabled) {
                                    requireActivity().runOnUiThread {
                                        binding.patternSwitch.isChecked = isPatternEnabled
                                        binding.pinSwitch.isChecked = false
                                        binding.fingerPrintSwitch.isChecked = false
                                    }

                                }
                                viewModel.saveUserPatternOffOn(value)
                            }

                            LockEnumType.IS_FOR_FINGER_PRINT -> {
                                val value = if (isFingerPrintEnabled) 1 else 0
                                if (isFingerPrintEnabled) {
                                    requireActivity().runOnUiThread {
                                        binding.fingerPrintSwitch.isChecked = isFingerPrintEnabled
                                        binding.patternSwitch.isChecked = false
                                        binding.pinSwitch.isChecked = false
                                    }
                                }
                                viewModel.saveUserFingerPrintOffOn(value)
                            }

                            LockEnumType.IS_FOR_PIN -> {
                                val value = if (isPinEnabled) 1 else 0
                                if (isPinEnabled) {
                                    requireActivity().runOnUiThread {
                                        binding.pinSwitch.isChecked = isPinEnabled
                                        binding.patternSwitch.isChecked = false
                                        binding.fingerPrintSwitch.isChecked = false
                                    }
                                }
                                viewModel.saveUserPinOffOn(value)
                            }

                            else -> {
                                showToast("Invalid security selection.")
                            }
                        }

                        delay(1000)

                    }
                    showToast(it.value.message)
                    binding.progress.hide()
                    enableUserTouch()
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(it.message.toString())
                    requireActivity().runOnUiThread {
                        if (viewModel.userData?.pinLock == 1) {
                            binding.pinSwitch.isChecked = true
                            binding.patternSwitch.isChecked = false
                            binding.fingerPrintSwitch.isChecked = false
                        } else if (viewModel.userData?.patternLock == 1) {
                            binding.pinSwitch.isChecked = false
                            binding.patternSwitch.isChecked = true
                            binding.fingerPrintSwitch.isChecked = false
                        } else if (viewModel.userData?.fingerprintLock == 1) {
                            binding.pinSwitch.isChecked = false
                            binding.patternSwitch.isChecked = false
                            binding.fingerPrintSwitch.isChecked = true
                        } else {
                            binding.pinSwitch.isChecked = false
                            binding.patternSwitch.isChecked = false
                            binding.fingerPrintSwitch.isChecked = false
                        }
                    }
                    enableUserTouch()
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

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.cardViewPin.setOnClickListener(this)
        binding.cardViewPattern.setOnClickListener(this)
        binding.imageViewBack.setOnClickListener(this)
        binding.pinSwitch.setOnClickListener(this)
        binding.patternSwitch.setOnClickListener(this)
        binding.fingerPrintSwitch.setOnClickListener(this)
        binding.cardViewFingerPrint.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewFingerPrint.id -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }

            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

            binding.pinSwitch.id -> {
                binding.progress.show()
                selectedSecurity = LockEnumType.IS_FOR_PIN
                isPinEnabled = binding.pinSwitch.isChecked
                if (isPinEnabled) {
                    binding.fingerPrintSwitch.isChecked = false
                    binding.patternSwitch.isChecked = false
                }
                viewModel.lockTypeToggle(LockTypeRequest(LockEnumType.PIN.getValue()))
                disableUserTouch()
                setObserver()
            }

            binding.fingerPrintSwitch.id -> {
                if (!isFingerPrintEnrolled) {
                    binding.fingerPrintSwitch.isChecked = false
                    showToast("Please set finger print first.")
                    return
                }
                binding.progress.show()
                selectedSecurity = LockEnumType.IS_FOR_FINGER_PRINT
                isFingerPrintEnabled = binding.fingerPrintSwitch.isChecked
                viewModel.lockTypeToggle(LockTypeRequest(LockEnumType.FINGER_PRINT.getValue()))
                disableUserTouch()
                setObserver()
            }

            binding.patternSwitch.id -> {
                binding.progress.show()
                selectedSecurity = LockEnumType.IS_FOR_PATTERN
                isPatternEnabled = binding.patternSwitch.isChecked
                if (isPatternEnabled) {
                    binding.pinSwitch.isChecked = false
                    binding.fingerPrintSwitch.isChecked = false
                }
                viewModel.lockTypeToggle(LockTypeRequest(LockEnumType.PATTERN.getValue()))
                disableUserTouch()
                setObserver()
            }

            binding.cardViewPin.id -> {
                findNavController().navigate(AppLockManagerFragmentDirections.actionAppLockManagerToPinFragment())
            }

            binding.cardViewPattern.id -> {
                findNavController().navigate(AppLockManagerFragmentDirections.actionAppLockManagerToPatternFragment())
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
}