package com.fictivestudios.demo.utils

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fictivestudios.demo.R
import com.fictivestudios.demo.databinding.BlockUserBottomDialogBinding
import com.fictivestudios.demo.databinding.DialogDeleteAccountBinding
import com.fictivestudios.demo.ui.fragments.main.profile.ApiResponse
import com.fictivestudios.demo.ui.fragments.main.profile.ProfileActionTypeEnum
import com.fictivestudios.demo.ui.fragments.main.profile.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BlockUserBottomSheet : BottomSheetDialogFragment(), ApiResponse {

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var binding: BlockUserBottomDialogBinding
    private var dialogBinding: DialogDeleteAccountBinding? = null
    var userId: String = ""
    var alertDialog: AlertDialog? = null
    private lateinit var viewModel: ProfileViewModel
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
        val view = View.inflate(context, R.layout.block_user_bottom_dialog, null)
        //binding views to data binding.
        binding = DataBindingUtil
            .bind<BlockUserBottomDialogBinding>(view) as BlockUserBottomDialogBinding
        //setting layout with bottom sheet
        bottomSheet.setContentView(view)


        val fragment: Fragment =
            parentFragmentManager.fragments[0].childFragmentManager.fragments[0]
        viewModel = ViewModelProvider(fragment)[ProfileViewModel::class.java]


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
        viewModel.listener = this
        return bottomSheet
    }

    private fun setOnClickListener() {
        binding.reportView.setSafeOnClickListener {
            showDialog(
                UserOptionEnum.REPORT.getValue()
            )
        }
        binding.blockView.setSafeOnClickListener {
            showDialog(
                UserOptionEnum.BLOCK.getValue()
            )
        }
        binding.deleteView.setSafeOnClickListener {
            showDialog(
                UserOptionEnum.DELETE.getValue()
            )
        }
    }

    private fun showDialog(type: String) {
        // Build and show the alert dialog
        alertDialog = AlertDialog.Builder(requireContext()).create()
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_delete_account,
            null,
            false
        )
        alertDialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogBinding!!.root)
            dialog.setCancelable(false)
            when (type) {
                UserOptionEnum.REPORT.getValue() -> {
                    dialogBinding?.textViewActionTitle?.text = "Report User"
                    dialogBinding?.textViewDes?.text = "Are you sure you want to report this User?"
                    dialogBinding?.buttonDelete?.text = "Report"
                    dialogBinding?.textInputLayoutReason?.show()
                }

                UserOptionEnum.DELETE.getValue() -> {
                    dialogBinding?.textViewActionTitle?.text = "Delete User"
                    dialogBinding?.textViewDes?.text = "Are you sure you want to delete this User?"
                    dialogBinding?.buttonDelete?.text = "Delete"
                    dialogBinding?.textInputLayoutReason?.gone()
                }

                UserOptionEnum.BLOCK.getValue() -> {
                    dialogBinding?.textViewActionTitle?.text = "Block User"
                    dialogBinding?.textViewDes?.text = "Are you sure you want to block this User?"
                    dialogBinding?.buttonDelete?.text = "Block"
                    dialogBinding?.textInputLayoutReason?.gone()
                }
            }

            dialogBinding?.imageViewDelete?.gone()

            dialogBinding!!.buttonCancel.setSafeOnClickListener {
                dialog.dismiss()
            }

            dialogBinding!!.buttonDelete.setSafeOnClickListener {
                when (type) {
                    UserOptionEnum.REPORT.getValue() -> {
                        hideKeyboard(requireActivity())
                        viewModel.reportUser(
                            userId,
                            dialogBinding?.textInputEditTextReason?.text.toString().trim()
                        )
                    }

                    UserOptionEnum.DELETE.getValue() -> {
                        viewModel.deleteContactById(userId)
                    }

                    UserOptionEnum.BLOCK.getValue() -> {
                        viewModel.blockUser(userId)
                    }
                }
                dialogBinding?.progress?.show()
            }

            dialogBinding!!.imageViewCancel.setSafeOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun response(isApiSuccess: Boolean, userActionType: String) {
        when (userActionType) {
            ProfileActionTypeEnum.BLOCK.getValue(), ProfileActionTypeEnum.DELETE.getValue() -> {
                alertDialog?.dismiss()
                findNavController().popBackStack()
                dismissNow()
            }

            ProfileActionTypeEnum.REPORT.getValue() -> {
                dialogBinding?.progress?.hide()
                alertDialog?.dismiss()
            }
        }
    }
}