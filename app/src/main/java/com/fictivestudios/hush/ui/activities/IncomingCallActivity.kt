package com.fictivestudios.hush.ui.activities

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.activity.BaseActivity
import com.fictivestudios.hush.databinding.ActivityCallBinding
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import org.json.JSONObject

class IncomingCallActivity : BaseActivity(), View.OnClickListener {

    private var _binding: ActivityCallBinding? = null
    private val binding get() = _binding!!

    private var activeCall: Call? = null
    private var isAudioOn = true
    private var isLoudSpeakerOn = false
    private var countDownTimer: CountDownTimer? = null
    private var elapsedTime: Long = 0

    private val viewModel: CallViewModel by viewModels()

    private var callSid: String = ""
    private var fromNumber: String = ""
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        _binding = ActivityCallBinding.inflate(layoutInflater)
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

        // Get FCM data from intent
Log.d("init called","called")

        callSid = intent.getStringExtra("callSid") ?: ""
        fromNumber = intent.getStringExtra("from") ?: ""
        token = intent.getStringExtra("token") ?: ""
        // Get FCM data from intent
        Log.d("init token","$token")

        binding.textViewUserName.text = fromNumber
        binding.textViewTimeStamp.text = "Incoming Call"
        binding.imageViewAccept.visibility = View.VISIBLE
        setOnClickListener()

    }
    override fun setObserver() {}

    override fun setOnClickListener() {
        binding.cardViewMike.setOnClickListener(this)
        binding.cardViewSpeaker.setOnClickListener(this)
        binding.imageViewEndCall.setOnClickListener(this)
        binding.imageViewAccept.setOnClickListener {
            connectTwilioCall()
            binding.imageViewAccept.visibility = View.GONE
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
            binding.imageViewEndCall.id -> {
                activeCall?.disconnect()
                finish()
            }
        }
    }

    private fun connectTwilioCall() {


        viewModel.init{ phone,tokens->
           if(phone.isNullOrEmpty() || token.isNullOrEmpty()){
               Log.d("income data null issue","$phone $token")
           }else{
               val params = mapOf("callSid" to callSid)

               val connectOptions = ConnectOptions.Builder(token.trim())
                   .params(params)
                   .build()



               activeCall = Voice.connect(this, connectOptions, object : Call.Listener {
                   override fun onConnected(call: Call) {
                       activeCall = call
                       setupTimer()
                       enableMicrophone(true)
                       enableLoudspeaker(false)
                   }

                   override fun onDisconnected(call: Call, error: CallException?) {
                       activeCall = null
                       finish()
                   }

                   override fun onConnectFailure(call: Call, error: CallException) {
                       showToast("Call failed: ${error.message}")
                       Log.d("income token",error.toString())
                       activeCall?.disconnect()
                       finish()
                   }

                   override fun onRinging(call: Call) {}
                   override fun onReconnecting(call: Call, error: CallException) {}
                   override fun onReconnected(call: Call) {}
               })
           }

        }


    }

    private fun setupTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
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
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = enable
    }

    private fun enableMicrophone(enable: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isMicrophoneMute = !enable
    }

    private fun toggleMicMute(mute: Boolean) {
        enableMicrophone(!mute)
    }
}
