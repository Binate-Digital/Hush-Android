package com.fictivestudios.hush.ui.fragments.auth

import android.app.AlertDialog
import android.content.Intent
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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.data.responses.RecoverUserRequest
import com.fictivestudios.hush.data.responses.SocialLoginRequest
import com.fictivestudios.hush.databinding.DialogRecoverAccountBinding
import com.fictivestudios.hush.databinding.FragmentPreLoginBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.ui.fragments.auth.login.LoginProfileVerifiedEnum
import com.fictivestudios.hush.ui.fragments.auth.login.LoginUserDeleteEnum
import com.fictivestudios.hush.ui.fragments.auth.login.LoginViewModel
import com.fictivestudios.hush.ui.fragments.auth.privacy.PrivacyTypeEnum
import com.fictivestudios.hush.utils.Constants.REQ_COE_GOOGLE_SIGN_IN
import com.fictivestudios.hush.utils.setSafeOnClickListener
import com.fictivestudios.hush.utils.startNewActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PreLoginFragment : BaseFragment(R.layout.fragment_pre_login), View.OnClickListener {

    private var _binding: FragmentPreLoginBinding? = null
    val binding
        get() = _binding!!

    val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    var firebaseToken = ""

    private var dialogRecoverAccountBinding: DialogRecoverAccountBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreLoginBinding.inflate(inflater)
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


    override fun initialize() {
        setOnBackPressedListener()
        setPrivacyTextView()

        binding.logoImageView.alpha = 0f
        binding.logoImageView.translationY = 50f
        binding.logoImageView.animate().alpha(1f).translationYBy(-50f).duration = 500
        binding.loginWithEmailView.alpha = 0f
        binding.loginWithEmailView.translationY = 50f
        binding.loginWithEmailView.animate().alpha(1f).translationYBy(-50f).duration = 1000

        FirebaseApp.initializeApp(requireContext())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_token))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseAuth = FirebaseAuth.getInstance()
        getFirebaseTokenFromFireBase()

    }

    override fun setObserver() {
        viewModel.socialLoginUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    enableUserTouch()
                    if (data.value.data?.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_COMPLETE.getValue() && data.value.data.isDeleted == LoginUserDeleteEnum.USER_NOT_DELETED.getValue()
                    ) {
                        lifecycleScope.launch {
                            viewModel.saveLoggedInUser(data.value.data, data.value.data.token)
                            delay(2000)
                            showToast(data.value.message)
                            binding.progress.hide()
                            val bundle = Bundle()
                            bundle.putString("userType", "Login")
                            requireActivity().startNewActivity(
                                DashBoardActivity::class.java,
                                bundle
                            )
                        }

                    } else if (data.value.data?.isDeleted == LoginUserDeleteEnum.USER_NOT_DELETED.getValue() && data.value.data.isProfileCompleted == LoginProfileVerifiedEnum.USER_PROFILE_NOT_COMPLETE.getValue()) {
                        binding.progress.hide()
                        showToast(data.value.message)
                        findNavController().navigate(
                            PreLoginFragmentDirections.actionPreLoginFragmentToCreateNewProfileFragment(
                                data.value.data._id?:"",
                                data.value.data.token?:"", data.value.data.name?:"", ""
                            )
                        )
                    } else if (data.value.data?.isDeleted == LoginUserDeleteEnum.USER_DELETED.getValue()) {
                        binding.progress.hide()

                        requireActivity().runOnUiThread {
                            showToast(data.value.message)
                            showRecoverAccountDialog(data.value.data._id?:"")
                        }
                    } else showToast("Invalid User")
                }


                is Resource.Failure -> {
                    binding.progress.hide()
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
                    data.value.data?.let {
                        binding.progress.hide()
                        showToast(data.value.message)
                        enableUserTouch()
                        signInGoogle()
                    }
                }

                is Resource.Failure -> {
                    binding.progress.hide()
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
        binding.loginWithEmailView.setOnClickListener(this)
        binding.loginWithPhoneView.setOnClickListener(this)
        binding.loginWithGoogleView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.loginWithEmailView.id -> {
                findNavController().navigate(PreLoginFragmentDirections.actionPreLoginFragmentToLoginWithEmailFragment())

            }

            binding.loginWithPhoneView.id -> {
                findNavController().navigate(
                    PreLoginFragmentDirections.actionPreLoginFragmentToLoginWithPhoneFragment(
                        firebaseToken
                    )
                )

            }

            binding.loginWithGoogleView.id -> {
                signInGoogle()
            }


        }
    }

    private fun setPrivacyTextView() {
        val privacyText =
            SpannableString("By signing up, you agree to our\n Terms & Conditions and Privacy Policy")


        val termsAndCondition: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                findNavController().navigate(
                    PreLoginFragmentDirections.actionPreLoginFragmentToPrivacyPolicyFragment(
                        PrivacyTypeEnum.TERMS_AND_CONDITION.getValue()
                    )
                )
            }
        }
        val privacy: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                findNavController().navigate(
                    PreLoginFragmentDirections.actionPreLoginFragmentToPrivacyPolicyFragment(
                        PrivacyTypeEnum.PRIVACY.getValue()
                    )
                )
            }
        }
        privacyText.let {
            it.setSpan(termsAndCondition, 33, 51, 0)
            it.setSpan(privacy, 56, 70, 0)
            it.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.theme_red
                    )
                ), 33, 51, 0
            )
            it.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.theme_red
                    )
                ), 56, 70, 0
            )
            it.setSpan(UnderlineSpan(), 33, 51, 0)
            it.setSpan(UnderlineSpan(), 56, 70, 0)
        }
        binding.textViewTerms.let {
            it.movementMethod = LinkMovementMethod.getInstance()
            it.setText(privacyText, TextView.BufferType.SPANNABLE)
            it.isSelected = false
            it.setTextIsSelectable(false)
            it.highlightColor = Color.TRANSPARENT
        }
    }

    private fun signInGoogle() {

        val signInIntent: Intent = googleSignInClient.signInIntent
        @Suppress("DEPRECATION")
        startActivityForResult(signInIntent, REQ_COE_GOOGLE_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_COE_GOOGLE_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun getFirebaseTokenFromFireBase() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    "firebase_token_failed",
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("firebase_token", token!!)
            firebaseToken = token

        })
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                if (account.idToken != null || account.idToken != "") {
                    val name = account.displayName
                    val data =
                        SocialLoginRequest(
                            account.idToken!!,
                            "google",
                            firebaseToken,
                            "android",
                            name = name
                        )
                    viewModel.socialLoginUserApi(data)
                    binding.progress.show()
                    disableUserTouch()
                    setObserver()
                }

            }
        } catch (e: ApiException) {
            e.localizedMessage?.let { showToast(it.toString()) }
            Log.d("ApiException", e.message.toString())
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
                }
            })
    }

}