package com.fictivestudios.hush.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.LayerDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.utils.Constants.IMAGE_BASE_URL
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

fun loadImageByUrl(
    imageView: AppCompatImageView,
    imageLink: String = "",
    strokeWidth: Float = 2f,
    centerRadius: Float = 2f,
) {

    val circleProgress = CircularProgressDrawable(imageView.context)
    circleProgress.strokeWidth = strokeWidth
    circleProgress.setColorSchemeColors(
        ContextCompat.getColor(
            imageView.context,
            R.color.theme_red
        )
    )
    //circleProgress.set = ContextCompat.getDrawable(imageView.context, R.drawable.place_holder)
    circleProgress.centerRadius = centerRadius
    circleProgress.start()
// Load the placeholder image
    val placeholderImage = ContextCompat.getDrawable(imageView.context, R.drawable.place_holder)

// Create a LayerDrawable and set the layers (placeholder + progress)
    val layers = arrayOf(placeholderImage, circleProgress)
    val layerDrawable = LayerDrawable(layers)

// Set the LayerDrawable as the image for your ImageView
    imageView.setImageDrawable(layerDrawable)

    Glide.with(imageView.context)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .load(IMAGE_BASE_URL + imageLink).placeholder(circleProgress)
        .placeholder(R.drawable.place_holder)
        .error(circleProgress)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
        .thumbnail(0.25f).timeout(10000)
        .into(imageView)
}

fun formatDateToTime(inputDateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
    val outputFormat = SimpleDateFormat("h:mm a")

    // Parse input date string
    val date: Date = inputFormat.parse(inputDateString) ?: Date()

    // Format date to desired format
    return outputFormat.format(date)
}

fun createVideoFile(path: String): Boolean {
    val mediaRecorder = MediaRecorder()

    try {
        // Configure MediaRecorder
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
        mediaRecorder.setOutputFile(path)

        // Prepare MediaRecorder
        mediaRecorder.prepare()

        // Start recording
        mediaRecorder.start()

        // Return true indicating success
        return true
    } catch (e: IllegalStateException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        // Release MediaRecorder resources
        mediaRecorder.release()
    }

    // Return false indicating failure
    return false
}


fun getPathFromUri(context: Context, uri: Uri): String? {
    var filePath: String? = null
    if ("content" == uri.scheme) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                filePath = if (columnIndex != -1) {
                    it.getString(columnIndex)
                } else {
                    null
                }
            }
        }
    } else if ("file" == uri.scheme) {
        filePath = uri.path
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
            context,
            uri
        )
    ) {
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                filePath = context.getExternalFilesDir(null)?.absolutePath + "/" + split[1]
            }
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), id.toLong()
            )
            filePath = getDataColumn(context, contentUri, null, null)
        }
    }
    return filePath
}

private fun getDataColumn(
    context: Context,
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = cursor.getString(columnIndex)
        }
    }
    return filePath
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

// document opener by custom tab
//fun openCustomTab(context: Context, url: String?) {
//    val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
//    val customTabsIntent: CustomTabsIntent = builder.build()
//    customTabsIntent.launchUrl(context, Uri.parse(url))
//}


fun convertUtcToLocalPsk(utcTime: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    // Parse the UTC time string
    val utcDate = dateFormat.parse(utcTime)

    // Convert to the current time zone
    val currentDateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    currentDateFormat.timeZone = TimeZone.getDefault() // Current time zone

    // Format the date in the current time zone
    return currentDateFormat.format(utcDate)
}


fun Fragment.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", requireActivity().packageName, null)
    ).also(::startActivity)
}


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", this.packageName, null)
    ).also(::startActivity)
}


fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeOnClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}


