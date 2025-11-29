package com.fictivestudios.demo.ui.fragments.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.LoginUserResponse
import com.fictivestudios.demo.databinding.FragmentProfileBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.utils.Constants.IMAGE_BASE_URL

class ProfileFragment : BaseFragment(R.layout.fragment_profile), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentProfileBinding? = null
    val binding
        get() = _binding!!
    val viewModel: ProfileViewModel by viewModels()
    private var userImage = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater)
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

    override fun initialize() {
        binding.progress.show()
        viewModel.getUserByToken()
    }


    override fun setObserver() {
        viewModel.loginUserResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    binding.progress.hide()
                    it.value.data?.let { data ->
                      setUserProfile(data)
                    }

                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.userPersonImageView.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun setUserProfile(data: LoginUserResponse?) {
        userImage = data?.profileImage.toString()
        data?.let {

            if (data.description == "" || data.description == null) {
                binding.descriptionCardView.gone()
            }

//            if (it.user_social_type == "google") {
//                binding.emailTextView.gone()
//                binding.tvEmail.gone()
//                binding.view2.gone()
//            } else {
//                binding.emailTextView.show()
//                binding.tvEmail.show()
//                binding.view2.show()
//            }
            val url = IMAGE_BASE_URL + it.profileImage
            if (it.profileImage == null) {
                binding.userPersonImageView.setImageResource(R.drawable.person)
            } else {

                Glide.with(requireContext())
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.userPersonImageView)
            }

            binding.userNameTextView.text = it.name
            binding.locationTextView.text = it.location?.address
            binding.phoneNoTextView.text = it.phone
            binding.textViewDescription.text = it.description
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.userPersonImageView.id -> {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToImagePreviewFragment(
                        userImage,"",""
                    )
                )
            }
        }
    }

    override fun onRefresh() {
        viewModel.getUserByToken()
    }
}