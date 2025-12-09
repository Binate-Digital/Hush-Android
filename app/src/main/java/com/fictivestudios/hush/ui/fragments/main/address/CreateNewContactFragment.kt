package com.fictivestudios.hush.ui.fragments.main.address

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.DialogProfileBinding
import com.fictivestudios.hush.databinding.FragmentNewContactBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.utils.AttachmentTypeEnum
import com.fictivestudios.hush.utils.gallery.GalleryBottomSheet
import com.fictivestudios.hush.utils.gallery.OnImageSelect
import com.fictivestudios.hush.utils.setSafeOnClickListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreateNewContactFragment : BaseFragment(R.layout.fragment_new_contact), View.OnClickListener {

    private var _binding: FragmentNewContactBinding? = null
    val binding
        get() = _binding!!
    val viewModel: AddressViewModel by viewModels()
    private var bottomSheet: GalleryBottomSheet? = null
    private var contactImage: File? = null

    private var dialogProfileCompletedBinding: DialogProfileBinding? = null
    private var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewContactBinding.inflate(inflater)
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
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        binding.ccp.registerCarrierNumberEditText(binding.textInputEditTextPhoneNo)
        binding.ccp.setNumberAutoFormattingEnabled(true)
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.createButton.setOnClickListener(this)
        binding.userPersonImageView.setOnClickListener(this)
        binding.cameraImageView.setOnClickListener(this)

    }

    override fun setObserver() {
        viewModel.createContactResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.progress.hide()
                    it.value.data?.let { loginResponse ->
                        binding.progress.hide()
                        enableUserTouch()
                        showAccountCreatedMessageBox()

                    }
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                    binding.progress.show()
                }

                else -> {}
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

            binding.userPersonImageView.id, binding.cameraImageView.id -> {
                bottomSheet = GalleryBottomSheet(

                    AttachmentTypeEnum.IMAGE.getValue(),
                    object : OnImageSelect {
                        override fun onSelectImageFromSystem(
                            imageUri: Uri?,
                            imageLink: File?
                        ) {
                            binding.cameraImageView.gone()
                            binding.userPersonImageView.setImageURI(imageUri)
                            contactImage = imageLink
                            bottomSheet?.dismiss()
                        }
                    })
                bottomSheet!!.show(requireActivity().supportFragmentManager, bottomSheet!!.tag)
            }

            binding.createButton.id -> {

                createContact()

            }
        }
    }

    private fun createContact() {
        val fullName = binding.textInputEditTextFullName.text.toString().trim()
        val lastName = binding.textInputEditTextLastName.text.toString().trim()
        val notes = binding.textInputEditTextNotes.text.toString().trim()

        if (fullName.isEmpty()) {
            showToast("Full name field can't be empty.")
            return
        }
        if (lastName.isEmpty()) {
            showToast("Last name field can't be empty.")
            return
        }
        if (binding.textInputEditTextPhoneNo.text.toString().trim().isEmpty()) {
            showToast("Phone Number field can't be empty")
            return
        }

        if (!binding.ccp.isValidFullNumber) {
            showToast("Please enter a valid Phone Number")
            return
        }


        val requestFile = contactImage?.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = (if (contactImage == null) null else requestFile!!)?.let {
            MultipartBody.Part.createFormData(
                "contactImage", contactImage.toString(),
                it
            )
        }

        viewModel.createContactApi(fullName, lastName, binding.ccp.fullNumberWithPlus, notes, imagePart)
        binding.progress.show()
        disableUserTouch()
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
            dialog.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            dialog.setView(dialogProfileCompletedBinding!!.root)
            dialog.setCancelable(false)
            dialogProfileCompletedBinding?.textViewDes?.text =
                getString(R.string.contact_has_been_created_successfully)

            dialogProfileCompletedBinding!!.imageViewCancel.gone()

            dialogProfileCompletedBinding!!.buttonContinue.setSafeOnClickListener {
                findNavController().popBackStack()
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}