@SuppressLint("Recycle")
fun createFileFromUri(context: Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)

    inputStream?.let {
        try {
            // Create a temporary file in your app's cache directory
            val tempFile = createTempImageFile(context)
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

fun byteArrayToVideoFile(context: Context, byteArray: ByteArray, fileName: String): File? {
    var file: File? = null
    var fos: FileOutputStream? = null

    try {
        // Get the directory for the user's public video directory.
        val videoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        videoDir.mkdirs()

        // Create a file in the video directory
        file = File(videoDir, fileName)
        fos = FileOutputStream(file)

        // Write the byte array to the file
        fos.write(byteArray)
        fos.flush()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    return file
}

fun createVideoFileFromUri(context: Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)

    inputStream?.let {
        try {
            // Create a temporary file in your app's cache directory
            val tempFile = createTempVideoFile(context)
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

fun createTempImageFile(context: Context): File {
    val imageFileName = "temp_image_${System.currentTimeMillis()}.png"
    val storageDir = context.cacheDir
    return File(storageDir, imageFileName)
}

fun createTempVideoFile(context: Context): File {
    val imageFileName = "video.mp4"
    val storageDir = context.cacheDir
    return File(storageDir, imageFileName)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun compressAndSaveImage(context: Context, imageUri: Uri, quality: Int): Uri {
    // Use content resolver to open an InputStream from the imageUri
    val contentResolver: ContentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)

    // Decode the InputStream into a Bitmap
    val originalBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

    // Close the InputStream
    inputStream?.close()

    // Use the compressAndSaveImage function to compress and save the Bitmap
    return compressAndSaveImage(context, originalBitmap, quality)
}

fun compressAndSaveImage(context: Context, originalBitmap: Bitmap, quality: Int): Uri {
    // Create a file to save the compressed image
    val compressedImageFile = createTempFile(context)

    // Compress the image to the specified quality
    originalBitmap.compress(
        Bitmap.CompressFormat.JPEG,
        quality,
        FileOutputStream(compressedImageFile)
    )

    // Insert the compressed image file into the MediaStore to get a content Uri
    val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, compressedImageFile.name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val uri = context.contentResolver.insert(contentUri, contentValues)

    // Use the content resolver to open an OutputStream and copy the compressed image file
    uri?.let { targetUri ->
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            compressedImageFile.inputStream().copyTo(outputStream)
        }
    }

    return uri ?: Uri.EMPTY
}

private fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("compressed_", ".jpg", storageDir)
}

fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
    return sdf.format(calendar.time)
}

private fun isValidLink(link: String): Boolean {
    val pattern = Pattern.compile("^(https?|ftp)://[A-Za-z0-9.-]+(\\:[0-9]+)?(/[A-Za-z0-9_.-]*)*$")
    return pattern.matcher(link).matches()
}

fun getAmPm(hour: Int): String {
    return if (hour < 12) "AM" else "PM"
}

fun getDate(dateTimeString: String): String {
    val instant = Instant.parse(dateTimeString)
    val zoneId = ZoneId.of("UTC") // Assuming the input string is in UTC timezone
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)
    return formatter.format(instant.atZone(zoneId))
}

//fun showPopupMenu(context: Context, anchorView: View, items: List<String>, onMenuItemClick: (String) -> Unit) {
//    val popupMenu = PopupMenu(context, anchorView)
//    popupMenu.menuInflater.inflate(R.menu.item_menu, popupMenu.menu)
//    popupMenu.menu.clear()
//
//    for (item in items) {
//        popupMenu.menu.add(item)
//    }
//
//    popupMenu.setOnMenuItemClickListener { menuItem ->
//        onMenuItemClick(menuItem.title.toString())
//        true
//    }
//
//    popupMenu.show()
//    //popupMenu.popupWindow?.setBackgroundDrawable(context.getDrawable(android.R.color.white)) // Set the background color
//    //popupMenu.menuView?.setBackgroundColor(context.getColor(android.R.color.white)) // Set the background color
//}
fun calculateAge(dateOfBirth: String): Int {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val dobDate = LocalDate.parse(dateOfBirth, formatter)

    val currentDate = LocalDate.now()

    return Period.between(dobDate, currentDate).years
}


fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Find the currently focused view, so we can grab the correct window token from it.
    val currentFocusedView = activity.currentFocus

    // If no view currently has focus, create a new one, just so we can grab a window token from it.
    if (currentFocusedView != null) {
        inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
    }
}

fun getTime(dateTimeString: String): String {
    val instant = Instant.parse(dateTimeString)
    val zoneId = ZoneId.of("UTC") // Assuming the input string is in UTC timezone
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    return formatter.format(instant.atZone(zoneId))
}


fun createVideoPart(videoFile: File): MultipartBody.Part {
    val requestFile = videoFile.asRequestBody("video/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(
        "attachments[]",
        videoFile.name,
        requestFile
    )
}

fun createImagePart(imageFile: File): MultipartBody.Part {
    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(
        "attachments[]",
        imageFile.name,
        requestFile
    )
}

fun getDateTime(dateTimeString: String): String {
    val instant = Instant.parse(dateTimeString)
    val zoneId = ZoneId.of("UTC")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.US)
    return formatter.format(instant.atZone(zoneId))
}


fun showConfirmationDialog(
    context: Context,
    title: String,
    description: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val alertDialogBuilder = Builder(context)
    alertDialogBuilder.setTitle(title)
    alertDialogBuilder.setMessage(description)


    alertDialogBuilder.setPositiveButton(confirmText) { _, _ ->
        onConfirm.invoke()
    }

    alertDialogBuilder.setNegativeButton(cancelText) { dialog, _ ->
        dialog.dismiss() // Dismiss the dialog
        onCancel.invoke()
    }


    val alertDialog = alertDialogBuilder.create()
    alertDialog.show()
}

fun String.getLocalDateFromString(dateFormat: String): Date? {
    val df = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    df.timeZone = TimeZone.getDefault()
    return df.parse(this)
}

