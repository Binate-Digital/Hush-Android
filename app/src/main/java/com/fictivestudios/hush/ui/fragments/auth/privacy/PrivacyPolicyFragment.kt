package com.fictivestudios.hush.ui.fragments.auth.privacy

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.FragmentPrivacyBinding


@Suppress("DEPRECATION")
class PrivacyPolicyFragment : BaseFragment(R.layout.fragment_privacy), View.OnClickListener {

    private var _binding: FragmentPrivacyBinding? = null
    val binding
        get() = _binding!!

    val viewModel: PrivacyViewModel by viewModels()

    private val argument by navArgs<PrivacyPolicyFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObserver()
        setOnClickListener()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initialize() {
        initializePrivacy()

        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.pluginState = WebSettings.PluginState.ON
        binding.webView.settings.javaScriptEnabled = true

        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webView.webViewClient = MyWebViewClient()


    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setObserver() {
        viewModel.privacyResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                        it.value.data?.let { data ->
                            binding.webView.settings.javaScriptEnabled = true
                            binding.webView.settings.loadWithOverviewMode = true
                            binding.webView.settings.useWideViewPort = true
                            binding.webView.webViewClient = WebViewClient()
                            binding.webView.loadUrl(data.url)
                    }
                    binding.progress.hide()
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(it.message.toString())
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }
        }
    }


    private fun initializePrivacy() {
        val args = arguments
        if (args != null) {
            val value = args.getString("type")
            if (value != null) {
                viewModel.getPrivacyContent(value)
                binding.progress.show()
                if (value == PrivacyTypeEnum.PRIVACY.getValue()) {
                    binding.textViewTitle.text = "Privacy Policy"
                } else {
                    binding.textViewTitle.text = "Terms And Conditions"
                }
            }
        } else {
            viewModel.getPrivacyContent(argument.type)
            if (argument.type == PrivacyTypeEnum.PRIVACY.getValue()) {
                binding.textViewTitle.text = "Privacy Policy"
            } else {
                binding.textViewTitle.text = "Terms And Conditions"
            }
        }
    }
    private inner class MyWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            // Load the URL in the WebView itself
            if (url != null) {
                view?.loadUrl(url)
            }
            return true
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.e("WebView Error", error?.toString() ?: "Unknown error")
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.e("WebView HTTP Error", errorResponse?.toString() ?: "Unknown error")
        }

    }
}