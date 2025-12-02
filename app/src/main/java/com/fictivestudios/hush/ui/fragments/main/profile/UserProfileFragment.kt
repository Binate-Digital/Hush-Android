package com.fictivestudios.hush.ui.fragments.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.data.responses.ContactResponse
import com.fictivestudios.hush.databinding.FragmentUserProfileBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.BlockUserBottomSheet
import com.fictivestudios.hush.utils.Constants

class UserProfileFragment : BaseFragment(R.layout.fragment_user_profile), View.OnClickListener,
    ApiResponse {

    private var _binding: FragmentUserProfileBinding? = null
    val binding
        get() = _binding!!
    val viewModel: ProfileViewModel by viewModels()
    private var bottomSheet: BlockUserBottomSheet? = null
    private val args by navArgs<UserProfileFragmentArgs>()
    private var userImage = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater)
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
        viewModel.getContactUserProfile(args.userId)
        binding.progress.show()
    }

    override fun setObserver() {

        viewModel.contactUserProfileResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    binding.progress.hide()
                    data.value.data?.let {
                        setUserProfile(it)
                    }
                    showToast(data.value.message)
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(data.message.toString())
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.blockUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    showToast(data.value.message)
                    viewModel.listener?.response(true, ProfileActionTypeEnum.BLOCK.getValue())
                }

                is Resource.Failure -> {
                    viewModel.listener?.response(false, ProfileActionTypeEnum.BLOCK.getValue())
                    showToast(
                        data.message.toString()
                    )
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }


        viewModel.deleteUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    showToast(data.value.message)
                    viewModel.listener?.response(true, ProfileActionTypeEnum.DELETE.getValue())
                }

                is Resource.Failure -> {
                    viewModel.listener?.response(false, ProfileActionTypeEnum.DELETE.getValue())
                    showToast(
                        data.message.toString()
                    )
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.reportUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    viewModel.listener?.response(true, ProfileActionTypeEnum.REPORT.getValue())
                    showToast(data.value.message)
                }

                is Resource.Failure -> {
                    viewModel.listener?.response(false, ProfileActionTypeEnum.REPORT.getValue())
                    showToast(data.message.toString())
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.imageViewMoreOption.setOnClickListener(this)
        binding.userProfileCardView.setOnClickListener(this)
        binding.imageViewBack.setOnClickListener(this)
        binding.buttonCall.setOnClickListener(this)
        binding.buttonMessage.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewMoreOption.id -> {
                val bottomSheet = BlockUserBottomSheet()
                bottomSheet.userId = args.userId
                bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)

            }

            binding.userProfileCardView.id -> {
                findNavController().navigate(
                    UserProfileFragmentDirections.actionUserProfileFragmentToImagePreviewFragment(
                        userImage,
                        "", ""
                    )
                )
            }

            binding.buttonCall.id, binding.buttonMessage.id -> {
                showToast("Will be implement in next phase.")
            }

            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }
        }
    }

    override fun response(isApiSuccess: Boolean, userActionType: String) {
        if (isApiSuccess) {
            when (userActionType) {
                ProfileActionTypeEnum.BLOCK.getValue(), ProfileActionTypeEnum.DELETE.getValue() -> {
                    findNavController().popBackStack()
                }

                ProfileActionTypeEnum.REPORT.getValue() -> {
                    bottomSheet?.dismissNow()
                }
            }
        }
    }

    private fun setUserProfile(data: ContactResponse) {
        binding.userNameTextView.text = data.fullName
        binding.phoneNoTextView.text = data.phone
        if (data.notes == "" || data.notes == null) {
            binding.notesCardView.gone()
        } else {
            binding.textViewNotes.text = data.notes
        }

        val url = Constants.IMAGE_BASE_URL + data.contactImage
        userImage = data.contactImage ?: ""
        if (data.contactImage == null) {
            binding.userPersonImageView.setImageResource(R.drawable.person)
        } else {
            Glide.with(binding.userPersonImageView.context)
                .load(url).placeholder(R.drawable.person)
                .into(binding.userPersonImageView)

        }
    }
}