fun String.getGlobalDateFromString(dateFormat: String): Date? {
    val df = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.parse(this)
}
//
//fun String.getFormattedDateFromServerDate(context: Context, requiredDateFormat: String): String {
//    val date = this.getLocalDateFromString("yyyy-MM-dd'T'HH:mm:ss")
//    return if (date != null) {
//        val finalFormatter = SimpleDateFormat(requiredDateFormat, Locale.getDefault())
//        finalFormatter.format(date)
//    } else {
//        context.getString(R.string.unknown_date)
//    }
//}

fun <A : Activity> Activity.startNewActivity(activity: Class<A>, bundle: Bundle? = null) {
    val intent = Intent(this, activity)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}

fun Fragment.handleApiError(
    activity: Activity,
    failure: Resource.Failure,
    retry: () -> Unit = {}
) {
    when {
        failure.isNetworkError -> requireView().showSnackBar(
            "Please check your internet connection",
            "Retry",
            retry
        )

//        failure.errorCode == 401 -> {
//            if (this is LoginFragment) {
//                requireView().showSnackBar("You've entered incorrect email or password")
//            } else {
//                if(activity == ChildDashboardActivity()){
//                    performUserLogoutFromChild()
//                }else{
//                    performUserLogoutFromParent()
//                }
//
//            }
//        }
//
//        failure.errorCode == 400 -> {
//            if (this is LoginFragment) {
//                requireView().showSnackBar(failure.message.toString())
//            }
//
////            if (this is ChangePasswordFragment) {
////                requireView().showSnackBar(failure.message.toString())
////            }
//
//            if (this is SignUpFragment) {
//                requireView().showSnackBar(failure.message.toString())
//            }
//        }
//
//        else -> {
//            val type = object : TypeToken<BaseNetworkResponse<Any>>() {}.type
//            val errorResponse: BaseNetworkResponse<Any>? =
//                Gson().fromJson(failure.errorBody?.string().toString(), type)
//
//            val message = errorResponse?.message ?: failure.errorBody?.string().toString()
//            errorResponse?.let {
//                requireView().showSnackBar(message)
//            }
//        }
    }
}

//fun vectorToBitmap(
//    context: Context,
//    @DrawableRes vectorDrawableId: Int,
//    @ColorInt color: Int,
//    size: Int
//): BitmapDescriptor {
//    // Convert the vector drawable to a bitmap and resize it
//    val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableId)
//    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//    vectorDrawable?.setBounds(0, 0, canvas.width, canvas.height)
//    DrawableCompat.setTint(vectorDrawable!!, color)
//    vectorDrawable.draw(canvas)
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}


fun blur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
    val renderScript = RenderScript.create(context)
    val inputAllocation = Allocation.createFromBitmap(renderScript, bitmap)
    val outputAllocation = Allocation.createTyped(renderScript, inputAllocation.type)

    val blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    blurScript.setInput(inputAllocation)
    blurScript.setRadius(radius)
    blurScript.forEach(outputAllocation)

    outputAllocation.copyTo(bitmap)
    renderScript.destroy()

    return bitmap
}


fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val resolver: ContentResolver = context.contentResolver
    val contentValues = ContentValues()
    contentValues.put(
        MediaStore.MediaColumns.DISPLAY_NAME,
        "image_${System.currentTimeMillis()}.png"
    )
    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

    val imageUri: Uri? =
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    imageUri?.let {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            outputStream?.close()
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}


//fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
//    val safeClickListener = SafeOnClickListener {
//        onSafeClick(it)
//    }
//    setOnClickListener(safeClickListener)
//}

fun View.showSnackBar(
    message: String,
    action: String = "",
    actionListener: () -> Unit = {}
): Snackbar {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    if (action != "") {
        snackBar.duration = Snackbar.LENGTH_INDEFINITE
        snackBar.setAction(action) {
            actionListener()
            snackBar.dismiss()
        }
    }
    snackBar.show()
    return snackBar
}

fun String.isValidEmail(): Boolean {
    val emailRegex = ("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    val pattern = Pattern.compile(emailRegex)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

fun String.isValidPassword(): Boolean {
    //        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
    val passwordRegex = "((?!\\s)\\A)(\\s|(?<!\\s)\\S){6,20}\\Z"
    val pattern = Pattern.compile(passwordRegex)
    val matcher = pattern.matcher(this)
    return matcher.matches()
}

//fun isInternetNotConnected(context: Context): Boolean {
//    val connectivityManager =
//        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        val networkCapabilities = connectivityManager.activeNetwork ?: return true
//        val activeNetwork =
//            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return true
//
//        return !activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    } else {
//        val activeNetworkInfo = connectivityManager.activeNetworkInfo
//        return activeNetworkInfo == null || !activeNetworkInfo.isConnected
//    }
//}