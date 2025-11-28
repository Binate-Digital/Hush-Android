package com.fictivestudios.demo.utils.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.GenericListAdapter
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.Compress
import com.fictivestudios.demo.databinding.DialogCompressBinding
import com.fictivestudios.demo.databinding.GalleryImageVideoDialogBinding
import com.fictivestudios.demo.ui.fragments.crop.CropFragment
import com.fictivestudios.demo.utils.Constants.REQUEST_IMAGE_FROM_CAMERA_CODE
import com.fictivestudios.demo.utils.compressAndSaveImage
import com.fictivestudios.demo.utils.createFileFromUri
import com.fictivestudios.demo.utils.createVideoFileFromUri
import com.fictivestudios.demo.utils.gallery.itemView.RowItemCompress
import com.fictivestudios.demo.utils.openAppSettings
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


@Suppress("DEPRECATION")
class ImageVideoBottomSheet(private val listener: OnItemSelect) :
    BottomSheetDialogFragment(), View.OnClickListener, CropFragment.ImageCrop {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var binding: GalleryImageVideoDialogBinding
    private lateinit var imageUri: Uri
    private var imageFile = ArrayList<File>()
    private var videoFile = ArrayList<File>()

    val videoNames = mutableListOf<String>()
    val videoUri = mutableListOf<Uri>()

    private var dialogCompressBinding: DialogCompressBinding? = null
    private var dialog: AlertDialog? = null
    private var viewTypeArray = ArrayList<ViewType<*>>()
    private val videoData = mutableListOf<Compress>()

    private val adapter by lazy {
        GenericListAdapter(object : OnItemClickListener<ViewType<*>> {
            override fun onItemClicked(view: View, item: ViewType<*>, position: Int) {
            }
        })
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val clipData = data.clipData
                    for (i in 0 until clipData!!.itemCount) {
                        lifecycleScope.launch {
                            val uri = clipData.getItemAt(i).uri
                            val compressImage = compressAndSaveImage(requireContext(), uri, 20)
                            imageFile.add(createFileFromUri(requireContext(), compressImage)!!)
                        }

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
                            videoUri.add(uri)
                            videoData.add(
                                i,
                                Compress(
                                    "",
                                    getVideoNameFromUri(uri) ?: "N/A",
                                    uri,
                                    getFileSize(getVideoSize(uri))
                                )
                            )


                            getVideoNameFromUri(uri)?.let { videoNames.add(it) }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "The selected item is not a video",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    showProgressDialog()
lifecycleScope.launch {
    siliComp(videoUri, videoNames)
}


                }
                //   listener.onSelectVideoFromSystem(videoFile)
            }


        }

    private fun getVideoSize(videoUri: Uri): Long {
        var size: Long = 0

        requireActivity().contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }

        return size
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


   suspend fun siliComp(list: List<Uri>, nameList: List<String>) = withContext(Dispatchers.IO)  {

        VideoCompressor.start(
            context = requireContext(), // => This is required
            uris = list, // => Source can be provided as content uris
            isStreamable = false,
            // THIS STORAGE
            sharedStorageConfiguration = SharedStorageConfiguration(
                saveAt = SaveLocation.movies, // => default is movies
                subFolderName = "my-videos" // => optional
            ),
            configureWith = Configuration(
                videoNames = nameList, /*list of video names, the size should be similar to the passed uris*/
                quality = VideoQuality.MEDIUM,
                isMinBitrateCheckEnabled = false,
                videoBitrateInMbps = 10, /*Int, ignore, or null*/
                disableAudio = false, /*Boolean, or ignore*/
                keepOriginalResolution = false, /*Boolean, or ignore*/
            ),
            listener = object : CompressionListener {
                override fun onProgress(index: Int, percent: Float) {
                    if (percent <= 100)
                        launchOnMainThread {
                            viewTypeArray[index] = RowItemCompress(
                                Compress(
                                    "",
                                    getVideoNameFromUri(videoUri[index]) ?: "N/A",
                                    videoUri[index],
                                    videoData[index].newFileSize, percent
                                )
                            )
                            adapter.items = viewTypeArray
                            adapter.notifyDataSetChanged()
                        }
                }

                override fun onStart(index: Int) {
                    launchOnMainThread {
                        viewTypeArray[index] = RowItemCompress(
                            Compress(
                                "",
                                getVideoNameFromUri(videoUri[index]) ?: "N/A",
                                videoUri[index],
                                videoData[index].newFileSize
                            )
                        )
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
Log.d("Size",getFileSize(size))
                    launchOnMainThread {
                        videoData[index] = Compress(
                            path,
                            getVideoNameFromUri(videoUri[index]) ?: "N/A", videoUri[index],
                            getFileSize(size),
                            100F
                        )
                        videoFile.add(File(path))
                        if (videoFile.size == list.size) {
                            viewTypeArray[index] = RowItemCompress(
                                Compress(
                                    "",
                                    getVideoNameFromUri(videoUri[index]) ?: "N/A",
                                    videoUri[index],
                                    getFileSize(size)
                                )
                            )
                            adapter.notifyDataSetChanged()
                            delay(4000)
                            dialog!!.dismiss()
                            listener.onSelectVideoFromSystem(videoFile)

                        }

                    }

                }

                override fun onFailure(index: Int, failureMessage: String) {
                    Log.d("onFailure", "onFailure: $failureMessage ")
                    dialog?.dismiss()
                    // On Failure
                }

                override fun onCancelled(index: Int) {
                    dialog?.dismiss()
                    // On Cancelled
                }

            }
        )

    }


    fun getVideoNameFromUri(uri: Uri): String? {
        // Resolve the URI to a file path
        val filePath = uri.path ?: return null

        // Extract the video name from the file path
        val videoName = File(filePath).name

        return videoName
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    fun showProgressDialog() {
        dialog = AlertDialog.Builder(requireActivity()).create()
        dialogCompressBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireActivity()),
            R.layout.dialog_compress,
            null,
            true
        )


        dialogCompressBinding?.recyclerView?.adapter = adapter

        for (data in videoData) {
            viewTypeArray.add(
                RowItemCompress(
                    data
                )
            )
        }
        adapter.items = viewTypeArray
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogCompressBinding!!.root)
            dialog.setCancelable(false)
            dialog.show()
        }


    }

    fun getFileSize(size: Long): String {
        if (size <= 0)
            return "0"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }


    fun getVideoWidthSize(uri: Uri): VideoSize? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val width =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
            val height =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
            return if (width != null && height != null) {
                VideoSize(width, height)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            retriever.release()
        }
    }

    fun launchOnMainThread(block: suspend CoroutineScope.() -> Unit) {
        val mainScope = MainScope()
        mainScope.launch {
            block()
        }
    }
}

data class VideoSize(val width: Int, val height: Int)

interface OnItemSelect {
    fun onSelectImageFromSystem(
        imageLinkList: ArrayList<File>? = null,
    )

    fun onSelectVideoFromSystem(
        videoFileList: ArrayList<File>? = null,
    )
}