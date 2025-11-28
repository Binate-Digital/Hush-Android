package com.fictivestudios.demo.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.activity.BaseActivity
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.databinding.ActivityDashBoardBinding
import com.fictivestudios.demo.databinding.DialogLogoutBinding
import com.fictivestudios.demo.ui.fragments.auth.privacy.PrivacyTypeEnum
import com.fictivestudios.demo.utils.Constants
import com.fictivestudios.demo.utils.gallery.CameraPermissionTextProvider
import com.fictivestudios.demo.utils.gallery.NotificationPermissionTextProvider
import com.fictivestudios.demo.utils.gallery.showPermissionDialog
import com.fictivestudios.demo.utils.openAppSettings
import com.fictivestudios.demo.utils.startNewActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DashBoardActivity : BaseActivity(), View.OnClickListener {

    private var _binding: ActivityDashBoardBinding? = null
    val binding
        get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var toggle: ActionBarDrawerToggle

    private var dialogLogoutBinding: DialogLogoutBinding? = null
    private var dialog: AlertDialog? = null

    private val viewModel: MainViewModel by viewModels()

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
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
                    onGoToAppSettingsClick = ::openAppSettings, context = this
                )
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        showNotificationPermissionRationale()
                    } else {
                        showSettingDialog()
                    }
                }
            }
        }

    private val destinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatFragment -> {
                    binding.textViewTitle.text = "Chats"
                    binding.viewSeparator.gone()
                    binding.actionBar.show()
                    binding.cardView.show()
                    binding.view.show()
                    binding.imageViewNotification.show()
                    binding.imageViewEditProfile.gone()
                }

                R.id.addressFragment -> {
                    binding.viewSeparator.gone()
                    binding.actionBar.show()
                    binding.textViewTitle.text = "Address Book"
                    binding.cardView.show()
                    binding.view.show()
                    binding.imageViewNotification.show()
                    binding.imageViewEditProfile.gone()
                }

                R.id.dialPadFragment -> {
                    binding.viewSeparator.gone()
                    binding.actionBar.show()
                    binding.textViewTitle.text = "Dial Pad"
                    binding.cardView.show()
                    binding.view.show()
                    binding.imageViewNotification.show()
                    binding.imageViewEditProfile.gone()
                }

                R.id.profileFragment -> {
                    binding.actionBar.show()
                    binding.viewSeparator.show()
                    binding.textViewTitle.text = "Profile"
                    binding.cardView.show()
                    binding.view.show()
                    binding.imageViewNotification.gone()
                    binding.imageViewEditProfile.show()
                }

                else -> {
                    binding.cardView.gone()
                    binding.view.gone()
                    binding.actionBar.gone()
                }
            }
        }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        _binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(destinationChangedListener)
        _binding = null
        dialog = null

        dialogLogoutBinding = null
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }

    override fun initialize() {56
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragmentDashboard) as NavHostFragment
        setOnBackPressedListener()
        val inflater = navHostFragment.navController.navInflater
        navGraph = inflater.inflate(R.navigation.nav_graph_dashboard)
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(destinationChangedListener)
        binding.bottomNavigationViewMain.setupWithNavController(navController)

            toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        )
        if (viewModel.userData == null) {
            showToast("something went wrong.")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setUserDataInDrawer(viewModel.userData)
        viewModel.getCallTokenApi()

        if (!checkCameraPermission()) {
            requestCameraPermission()
        }
        if (!checkNotificationPermission()) {
            requestNotificationPermission()
        }
    }


    override fun setObserver() {
        viewModel.logoutUserResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    showToast(it.value!!.message)
                    binding.progress.hide()
                    viewModel.preferenceLogout()
                    val bundle = Bundle()
                    bundle.putString("userLogout", "logout")
                    startNewActivity(MainActivity::class.java, bundle)
                    finish()
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    if (it.errorCode == 401) {

                        lifecycleScope.launch {
                            viewModel.preferenceLogout()
                            val intent =
                                Intent(this@DashBoardActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                is Resource.Loading -> {
                    binding.progress.show()
                }

                else -> {}
            }
        }

        viewModel.callTokenResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        viewModel.saveCallToken(it.value.data?.token ?: "")
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        lifecycleScope.launch {
                            viewModel.preferenceLogout()
                            val intent =
                                Intent(this@DashBoardActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.imageViewMoreOptionDrawer.setOnClickListener(this)
        binding.imageViewNotification.setOnClickListener(this)
        binding.imageViewEditProfile.setOnClickListener(this)
        binding.navLogout.setOnClickListener(this)
        binding.navCallLogs.setOnClickListener(this)
        binding.navHome.setOnClickListener(this)
        binding.navTerms.setOnClickListener(this)
        binding.navPrivacy.setOnClickListener(this)
        binding.navScreenShot.setOnClickListener(this)
        binding.navSetting.setOnClickListener(this)
        binding.navSecurity.setOnClickListener(this)
        binding.navRegisterNumber.setOnClickListener(this)
        binding.navSubscription.setOnClickListener(this)
        binding.navSetting.setOnClickListener(this)
        binding.imageViewEditProfile.setOnClickListener(this)
        binding.imageViewUser.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.navSubscription.id -> {
                showToast("Will be implement in next phase.")
            }

            binding.navSecurity.id -> {
                navigateFragment(R.id.securityFeatureFragment)
            }

            binding.navScreenShot.id -> {
                navigateFragment(R.id.screenShotFragment)
            }

            binding.navRegisterNumber.id -> {
                navigateFragment(R.id.registerPhoneNo)
            }

            binding.navCallLogs.id -> {
                navigateFragment(R.id.callLogFragment)
            }

            binding.navHome.id -> {
                navigateFragment(R.id.chatFragment)
            }

            binding.navSetting.id -> {
                navigateFragment(R.id.settingFragment)
            }

            binding.imageViewUser.id -> {
                navigateFragment(R.id.profileFragment)
            }

            binding.navPrivacy.id -> {
                val args = Bundle()
                args.putString("type", PrivacyTypeEnum.PRIVACY.getValue())
                navigateFragment(R.id.privacyPolicyFragment2, args)
            }

            binding.navTerms.id -> {
                val args = Bundle()
                args.putString("type", PrivacyTypeEnum.TERMS_AND_CONDITION.getValue())
                navigateFragment(R.id.privacyPolicyFragment2, args)
            }


            binding.imageViewMoreOptionDrawer.id -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                toggle.syncState()
            }

            binding.imageViewEditProfile.id -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                navController.navigate(R.id.editProfileFragment)
            }

            binding.imageViewNotification.id -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                navController.navigate(R.id.notificationFragment)

            }

            binding.navSetting.id -> {
                navigateFragment(R.id.settingFragment)

            }

            binding.navLogout.id -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                lifecycleScope.launch {
                    delay(100)
                    showExitMessageBox(true)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkNotificationPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = CameraPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA),
                onDismiss = {},
                onOkClick = { openAppSettings() },
                onGoToAppSettingsClick = ::openAppSettings, context = this
            )
        } else {
            // Request camera permission using the launcher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestNotificationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            showPermissionDialog(
                permissionTextProvider = NotificationPermissionTextProvider(),
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                onDismiss = {},
                onOkClick = {
                    openAppSettings()
                },
                onGoToAppSettingsClick = ::openAppSettings, context = this
            )
        } else {
            // Request camera permission using the launcher
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun setUserDataInDrawer(userData: LoginUserResponse?) {
        userData?.apply {
            val url = Constants.IMAGE_BASE_URL + this.profileImage
            @Suppress("SENSELESS_COMPARISON")
            if (this.profileImage == null) {
                binding.imageViewUser.setImageResource(R.drawable.person)
            } else {
                Glide.with(this@DashBoardActivity)
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.imageViewUser)
            }

            binding.textViewUserName.text = this.name
        }
    }


    private fun showExitMessageBox(isUserLogout: Boolean) {
        // Build and show the alert dialog
        dialog = AlertDialog.Builder(this).create()
        dialogLogoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.dialog_logout,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogLogoutBinding!!.root)
            dialog.setCancelable(false)

            if (isUserLogout) {
                dialogLogoutBinding!!.textViewActionTitle.text = "Logout"
                dialogLogoutBinding!!.buttonDelete.text = "Logout"
                dialogLogoutBinding!!.textViewDes.text = "Are you sure you want to \nlogout?"
            } else {
                dialogLogoutBinding!!.textViewActionTitle.text = "Exit"
                dialogLogoutBinding!!.buttonDelete.text = "Exit"
                dialogLogoutBinding!!.textViewDes.text = "Are you sure you want to \nexit?"
            }

            dialogLogoutBinding!!.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogLogoutBinding!!.imageViewCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogLogoutBinding!!.buttonDelete.setOnClickListener {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                if (isUserLogout) {
                    logout()
                } else {
                    dialog.dismiss()
                    finish()
                }
                dialogLogoutBinding!!.progress.show()
            }
            dialog.show()
        }
    }

    private fun navigateFragment(id: Int, args: Bundle? = null) {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        lifecycleScope.launch {
            delay(400)
            if (args == null) {
                navController.navigate(id)
            } else {
                navController.navigate(id, args)
            }
        }
    }

    fun logout() {
        if (viewModel.userData?.userSocialType == "google" || viewModel.userData?.userSocialType == "phone") {
            viewModel.preferenceLogout()
            signOutSocialLogin()
        } else {
            binding.progress.show()
            viewModel.performLogout()
            binding.progress.show()
        }

    }

    fun signOutSocialLogin() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val googleSignInClient =
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                lifecycleScope.launch {
                    viewModel.preferenceLogout()
                    delay(1500)
                    val bundle = Bundle()
                    bundle.putString("userLogout", "logout")
                    startNewActivity(MainActivity::class.java, bundle)
                    finish()

                }

            }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun showNotificationPermissionRationale() {

        showPermissionDialog(
            permissionTextProvider = NotificationPermissionTextProvider(),
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            ),
            onDismiss = {},
            onOkClick = {
                openAppSettings()


            },
            onGoToAppSettingsClick = ::openAppSettings, context = this
        )

    }

    private fun setOnBackPressedListener() {
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitMessageBox(false)
                }
            })
    }
}