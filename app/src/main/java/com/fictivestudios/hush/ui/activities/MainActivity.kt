package com.fictivestudios.hush.ui.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.activity.BaseActivity
import com.fictivestudios.hush.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private var _binding: ActivityMainBinding? = null
    val binding
        get() = _binding!!

    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    //val viewModel: MainViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        // Disconnect the sock
    // et when the activity is destroyed
        // viewModel.disconnectSocketIO()
    }

    override fun initialize() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        setOnBackPressedListener()
        val inflater = navHostFragment.navController.navInflater
        navGraph = inflater.inflate(R.navigation.nav_graph)
        navController = navHostFragment.navController

        val bundle = intent.extras
        val value = bundle?.getString("userLogout") ?: ""
        if (value == "logout") {
            navGraph.setStartDestination(R.id.preLoginFragment)
            navController.navigate(R.id.preLoginFragment)
        }else{
            navGraph.setStartDestination(R.id.splashFragment)
        }
        navController.graph = navGraph
    }

    override fun setObserver() {

    }

    override fun setOnClickListener() {
    }

    private fun setOnBackPressedListener() {
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            })
    }
}