package com.fictivestudios.hush.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.activity.BaseActivity
import com.fictivestudios.hush.databinding.ActivityIncomingCallBinding
import com.fictivestudios.hush.pushNotification.CallManager
import com.twilio.voice.Call
import com.twilio.voice.CallException

class IncomingCallActivity : BaseActivity(), View.OnClickListener {

    private var _binding: ActivityIncomingCallBinding? = null
    private val binding get() = _binding!!

    private var activeCall: Call? = null
    private var isAudioOn = true
    private var isLoudSpeakerOn = false
    private var countDownTimer: CountDownTimer? = null
    private var elapsedTime: Long = 0

    private var callSid: String = ""
    private var fromNumber: String = ""
    private var token: String = ""

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        _binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        activeCall?.disconnect()
        _binding = null
    }

    override fun initialize() {

        // Get FCM data from intent

        callSid = intent.getStringExtra("callSid") ?: ""
        fromNumber = intent.getStringExtra("from") ?: ""
        token = intent.getStringExtra("token") ?: ""
        // Get FCM data from intent

        binding.textViewUserName.text = fromNumber
        setOnClickListener()

    }
    override fun setObserver() {}

    override fun setOnClickListener() {
        binding.cardViewMike.setOnClickListener(this)
        binding.cardViewSpeaker.setOnClickListener(this)
        binding.imageViewEndCall.setOnClickListener(this)
        binding.imageViewEndCall2.setOnClickListener(this)
        binding.imageViewAcceptCall.setOnClickListener {
          handleCall()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewMike.id -> {
                isAudioOn = !isAudioOn
                toggleMicMute(!isAudioOn)
                binding.imageViewMike.setImageResource(
                    if (isAudioOn) R.drawable.ic_microphone else R.drawable.ic_off_mic
                )
            }
            binding.cardViewSpeaker.id -> {
                isLoudSpeakerOn = !isLoudSpeakerOn
                enableLoudspeaker(isLoudSpeakerOn)
                binding.imageViewSpeaker.setImageResource(
                    if (isLoudSpeakerOn) R.drawable.ic_speaker else R.drawable.ic_off_speaker
                )
            }
            binding.imageViewEndCall.id,  binding.imageViewEndCall2.id -> {
                activeCall?.disconnect()
                finish()
            }
        }
    }

    private fun handleCall(){
        val callInvite = CallManager.currentCallInvite
        if (callInvite != null) {
            activeCall = callInvite.accept(this, object : Call.Listener {
                override fun onConnected(call: Call) {
                    activeCall = call
                    setupTimer()
                }

                override fun onReconnected(call: Call) { activeCall = call }
                override fun onReconnecting(call: Call, callException: CallException) {}
                override fun onDisconnected(call: Call, error: CallException?) {}
                override fun onConnectFailure(call: Call, error: CallException) {}
                override fun onRinging(call: Call) {}
            })
            CallManager.currentCallInvite = null
        }
        binding.imageViewAcceptCall.visibility = View.GONE
        binding.imageViewEndCall.visibility = View.GONE
        binding.imageViewEndCall2.visibility = View.VISIBLE
    }

    private fun setupTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime += 1000
                val seconds = elapsedTime / 1000
                val minutes = (seconds % 3600) / 60
                val remainingSeconds = seconds % 60
                binding.textViewTimeStamp.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }
            override fun onFinish() {}
        }
        countDownTimer?.start()
    }

    private fun enableLoudspeaker(enable: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = enable
    }

    private fun enableMicrophone(enable: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !enable
    }

    private fun toggleMicMute(mute: Boolean) {
        enableMicrophone(!mute)
    }
}
