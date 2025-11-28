package com.fictivestudios.demo.ui.fragments.main.dialPad

import android.content.Intent
import android.media.AudioManager.STREAM_VOICE_CALL
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.databinding.FragmentDialPadBinding
import com.fictivestudios.demo.ui.activities.CallActivity
import com.fictivestudios.demo.utils.setSafeOnClickListener

class DialPadFragment : BaseFragment(R.layout.fragment_dial_pad), View.OnClickListener {

    private var _binding: FragmentDialPadBinding? = null
    val binding
        get() = _binding!!
    val viewModel: DialPadViewModel by viewModels()
    private lateinit var toneGenerator: ToneGenerator
    private var phoneNumber = ""
    private var digitButtons = ArrayList<AppCompatButton>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialPadBinding.inflate(inflater)
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
        toneGenerator = ToneGenerator(STREAM_VOICE_CALL, 100)
        binding.ccp.registerCarrierNumberEditText(binding.textViewDial)
        binding.ccp.setDefaultCountryUsingNameCode("US")
        addDigitButton()
    }

    override fun setObserver() {}

    override fun setOnClickListener() {
        binding.cardViewDigitBack.setOnClickListener(this)

        binding.cardViewCall.setSafeOnClickListener {
            if (viewModel.userData?.phoneSid.isNullOrEmpty()) {
                showToast("Please register phone number first")
                findNavController().navigate(R.id.registerPhoneNo)
                return@setSafeOnClickListener
            }
            if (binding.textViewDial.text.toString().isEmpty()) {
                showToast("Please enter phone number first")
                return@setSafeOnClickListener
            }
            val phoneNo = binding.ccp.fullNumberWithPlus
            Log.d("phoneNo", phoneNo)
            val intent = Intent(requireActivity(), CallActivity::class.java)
            intent.putExtra(
                "phone_no",
                phoneNo
            )
            intent.putExtra("user_name", viewModel.userData?.name)
            startActivity(intent)
        }
        binding.cardViewDigitBack.setOnLongClickListener {
            clearPhoneNumber()
            true
        }
        digitButtons.forEach { button ->
            button.setOnClickListener {
                onDigitButtonClick(button.text.toString())
                playDialSound()
            }
        }
    }

    private fun addDigitButton() {
        digitButtons.add(binding.buttonDigit0)
        digitButtons.add(binding.buttonDigit1)
        digitButtons.add(binding.buttonDigit2)
        digitButtons.add(binding.buttonDigit3)
        digitButtons.add(binding.buttonDigit4)
        digitButtons.add(binding.buttonDigit5)
        digitButtons.add(binding.buttonDigit6)
        digitButtons.add(binding.buttonDigit7)
        digitButtons.add(binding.buttonDigit8)
        digitButtons.add(binding.buttonDigit9)
        digitButtons.add(binding.buttonDigitStar)
        digitButtons.add(binding.buttonDigitHash)
    }

    private fun onDigitButtonClick(digit: String) {
        when (digit) {
            "C" -> clearPhoneNumber()
            else -> appendDigit(digit)
        }
    }

    private fun appendDigit(digit: String) {
        phoneNumber += digit
        updatePhoneNumberDisplay()
    }

    private fun backspace() {
        if (phoneNumber.isNotEmpty()) {
            phoneNumber = phoneNumber.substring(0, phoneNumber.length - 1)
            updatePhoneNumberDisplay()
        }
    }

    private fun clearPhoneNumber() {
        phoneNumber = ""
        updatePhoneNumberDisplay()
    }

    private fun updatePhoneNumberDisplay() {
        binding.textViewDial.setText(phoneNumber)
    }

    private fun playDialSound() {
        // Play dial tone
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 150) // Adjust tone duration as needed
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewDigitBack.id -> {
                backspace()
            }
        }
    }
}
