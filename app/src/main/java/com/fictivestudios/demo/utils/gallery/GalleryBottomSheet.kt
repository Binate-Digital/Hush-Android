package com.fictivestudios.demo.utils.gallery

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.fictivestudios.demo.R
import com.fictivestudios.demo.databinding.GalleryDialogBinding
import com.fictivestudios.demo.ui.fragments.crop.CropFragment
import com.fictivestudios.demo.utils.AttachmentTypeEnum
import com.fictivestudios.demo.utils.Constants.REQUEST_IMAGE_FROM_CAMERA_CODE
import com.fictivestudios.demo.utils.createFileFromUri
import com.fictivestudios.demo.utils.gone
import com.fictivestudios.demo.utils.openAppSettings
import com.fictivestudios.demo.utils.show
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File


@Suppress("DEPRECATION")
class GalleryBottomSheet(
    private val type: String,
    private val listener: OnImageSelect,

    ) :
    BottomSheetDialogFragment(), View.OnClickListener, CropFragment.ImageCrop {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var binding: GalleryDialogBinding
    private lateinit var imageUri: Uri


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                if (imageUri != null) {
                    openCropDialogFragment(imageUri)
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                showPermissionDialog(
                    permissionTextProvider = ReadImagePermissionTextProvider(),
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

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MyBottSheetDialog)

    }

    override fun onStart() {
        super.onStart()
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)

        //inflating layout
        val view = View.inflate(context, R.layout.gallery_dialog, null)
        //binding views to data binding.
        binding = DataBindingUtil
            .bind<GalleryDialogBinding>(view) as GalleryDialogBinding
        //setting layout with bottom sheet
        bottomSheet.setContentView(view)


        //  (view.parent as View).setBackgroundResource(R.drawable.top_round_corner_bg)

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        //setting Peek at the 16:9 ratio key line of its parent.
        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO

        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss()
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })
        if (type == AttachmentTypeEnum.VIDEO.getValue()) {
            binding.textViewCameraVideo.show()
            binding.textViewCamera.gone()
        } else {
            binding.textViewCameraVideo.gone()
            binding.textViewCamera.show()
        }
        setOnClickListener()
        return bottomSheet
    }

    private fun setOnClickListener() {
        binding.btnCancelDialog.setOnClickListener(this)
        binding.textViewGallery.setOnClickListener(this)
        binding.textViewCamera.setOnClickListener(this)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnCancelDialog.id -> {
                dismissNow()
            }

            binding.textViewGallery.id -> {
                if (checkPermission())
                    openImagePicker()
                else
                    requestPermission()
            }

            binding.textViewCamera.id -> {
                if (checkCameraPermission()) {
                    openCamera()
                } else {
                    requestCameraPermission()
                }
            }
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_FROM_CAMERA_CODE -> {
                    try {
                        openCropDialogFragment(imageUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getImageCropped(cropImage: Uri?) {
        val file = cropImage?.let { createFileFromUri(requireContext(), it) }
        listener.onSelectImageFromSystem(cropImage, file)
        dismissNow()
    }

    private fun openCamera() {
        val file = File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM),
            Environment.DIRECTORY_PICTURES
        )
        imageUri = FileProvider.getUriForFile(
            requireActivity(),
            "com.fictivestudios.demo.fileprovider",
            file
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Grant permission to the camera app
        val cameraActivities: List<ResolveInfo> =
            requireActivity().packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        for (info in cameraActivities) {
            requireActivity().grantUriPermission(
                info.activityInfo.packageName,
                imageUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        startActivityForResult(intent, REQUEST_IMAGE_FROM_CAMERA_CODE)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun checkPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )

        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        val currentVersion = Build.VERSION.SDK_INT
        if (currentVersion >= Build.VERSION_CODES.Q && currentVersion < Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDialog(
                    permissionTextProvider = ReadImagePermissionTextProvider(),
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    onDismiss = {},
                    onOkClick = {
                        openAppSettings()

                    },
                    onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
                )
            } else {
                // Request camera permission using the launcher
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            ) {
                showPermissionDialog(
                    permissionTextProvider = ReadImagePermissionTextProvider(),
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_MEDIA_IMAGES
                    ),
                    onDismiss = {},
                    onOkClick = {
                        openAppSettings()
                    },
                    onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
                )
            } else {
                // Request camera permission using the launcher
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }


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
                    dismissNow()

                },
                onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
            )
        } else {
            // Request camera permission using the launcher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    private fun openCropDialogFragment(imageUri: Uri) {
        val mapDialogFragment = CropFragment(imageUri, this)
        mapDialogFragment.show(childFragmentManager, "MapDialogFragment")
    }

}

interface OnImageSelect {
    fun onSelectImageFromSystem(
        imageUri: Uri? = null,
        imageLink: File? = null
    )
}
