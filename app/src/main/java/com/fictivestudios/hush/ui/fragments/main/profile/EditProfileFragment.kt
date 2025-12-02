package com.fictivestudios.hush.ui.fragments.main.profile

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.data.responses.LoginUserResponse
import com.fictivestudios.hush.data.responses.MapResponse
import com.fictivestudios.hush.databinding.DialogProfileBinding
import com.fictivestudios.hush.databinding.FragmentEditProfileBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.AttachmentTypeEnum
import com.fictivestudios.hush.utils.Constants
import com.fictivestudios.hush.utils.MapDialog
import com.fictivestudios.hush.utils.UsPhoneNumberFormatter
import com.fictivestudios.hush.utils.gallery.GalleryBottomSheet
import com.fictivestudios.hush.utils.gallery.OnImageSelect
import com.fictivestudios.hush.utils.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.ref.WeakReference

class EditProfileFragment : BaseFragment(R.layout.fragment_edit_profile),
    MapDialog.MapDialogListener, View.OnClickListener {

    private var _binding: FragmentEditProfileBinding? = null
    val binding
        get() = _binding!!
    val viewModel: ProfileViewModel by viewModels()
    private var profileImage: File? = null
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var locationName: String = ""
    private var bottomSheet: GalleryBottomSheet? = null
    private var userData: LoginUserResponse? = null

    private var dialogProfileCompletedBinding: DialogProfileBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
        dialogProfileCompletedBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        viewModel.getUserByToken()
        disableUserTouch()
    }

    override fun setObserver() {
        viewModel.loginUserResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progress.show()
                }

                is Resource.Success -> {
                    it.value.data.let {
                        binding.progress.hide()
                        userData = it
                        lat = userData?.location?.coordinates?.get(1) ?: 0.0
                        long = userData?.location?.coordinates?.get(0) ?: 0.0
                        setUserProfile(it)
                        enableUserTouch()
                    }
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    enableUserTouch()
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                else -> {}
            }
        }

        viewModel.createNewProfileResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    it.value.data?.let { loginResponse ->
                        lifecycleScope.launch {
                            viewModel.saveLoggedInUser(loginResponse)
                            (requireActivity() as DashBoardActivity).setUserDataInDrawer(
                                loginResponse
                            )
                            delay(2000)
                            binding.progress.hide()
                            enableUserTouch()
                            showAccountCreatedMessageBox()
                        }

                    }

                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    enableUserTouch()
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {}

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.saveButton.setOnClickListener(this)
        binding.userPersonImageView.setOnClickListener(this)
        binding.imageViewBack.setOnClickListener(this)
        binding.textInputLayoutLocation.setOnClickListener(this)
        binding.textInputEditTextLocation.setOnClickListener(this)

        val addLineNumberFormatter = UsPhoneNumberFormatter(
            WeakReference<TextInputEditText>(binding.textInputEditTextPhoneNo)
        )
        binding.textInputEditTextPhoneNo.addTextChangedListener(addLineNumberFormatter)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.textInputEditTextLocation.id, binding.textInputLayoutLocation.id -> {
                openMapDialogFragment()
            }

            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

            binding.saveButton.id -> {
                hideKeyboard(requireActivity())
                updateProfile()

            }

            binding.userPersonImageView.id -> {
                bottomSheet = GalleryBottomSheet(
                    AttachmentTypeEnum.IMAGE.getValue(),
                    object : OnImageSelect {
                        override fun onSelectImageFromSystem(
                            imageUri: Uri?,
                            imageLink: File?
                        ) {
                            binding.userPersonImageView.setImageURI(imageUri)
                            profileImage = imageLink
                            bottomSheet?.dismiss()
                        }
                    })
                bottomSheet!!.show(requireActivity().supportFragmentManager, bottomSheet!!.tag)
            }
        }
    }

    private fun setUserProfile(data: LoginUserResponse?) {
        if (data?.userSocialType == "phone") {
            binding.textInputEditTextPhoneNo.isEnabled = false
            binding.textInputEditTextPhoneNo.isClickable = false
        }
        locationName = data?.location?.address ?: ""
        lat = data?.location?.coordinates?.get(1) ?: 0.0
        long = data?.location?.coordinates?.get(0) ?: 0.0
        data?.let {
//            if (it.user_social_type == "google") {
//                binding.emailTextView.gone()
//                binding.tvEmail.gone()
//                binding.view2.gone()
//            } else {
//                binding.emailTextView.show()
//                binding.tvEmail.show()
//                binding.view2.show()
//            }
            val url = Constants.IMAGE_BASE_URL + it.profileImage
            if (it.profileImage == null) {
                binding.userPersonImageView.setImageResource(R.drawable.person)
            } else {
                Glide.with(requireContext())
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.userPersonImageView)
            }

            binding.textInputEditTextFullName.setText(it.name)
            binding.textInputEditTextLocation.setText(it.location?.address)
            binding.textInputEditTextPhoneNo.setText(it.phone)
            binding.textInputEditTextDescription.setText(it.description)
        }
    }

    private fun updateProfile() {

        val phoneNo = binding.textInputEditTextPhoneNo.text.toString().trim()
        val fullName = binding.textInputEditTextFullName.text.toString().trim()
        val address = binding.textInputEditTextLocation.text.toString().trim()
        val description = binding.textInputEditTextDescription.text.toString().trim()

        if (fullName.isEmpty()) {
            showToast("Full name field can't be empty.")
            return
        }


        if (phoneNo.isEmpty()) {
            showToast("Phone number field can't be empty")
            return
        }
        if (phoneNo.length > 15) {
            showToast("Invalid Phone Number")
            return
        }

        if (address.isEmpty()) {
            showToast("Location field can't be empty")
            return
        }

        val requestFile = profileImage?.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = (if (profileImage == null) null else requestFile!!)?.let {
            MultipartBody.Part.createFormData(
                "profileImage", profileImage.toString(),
                it
            )
        }
        binding.progress.show()
        disableUserTouch()
        viewModel.createNewProfileApi(
            name = fullName,
            phoneNo = phoneNo,
            profileImage = imagePart,
            description = description,
            lat = lat.toString(),
            long = long.toString(),
            address = address,
            token = userData?.token ?: ""
        )


    }

    private fun openMapDialogFragment() {
        val mapDialogFragment = MapDialog()
        mapDialogFragment.listener = this
        mapDialogFragment.latitude = lat
        mapDialogFragment.longitude = long
        mapDialogFragment.locationName = locationName
        mapDialogFragment.show(childFragmentManager, "MapDialogFragment")
    }

    private fun showAccountCreatedMessageBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogProfileCompletedBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_profile,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogProfileCompletedBinding!!.root)
            dialog.setCancelable(false)
            dialogProfileCompletedBinding?.textViewDes?.text =
                "Profile has been updated successfully."

            dialogProfileCompletedBinding!!.imageViewCancel.gone()
            dialogProfileCompletedBinding!!.buttonContinue.setOnClickListener {
                findNavController().popBackStack()
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onDataPassed(data: MapResponse) {
        binding.textInputEditTextLocation.setText(data.name)
//        if(lat.toString().contains("-")){
//            lat = data.longitude
//            long = data.latitude
//        }else{
//            lat = data.latitude
//            long = data.longitude
//        }

        lat = data.latitude
        long = data.longitude

    }
}
