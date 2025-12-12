package com.fictivestudios.hush.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.activity.BaseActivity
import com.fictivestudios.hush.databinding.ActivityCallBinding
import com.fictivestudios.hush.utils.gallery.RecordAudioPermissionTextProvider
import com.fictivestudios.hush.utils.gallery.showPermissionDialog
import com.fictivestudios.hush.utils.openAppSettings
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice


class CallActivity : BaseActivity(), View.OnClickListener {

    private var _binding: ActivityCallBinding? = null
    val binding
        get() = _binding!!

    private var isAudioOn = true
    private var isLoudSpeakerOn = false
    private var countDownTimer: CountDownTimer? = null

    private val viewModel: CallViewModel by viewModels()
    private var activeCall: Call? = null
    private var userName = ""
    private var userPhoneNo = ""
    private var token = ""
    private var elapsedTime: Long = 0

    private val requestCallPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (userPhoneNo != "") {
                    makeTwilioVoiceCall(userPhoneNo)
                } else {
                    showToast("Invalid phone number.")
                    finish()
                }
            } else {
                showPermissionDialog(
                    permissionTextProvider = RecordAudioPermissionTextProvider(),
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    onDismiss = {},
                    onOkClick = {
                        openAppSettings()


                    },
                    onGoToAppSettingsClick = ::openAppSettings, context = this
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        _binding = ActivityCallBinding.inflate(layoutInflater)

        userPhoneNo = intent.getStringExtra("phone_no") ?: ""
        token = intent.getStringExtra("token") ?: ""
        userName = intent.getStringExtra("user_name") ?: ""

        binding.textViewUserName.text = userName
        setContentView(binding.root)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        _binding = null
    }


    override fun initialize() {

        if (!checkAudioCallPermission()) {
            requestCallPermission()
        } else {
            if (userPhoneNo != "") {
                makeTwilioVoiceCall(userPhoneNo)
            } else {
                showToast("Invalid phone number.")
                finish()
            }

        }
    }

    private fun requestCallPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = RecordAudioPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    Manifest.permission.RECORD_AUDIO
                ),
                onDismiss = {},
                onOkClick = {
                    openAppSettings()
                },
                onGoToAppSettingsClick = ::openAppSettings, context = this
            )
        } else {
            // Request call audio permission using the launcher
            requestCallPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun setObserver() {

    }

    override fun setOnClickListener() {
        binding.cardViewMike.setOnClickListener(this)
        binding.cardViewSpeaker.setOnClickListener(this)
        binding.imageViewEndCall.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewMike.id -> {
                isAudioOn = if (isAudioOn) {
                    binding.imageViewMike.setImageResource(R.drawable.ic_microphone)
                    toggleMicMute(false)
                    false
                } else {
                    toggleMicMute(true)
                    binding.imageViewMike.setImageResource(R.drawable.ic_off_mic)
                    true
                }
            }

            binding.cardViewSpeaker.id -> {
                isLoudSpeakerOn = if (!isLoudSpeakerOn) {
                    enableLoudspeaker(true)
                    binding.imageViewSpeaker.setImageResource(R.drawable.ic_speaker)
                    true
                } else {
                    enableLoudspeaker(false)
                    binding.imageViewSpeaker.setImageResource(R.drawable.ic_off_speaker)
                    false
                }
            }

            binding.imageViewEndCall.id -> {
                activeCall?.disconnect()
                finish()
            }
        }

    }

    private fun checkAudioCallPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun enableLoudspeaker(enable: Boolean) {
        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = enable
    }

    // Function to enable/disable microphone
    private fun enableMicrophone(enable: Boolean) {
        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !enable
    }

    private fun makeTwilioVoiceCall(phoneNumber: String) {
        if (token.isEmpty()) {
            showToast("Invalid Token")
            this.finish()
        }

        viewModel.init{ phone,tokens->
            phone?.let {
                val params = hashMapOf<String, String>()
                val from = it
                Log.d("from",from)
                Log.d("from", from)
                params["from"] = from
                params["to"] = phoneNumber
                Voice.enableInsights(true)
                val connectOptions = ConnectOptions.Builder(token)
                    .params(params)
                    .build()
                activeCall = Voice.connect(this, connectOptions, OutgoingCallListener())
            }?: showToast("phone no null")

        }


    }

    private inner class OutgoingCallListener : Call.Listener {
        override fun onConnectFailure(call: Call, callException: CallException) {
            Log.d("TAG", "onConnectFailure: connect fail $callException")
            showToast("Call Failed $callException")
            finish()
        }

        override fun onRinging(call: Call) {
            Log.d("TAG", "onRinging: ringing")
            binding.textViewTimeStamp.text = "Ringing"
        }

        override fun onConnected(call: Call) {
            Log.d("TAG", "onConnected: .connected")
            activeCall = call
            setupTimer()
            enableLoudspeaker(false)
            enableMicrophone(true)
        }

        override fun onReconnecting(call: Call, callException: CallException) {
            Log.d("onReconnecting", "onReconnecting: reconnecting")
            Log.d("onReconnecting", "onReconnecting: ${callException.message}")
            Log.d("onReconnecting", "onReconnecting: ${callException.cause}")
            binding.textViewTimeStamp.text = "Reconnecting"
        }

        override fun onReconnected(call: Call) {
            activeCall = call
            updateUI(elapsedTime)
            Log.d("TAG", "onReconnected: reconnected")
        }

        override fun onDisconnected(call: Call, callException: CallException?) {
            Log.d("TAG", "onDisconnected: ondisconnected $callException")
            activeCall = call
            finish()
        }
    }

    private fun setupTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            // Long.MAX_VALUE means it will count down from the maximum possible value
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                updateUI(elapsedTime)
            }

            override fun onFinish() {
                // This method is not used since the timer is infinite
            }
        }

        countDownTimer?.start()
    }

    fun toggleMicMute(mute: Boolean) {
        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Check if the app has MODIFY_AUDIO_SETTINGS permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.MODIFY_AUDIO_SETTINGS),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        // Proceed with toggling microphone mute
        audioManager.isMicrophoneMute = mute
    }

    private fun updateUI(elapsedTime: Long) {
        val seconds = elapsedTime / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        val formattedTime = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else if (minutes > 0) {
            String.format("%d:%02d", minutes, remainingSeconds)
        } else {
            String.format("0:%02d", remainingSeconds)
        }

        binding.textViewTimeStamp.text = formattedTime
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
    }


}