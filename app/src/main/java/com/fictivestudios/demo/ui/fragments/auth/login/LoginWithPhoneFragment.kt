package com.fictivestudios.demo.ui.fragments.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.databinding.FragmentLoginWithPhoneBinding
import com.fictivestudios.demo.utils.hideKeyboard
import com.fictivestudios.demo.utils.setSafeOnClickListener

class LoginWithPhoneFragment : BaseFragment(R.layout.fragment_login_with_phone),
    View.OnClickListener {

    private var _binding: FragmentLoginWithPhoneBinding? = null
    val binding
        get() = _binding!!

    val viewModel: LoginViewModel by viewModels()
    private val args by navArgs<LoginWithPhoneFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginWithPhoneBinding.inflate(inflater)

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
        binding.ccp.registerCarrierNumberEditText(binding.textInputEditTextPhoneNo)
        binding.ccp.setNumberAutoFormattingEnabled(true)
//        val addLineNumberFormatter = UsPhoneNumberFormatter(
//            WeakReference<TextInputEditText>(binding.textInputEditTextPhoneNo)
//        )
//        binding.textInputEditTextPhoneNo.addTextChangedListener(addLineNumberFormatter)
    }

    override fun setObserver() {
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonContinue.setSafeOnClickListener {
            hideKeyboard(requireActivity())
            login()
            setObserver()
        }
        binding.textInputLayoutPhone.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()

            }

            binding.textInputLayoutPhone.id -> {
                binding.textInputLayoutPhone.background = null

            }
        }
    }


    private fun login() {

        if (binding.textInputEditTextPhoneNo.text.toString().trim().isEmpty()) {
            showToast("Phone Number field can't be empty")
            return
        }

        if (!binding.ccp.isValidFullNumber) {
            showToast("Please enter a valid Phone Number")
            return
        }

        findNavController().navigate(
            LoginWithPhoneFragmentDirections.actionLoginWithPhoneFragmentToOtpVerificationFragment(
                binding.ccp.fullNumberWithPlus,
                "Phone_fragment", args.firebaseToken, "", ""
            )
        )
    }

}