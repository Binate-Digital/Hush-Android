package com.fictivestudios.demo.ui.fragments.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.preference.DataPreference
import com.fictivestudios.demo.base.preference.DataPreference.Companion.IS_LOGIN
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.VerifyCodeRequest
import com.fictivestudios.demo.databinding.FragmentSplashBinding
import com.fictivestudios.demo.databinding.PasswordDialogBinding
import com.fictivestudios.demo.databinding.PatternDialogBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.activities.MainViewModel
import com.fictivestudios.demo.utils.PatternLockView
import com.fictivestudios.demo.utils.compressAndSaveImage
import com.fictivestudios.demo.utils.gallery.CameraPermissionTextProvider
import com.fictivestudios.demo.utils.gallery.showPermissionDialog
import com.fictivestudios.demo.utils.hideKeyboard
import com.fictivestudios.demo.utils.startNewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor


class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private var _binding: FragmentSplashBinding? = null
    val binding
        get() = _binding!!

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var cameraProvider: ProcessCameraProvider? = null

    private var dialog: AlertDialog? = null
    private var dialogBinding: PasswordDialogBinding? = null
    private var dialogPatternBinding: PatternDialogBinding? = null
    private var imageCapture: ImageCapture? = null
    private var pinAttempt = 0
    val viewModel: MainViewModel by viewModels()

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
                takePhoto()
            } else {
                showPermissionDialog(
                    permissionTextProvider = CameraPermissionTextProvider(),
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                    ),
                    onDismiss = {},
                    onOkClick = {
                        openAppSettings()


                    },
                    onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogBinding = null
        dialogPatternBinding = null
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
        stopCamera()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }


    override fun initialize() {
        getSession()
    }


    override fun setOnClickListener() {}

    override fun setObserver() {

        viewModel.uploadScreenShotResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                }

                is Resource.Failure -> {
                    if (it.errorCode == 401) {
                        stopCamera()
                        showToast(it.message.toString())
                        viewModel.preferenceLogout()
                        requireActivity().recreate()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.verifyPinCodeResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    hideKeyboard(requireActivity())
                    showToast(it.value.message)
                    dialogBinding!!.progress.hide()
                    dialog?.dismiss()
                    stopCamera()
                    lifecycleScope.launch {

                        delay(500)
                        val bundle = Bundle()
                        bundle.putString("userType", "Login")
                        requireActivity().startNewActivity(
                            DashBoardActivity::class.java,
                            bundle
                        )
                        requireActivity().finish()
                    }

                }

                is Resource.Failure -> {
                    showToast(it.message.toString())
                    dialogBinding!!.progress.hide()
                    dialogBinding!!.textInputEditTextEnterPin.text!!.clear()

                    if (it.errorCode == 401) {
                        viewModel.preferenceLogout()
                        requireActivity().recreate()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }


        viewModel.patternResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    dialogPatternBinding!!.progress.hide()
                    showToast(it.value.message)
                    dialog?.dismiss()
                    stopCamera()
                    lifecycleScope.launch {
                        delay(500)
                        val bundle = Bundle()
                        bundle.putString("userType", "Login")
                        requireActivity().startNewActivity(
                            DashBoardActivity::class.java,
                            bundle
                        )
                    }
                }

                is Resource.Failure -> {
                    pinAttempt++
                    if (pinAttempt == 3) {
                        if (!checkCameraPermission()) {
                            requestCameraPermission()
                        } else {
                            takePhoto()
                        }

                        pinAttempt = 0
                    }
                    dialogPatternBinding!!.progress.hide()
                    dialogPatternBinding!!.patternLockView.reset()
                    dialogPatternBinding!!.patternLockView.isTouched = true
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        stopCamera()
                        viewModel.preferenceLogout()
                        requireActivity().recreate()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getSession() {

        lifecycleScope.launch {
            delay(500)
            if (DataPreference(requireContext()).getBooleanData(IS_LOGIN)) {
                requireActivity().runOnUiThread {
                    startCamera()
                }

                if (viewModel.userData?.pinVerified == 1 && viewModel.userData?.pinLock == 1) {
                    requireActivity().runOnUiThread {
                        showMessageBox()
                    }

                } else if (viewModel.userData?.fingerprintLock == 1) {
                    requireActivity().runOnUiThread {
                        showFingerPrintDialog()
                    }

                } else if (viewModel.userData?.patternLock == 1) {
                    requireActivity().runOnUiThread {
                        showPatternDialogBox()
                    }
                } else {
                    delay(2000)
                    val bundle = Bundle()
                    bundle.putString("userType", "Login")
                    requireActivity().startNewActivity(
                        DashBoardActivity::class.java,
                        bundle
                    )
                }
            } else {
                stopCamera()
                delay(2000)
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToPreLoginFragment())
            }
        }
    }

    private fun showFingerPrintDialog() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(requireActivity(), executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    pinAttempt++
                    Log.d("errorPrint", pinAttempt.toString())
                    if (pinAttempt == 3) {
                        if (!checkCameraPermission()) {
                            requestCameraPermission()
                        } else {
                            takePhoto()
                        }

                        pinAttempt = 0
                    }
                    Toast.makeText(
                        requireContext(),
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val bundle = Bundle()
                    bundle.putString("userType", "Login")
                    requireActivity().startNewActivity(
                        DashBoardActivity::class.java,
                        bundle
                    )
                    Toast.makeText(
                        requireContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    pinAttempt++
                    if (pinAttempt == 3) {
                        if (!checkCameraPermission()) {
                            requestCameraPermission()
                        } else {
                            takePhoto()
                        }

                        pinAttempt = 0
                    }
                    Log.d("failedPrint", pinAttempt.toString())
                    Toast.makeText(
                        requireContext(), "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })


        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Hush")
            .setNegativeButtonText("Cancel")
            .build()


        biometricPrompt.authenticate(promptInfo)
    }

    private fun showMessageBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogBinding =
            inflate(LayoutInflater.from(requireActivity()), R.layout.password_dialog, null, false)
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogBinding!!.root)
            dialog.setCancelable(false)

            dialogBinding!!.doneButton.setOnClickListener {
                // hideKeyboard(requireActivity())
                val im: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(dialogBinding!!.textInputEditTextEnterPin.windowToken, 0)
                pinAttempt++
                if (pinAttempt == 3) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission()
                    } else {
                        takePhoto()
                        dialogBinding!!.textInputEditTextEnterPin.text?.clear()
                    }

                    pinAttempt = 0
                } else {
                    verifyPinCode()
                }
            }
            dialog.show()
        }
    }

    private fun showPatternDialogBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogPatternBinding =
            inflate(LayoutInflater.from(requireActivity()), R.layout.pattern_dialog, null, false)
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogPatternBinding!!.root)
            dialog.setCancelable(false)

            dialogPatternBinding!!.patternLockView.setOnPatternListener(object :
                PatternLockView.OnPatternListener {
                override fun onStarted() {
                    super.onStarted()
                }

                override fun onProgress(ids: ArrayList<Int>) {
                    super.onProgress(ids)
                }

                override fun onComplete(ids: ArrayList<Int>): Boolean {
                    var idList = arrayListOf<String>()
                    for (id in ids) {
                        idList.add(id.toString())
                    }
                    viewModel.verifyPattern(idList)
                    dialogPatternBinding!!.progress.show()
                    return true
                }
            })
            dialog.show()
        }
    }


    private fun verifyPinCode() {
        if (dialogBinding!!.textInputEditTextEnterPin.text!!.toString().trim().isEmpty()) {
            showToast("Pin code must not be empty.")
            return
        }
        if (dialogBinding!!.textInputEditTextEnterPin.text!!.toString()
                .trim().length < 4
        ) {
            showToast("Pin code should be 4 to 16 digits")
            return
        }
        viewModel.verifyPinCode(
            VerifyCodeRequest(
                dialogBinding!!.textInputEditTextEnterPin.text!!.toString().trim()
            )
        )
        dialogBinding!!.progress.show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider) {
        this.cameraProvider = cameraProvider // Store the camera provider

        val preview = Preview.Builder().build()
        imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        this.cameraProvider!!.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            imageCapture,
            preview
        )
    }

    private fun takePhoto() {
        val fileName = "${System.currentTimeMillis()}.jpg"
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val photoFile = File(downloadsDirectory, fileName)

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri =
                        compressAndSaveImage(requireContext(), outputFileResults.savedUri!!, 15)
                    sentScreenShotData(uri)

                }

                override fun onError(exception: ImageCaptureException) {
                    showToast(exception.message.toString())
                }
            }
        )
    }

    private fun sentScreenShotData(imageUri: Uri) {
        val file = createFileFromUri(imageUri)

        val requestFile = file?.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = (if (file == null) null else requestFile!!)?.let {
            MultipartBody.Part.createFormData(
                "screenshot", file.toString(),
                it
            )
        }

        viewModel.uploadScreenShot(imagePart)
    }


    private fun createFileFromUri(uri: Uri): File? {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri)

        inputStream?.let {
            try {
                // Create a temporary file in your app's cache directory
                val tempFile =
                    File.createTempFile("profile_image_", ".jpg", requireContext().cacheDir)
                tempFile.outputStream().use { output ->
                    it.copyTo(output)
                }
                return tempFile
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return null
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun stopCamera() {
        // Unbind the camera provider and release the camera resources
        cameraProvider?.shutdown()
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null

    }

    private fun checkCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }


    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = CameraPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
                ),
                onDismiss = {},
                onOkClick = {
                    openAppSettings()
                },
                onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
            )
        } else {
            // Request camera permission using the launcher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
        )
        startActivityForResult(intent, 120)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 120) {
            // Check if the user granted the camera permission after going to app settings
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
                takePhoto()
            } else {
                showToast("Permission denied")
            }
        }
    }

}