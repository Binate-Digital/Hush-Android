package com.fictivestudios.hush.ui.fragments.main.registerPhoneNo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.FragmentRegisterPhoneNoBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.hideKeyboard
import com.fictivestudios.hush.utils.setSafeOnClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentRegisterPhoneNo : BaseFragment(R.layout.fragment_register_phone_no),
    View.OnClickListener {

    private var _binding: FragmentRegisterPhoneNoBinding? = null
    val binding
        get() = _binding!!
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        viewModel.getUserData()
        setOnBackPressedListener()

    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonBuy.setSafeOnClickListener {
            hideKeyboard(requireActivity())
            viewModel.registerPhoneNoApi()
            binding.progress.show()
        }
    }

    override fun setObserver() {
        viewModel.userData?.observe(viewLifecycleOwner) {
            binding.cardView.show()
            binding.textViewAssignNo.text =
                if (it.purchasedTwilioNumber.isNullOrEmpty()) {
                    binding.buttonBuy.show()

                    "No number assign yet."

                } else {
                    binding.buttonBuy.gone()
                    it.purchasedTwilioNumber
                }
        }
        viewModel.registerPhoneNoResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()

//call profile api viewModel.ge

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
}