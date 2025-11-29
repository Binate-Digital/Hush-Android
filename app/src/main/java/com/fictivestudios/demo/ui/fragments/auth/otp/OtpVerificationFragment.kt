package com.fictivestudios.demo.ui.fragments.auth.otp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.OTPRequest
import com.fictivestudios.demo.data.responses.RecoverUserRequest
import com.fictivestudios.demo.data.responses.SocialLoginRequest
import com.fictivestudios.demo.databinding.DialogRecoverAccountBinding
import com.fictivestudios.demo.databinding.FragmentOtpVerificationBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.fragments.auth.login.LoginProfileVerifiedEnum
import com.fictivestudios.demo.ui.fragments.auth.login.LoginUserDeleteEnum
import com.fictivestudios.demo.ui.fragments.auth.login.LoginVerifiedEnum
import com.fictivestudios.demo.utils.CircularTimerListener
import com.fictivestudios.demo.utils.TimeFormatEnum
import com.fictivestudios.demo.utils.hideKeyboard
import com.fictivestudios.demo.utils.otpview.OTPListener
import com.fictivestudios.demo.utils.setSafeOnClickListener
import com.fictivestudios.demo.utils.startNewActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


class OtpVerificationFragment : BaseFragment(R.layout.fragment_otp_verification),
    View.OnClickListener, OTPListener {

    private val args by navArgs<OtpVerificationFragmentArgs>()

    private var _binding: FragmentOtpVerificationBinding? = null
    val binding
        get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var verificationId: String
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val viewModel: OtpViewModel by viewModels()
    private val otpEditTexts = ArrayList<EditText>()
    private var otpKey = ""
    private var enterOtp = ""
    private var isOtpVerified = false
    private var isTimerFinished = false

    private var dialogRecoverAccountBinding: DialogRecoverAccountBinding? = null
    private var dialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater)
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

    override fun initialize() {
        setOnBackPressedListener()
        if (args.from == "Phone_fragment") {
            firebaseAuth = FirebaseAuth.getInstance()
            binding.progress.show()
            sendVerificationCode(args.phoneNo)
        } else {
            resendOtp()
        }
        disableUserTouch()
        countDown()

        binding.progressTimer.progress = 0f
        binding.progressTimer.startTimer()
        isTimerFinished = false
        setResendTextView()

    }

    override fun setObserver() {
        viewModel.otpVerificationResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    enableUserTouch()
                    data.value.data.apply {

                        if (this!!.isVerified == LoginVerifiedEnum.USER_VERIFIED.getValue() &&
                            this.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_COMPLETE.getValue()
                        ) {
                            lifecycleScope.launch {
                                Log.d("OTP RESPONSE",data.value.data.toString())
                                viewModel.saveLoggedInUser(data.value.data!!, data.value.data.token)
                                delay(10000)
                                showToast(data.value.message)
                                binding.progress.hide()
                                val bundle = Bundle()
                                bundle.putString("userType", "Login")
                                requireActivity().startNewActivity(
                                    DashBoardActivity::class.java,
                                    bundle
                                )
                            }

                        } else if (this.isVerified == LoginVerifiedEnum.USER_VERIFIED.getValue()
                            && this.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_NOT_COMPLETE.getValue()
                        ) {
                            findNavController().navigate(
                                OtpVerificationFragmentDirections.actionOtpVerificationFragmentToCreateNewProfileFragment(
                                    this._id?:"",
                                    this.token ?: "",
                                    "", ""
                                )
                            )
                        } else showToast("Invalid User")
                    }
                }

                is Resource.Loading -> binding.progress.show()

                is Resource.Failure -> {
                    binding.textViewResend.isEnabled = true
                    binding.progress.hide()
                    showToast(data.message.toString())
                    binding.otpView.resetEditText()
                    enableUserTouch()
                }

                else -> {}
            }
        }

        viewModel.resendOtpVerificationResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    enableUserTouch()
                    otpKey = "12345"
                }

                is Resource.Failure -> {
                    enableUserTouch()
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.socialLoginUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    enableUserTouch()
                    data.value.data?.let { loginResponse ->
                        if (loginResponse.isVerified == LoginVerifiedEnum.USER_VERIFIED.getValue() &&
                            loginResponse.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_COMPLETE.getValue() && loginResponse.isDeleted == LoginUserDeleteEnum.USER_NOT_DELETED.getValue()
                        ) {
                            lifecycleScope.launch {
                                viewModel.saveLoggedInUser(loginResponse, loginResponse.token)
                                delay(2000)
                                showToast(data.value.message)
                                binding.progress.hide()
                                binding.otpView.resetEditText()
                                requireActivity().runOnUiThread {
                                    val bundle = Bundle()
                                    bundle.putString("userType", "Login")
                                    requireActivity().startNewActivity(
                                        DashBoardActivity::class.java,
                                        bundle
                                    )
                                }
                            }

                        } else if (loginResponse.isVerified == LoginVerifiedEnum.USER_VERIFIED.getValue()
                            && loginResponse.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_NOT_COMPLETE.getValue() && loginResponse.isDeleted == LoginUserDeleteEnum.USER_NOT_DELETED.getValue()
                        ) {
                            binding.otpView.resetEditText()
                            findNavController().navigate(
                                OtpVerificationFragmentDirections.actionOtpVerificationFragmentToCreateNewProfileFragment(
                                    loginResponse._id?:"",
                                    loginResponse.token ?: "",
                                    loginResponse.name ?: "", args.phoneNo
                                )
                            )
                        } else if (loginResponse.isDeleted == LoginUserDeleteEnum.USER_DELETED.getValue()) {
                            binding.otpView.resetEditText()
                            showRecoverAccountDialog(loginResponse._id?:"")

                        } else showToast("Invalid User")
                    }
                }


                is Resource.Failure -> {
                    binding.textViewResend.isEnabled = true
                    binding.progress.hide()
                    showToast(data.message.toString())
                    binding.otpView.resetEditText()
                    enableUserTouch()

                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.recoverUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        data.value.data?.let { viewModel.saveLoggedInUser(it, data.value.data.token) }
                        delay(2000)
                        showToast(data.value.message)
                        binding.progress.hide()
                        binding.otpView.resetEditText()
                        requireActivity().runOnUiThread {
                            val bundle = Bundle()
                            bundle.putString("userType", "Login")
                            requireActivity().startNewActivity(
                                DashBoardActivity::class.java,
                                bundle
                            )
                        }
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
    }

    override fun setOnClickListener() {
        binding.cardViewBack.setOnClickListener(this)
        binding.otpView.otpListener = this
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewBack.id -> {
                findNavController().navigate(OtpVerificationFragmentDirections.actionOtpVerificationFragmentToPreLoginFragment())
            }
        }
    }

    override fun onInteractionListener() {

    }

    override fun onOTPComplete(otp: String) {
        if (args.from == "Phone_fragment") {
            verifyCode(otp)
        } else {
            binding.progress.show()
            val data = OTPRequest(otp, args.userId)
            viewModel.verifyOtpApi(data)
        }
        hideKeyboard(requireActivity())
    }

    private fun countDown() {
        binding.progressTimer.setCircularTimerListener(object : CircularTimerListener {
            override fun updateDataOnTick(remainingTimeInMs: Long): String {
                val seconds = ceil((remainingTimeInMs / 1000f).toDouble()).toInt()
                val formattedSeconds = seconds.toString().padStart(2, '0')
                return formattedSeconds
            }

            override fun onTimerFinished() {
                binding.otpView.resetEditText()
                binding.textViewResend.isEnabled = true
                isTimerFinished = true
                setResendTextView()
            }
        }, 60, TimeFormatEnum.SECONDS, 60)
    }

    // Method to verify the OTP
    private fun verifyOTP(enteredOTP: String) {
        isOtpVerified = otpKey != "" && otpKey == enteredOTP

    }

    private fun setResendTextView() {

        val resendText =
            SpannableString("Code didn't received? Resend")


        val resend: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                if (isTimerFinished) {
                    if (args.from == "Phone_fragment") {
                        binding.progress.show()
                        resendVerificationCode(args.phoneNo)
                    } else {
                        resendOtp()
                    }
                    disableUserTouch()
                    binding.progressTimer.progress = 0f
                    binding.progressTimer.startTimer()
                    isTimerFinished = false
                    setResendTextView()
                }
            }
        }
        resendText.let {
            if (isTimerFinished) {
                it.setSpan(resend, 22, 28, 0)
                it.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.theme_red
                        )
                    ), 22, 28, 0
                )
            } else {
                it.setSpan(resend, 22, 28, 0)
                it.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.text_gray
                        )
                    ), 22, 28, 0
                )
            }

            it.setSpan(UnderlineSpan(), 22, 28, 0)
        }
        binding.textViewResend.let {
            it.movementMethod = LinkMovementMethod.getInstance()
            it.setText(resendText, TextView.BufferType.SPANNABLE)
            it.isSelected = false
            it.setTextIsSelectable(false)
            it.highlightColor = Color.TRANSPARENT
        }
    }

    private fun resendOtp() {
        viewModel.resendVerifyOtpApi(args.userId)
        disableUserTouch()
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Automatically sign in the user
                    signInWithPhoneAuthCredential(credential)
                    binding.progress.hide()
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    binding.progress.hide()
                    Log.e("PhoneAuthentication", "Verification failed: ${exception.message}")
                    showToast("Verification failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationId, token)
                    resendToken = token
                    binding.progress.hide()
                    this@OtpVerificationFragment.verificationId = verificationId
                    showToast("OTP verification code has been sent to your phone number.")
                    enableUserTouch()
                    // Show the keyboard
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.otpView.otpEditText, InputMethodManager.SHOW_IMPLICIT)
                    // You can save the verificationId to use later if needed
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phoneNumber: String) {
        val options = resendToken?.let {
            PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, token)
                        binding.progress.hide()
                        enableUserTouch()
                        this@OtpVerificationFragment.verificationId = verificationId
                        showToast("OTP verification code has been resent to your phone number.")

                        // You can save the verificationId to use later if needed
                    }

                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Automatically sign in the user
                        signInWithPhoneAuthCredential(credential)
                        binding.progress.hide()
                    }

                    override fun onVerificationFailed(exception: FirebaseException) {
                        binding.progress.hide()
                        Log.e("PhoneAuthentication", "Verification failed: ${exception.message}")
                        showToast("Verification failed")
                    }
                })
                .setForceResendingToken(it) // Pass the previous token for resending
                .build()
        }

        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = firebaseAuth.currentUser
                    // Continue with your app logic
                    val data =
                        SocialLoginRequest(user?.uid ?: "", "phone", args.firebaseToken, "android")
                    viewModel.socialLoginUserApi(data)
                    binding.progress.show()
                    disableUserTouch()
                } else {
                    // If sign-in fails, display a message to the user.
                    Log.e("PhoneAuthentication", "signInWithCredential:failure", task.exception)
                    showToast("Authentication failed")
                }
            }
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

    private fun setOnBackPressedListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(OtpVerificationFragmentDirections.actionOtpVerificationFragmentToPreLoginFragment())
                }
            })
    }


}