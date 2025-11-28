package com.appsnado.demo.utils.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appsnado.demo.R
import com.appsnado.demo.databinding.GalleryImageVideoDialogBinding
import com.appsnado.demo.ui.fragments.crop.CropFragment
import com.appsnado.demo.utils.Constants.REQUEST_IMAGE_FROM_CAMERA_CODE
import com.appsnado.demo.utils.VideoCompressor
import com.appsnado.demo.utils.createFileFromUri
import com.appsnado.demo.utils.createVideoFileFromUri
import com.appsnado.demo.utils.getPathFromUri
import com.appsnado.demo.utils.openAppSettings
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File


@Suppress("DEPRECATION")
class ImageVideoBottomSheet(private val listener: OnItemSelect) :
    BottomSheetDialogFragment(), View.OnClickListener, CropFragment.ImageCrop {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var binding: GalleryImageVideoDialogBinding
    private lateinit var imageUri: Uri

    private var imageFile = ArrayList<File>()
    private var videoFile = ArrayList<File>()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val selectedVideoUris = ArrayList<Uri>()
                if (data?.clipData != null) {
                    val clipData = data.clipData
                    for (i in 0 until clipData!!.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        imageFile.add(createFileFromUri(requireContext(), uri)!!)
                    }

                } else if (data?.data != null) {
                    val videoUri = data.data
                    val contentType = videoUri?.let {
                        requireContext().contentResolver.getType(
                            it
                        )
                    }
                    if (contentType?.startsWith("image/") == true) {
                        val file = createFileFromUri(requireContext(), videoUri)
                        imageFile.add(file!!)
                        selectedVideoUris.add(videoUri)
                        listener.onSelectImageFromSystem(imageFile)
                    }
                }
                listener.onSelectImageFromSystem(imageFile)
            }
        }

    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val clipData = data.clipData
                    for (i in 0 until clipData!!.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val contentType = requireContext().contentResolver.getType(uri)
                        if (contentType?.startsWith("video/") == true) {
                            val file = createVideoFileFromUri(requireContext(), uri)
                            Log.d("videoPath", file?.path ?: "")
                            videoFile.add(
                                VideoCompressor().compressVideo(
                                    getPathFromUri(requireContext(), uri) ?: "",
                                    getPathFromUri(requireContext(), uri) ?: ""
                                )
                            )
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "The selected item is not a video",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                listener.onSelectVideoFromSystem(videoFile)
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
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_IMAGES
                    ),
                    onDismiss = {},
                    onOkClick = {
                        openAppSettings()


                    },
                    onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
                )
            }
        }

    private val requestVideoPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openVideoPicker()
            } else {
                showPermissionDialog(
                    permissionTextProvider = VideoPermissionTextProvider(),
                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_VIDEO
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
        val view = View.inflate(context, R.layout.gallery_image_video_dialog, null)
        //binding views to data binding.
        binding = DataBindingUtil
            .bind<GalleryImageVideoDialogBinding>(view) as GalleryImageVideoDialogBinding
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
        setOnClickListener()
        return bottomSheet
    }

    private fun setOnClickListener() {
        binding.btnCancelDialog.setOnClickListener(this)
        binding.textViewVideo.setOnClickListener(this)
        binding.textViewImage.setOnClickListener(this)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnCancelDialog.id -> {
                dismissNow()
            }

            binding.textViewImage.id -> {
                if (checkPermission())
                    openImagePicker()
                else
                    requestImagePermission()
            }

            binding.textViewVideo.id -> {
                if (checkVideoPermission()) {
                    openVideoPicker()
                } else {
                    requestVideoPermission()
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
        if (file != null) {
            imageFile.add(file)
        }
        dismissNow()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        pickImageLauncher.launch(intent)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickVideoLauncher.launch(intent)
    }

    private fun checkPermission(): Boolean {

        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_IMAGES
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkVideoPermission(): Boolean {

        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_VIDEO
        )
        return permission == PackageManager.PERMISSION_GRANTED

    }

    private fun requestImagePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_IMAGES
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = ReadImagePermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_IMAGES
                ),
                onDismiss = {},
                onOkClick = {
                    openAppSettings()

                },
                onGoToAppSettingsClick = ::openAppSettings, context = requireContext()
            )
        } else {
            // Request camera permission using the launcher
            requestPermissionLauncher.launch(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_IMAGES)
        }
    }


    private fun requestVideoPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_VIDEO
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = CameraPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_VIDEO
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
            requestVideoPermissionLauncher.launch(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) READ_EXTERNAL_STORAGE else READ_MEDIA_VIDEO)
        }
    }


    private fun openCropDialogFragment(imageUri: Uri) {
        val mapDialogFragment = CropFragment(imageUri, this)
        mapDialogFragment.show(childFragmentManager, "MapDialogFragment")
    }

    fun getDownloadFolderPath(): String? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}


interface OnItemSelect {
    fun onSelectImageFromSystem(
        imageLinkList: ArrayList<File>? = null,
    )

    fun onSelectVideoFromSystem(
        videoFileList: ArrayList<File>? = null,
    )


}