package com.fictivestudios.hush.ui.fragments.auth.createNewProfile

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.data.responses.MapResponse
import com.fictivestudios.hush.databinding.DialogProfileBinding
import com.fictivestudios.hush.databinding.FragmentCreateNewProfileBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.AttachmentTypeEnum
import com.fictivestudios.hush.utils.MapDialog
import com.fictivestudios.hush.utils.UsPhoneNumberFormatter
import com.fictivestudios.hush.utils.gallery.GalleryBottomSheet
import com.fictivestudios.hush.utils.gallery.OnImageSelect
import com.fictivestudios.hush.utils.hideKeyboard
import com.fictivestudios.hush.utils.setSafeOnClickListener
import com.fictivestudios.hush.utils.startNewActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.ref.WeakReference

class CreateNewProfileFragment : BaseFragment(R.layout.fragment_create_new_profile),
    View.OnClickListener, MapDialog.MapDialogListener {

    private var _binding: FragmentCreateNewProfileBinding? = null
    val binding
        get() = _binding!!

    private var bottomSheet: GalleryBottomSheet? = null
    private val viewModel: CreateNewProfileViewModel by viewModels()
    private val args by navArgs<CreateNewProfileFragmentArgs>()
    private var profileImage: File? = null
    private var lat: Double = 0.0
    private var long: Double = 0.0

    private var dialogProfileCompletedBinding: DialogProfileBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNewProfileBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialogProfileCompletedBinding = null
        dialog = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setOnClickListener()
    }

    override fun initialize() {
        if (args.name != "") {
            binding.textInputEditTextFullName.setText(args.name)
        }

        if (args.phoneNo != "") {
            binding.textInputEditTextPhoneNo.setText(args.phoneNo)
            binding.textInputEditTextPhoneNo.isEnabled = false
            binding.textInputEditTextPhoneNo.isClickable = false
            binding.textInputEditTextPhoneNo.isFocusable = false
        }
        setOnBackPressedListener()
    }

    override fun setObserver() {
        viewModel.createNewProfileResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    enableUserTouch()
                    it.value.data?.let { loginResponse ->
                        lifecycleScope.launch {
                            viewModel.saveLoggedInUser(loginResponse, loginResponse.token)
                            delay(2000)
                            binding.progress.hide()
                            requireActivity().runOnUiThread {
                                showAccountCreatedMessageBox()
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(it.message.toString())
                    enableUserTouch()
                }

                is Resource.Loading -> {}

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.buttonCreateProfile.setOnClickListener(this)
        binding.userProfileImageView.setOnClickListener(this)
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
                findNavController().navigate(CreateNewProfileFragmentDirections.actionCreateNewProfileFragmentToPreLoginFragment())
            }

            binding.buttonCreateProfile.id -> {
                hideKeyboard(requireActivity())
                createNewProfile()
                setObserver()

            }

            binding.userProfileImageView.id, binding.userPersonImageView.id -> {
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


    private fun createNewProfile() {

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
        viewModel.createNewProfileApi(
            name = fullName,
            phoneNo = phoneNo,
            profileImage = imagePart,
            description = description,
            lat = lat.toString(),
            long = long.toString(),
            address = address,
            token = args.token
        )
        disableUserTouch()
    }

    private fun setOnBackPressedListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(CreateNewProfileFragmentDirections.actionCreateNewProfileFragmentToPreLoginFragment())
                }
            })
    }

    private fun openMapDialogFragment() {
        val mapDialogFragment = MapDialog()
        mapDialogFragment.listener = this
        mapDialogFragment.show(childFragmentManager, "MapDialogFragment")
    }

    override fun onDataPassed(data: MapResponse) {
        binding.textInputEditTextLocation.setText(data.name)
        lat = data.latitude
        long = data.longitude
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

            dialogProfileCompletedBinding!!.imageViewCancel.gone()

            dialogProfileCompletedBinding!!.buttonContinue.setSafeOnClickListener {

                val bundle = Bundle()
                bundle.putString("userType", "New User")
                requireActivity().startNewActivity(
                    DashBoardActivity::class.java,
                    bundle
                )
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}