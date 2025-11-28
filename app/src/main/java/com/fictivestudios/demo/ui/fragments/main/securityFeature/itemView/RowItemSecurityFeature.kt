package com.fictivestudios.demo.ui.fragments.main.securityFeature.itemView

import android.annotation.SuppressLint
import android.util.Log
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.Attachments
import com.fictivestudios.demo.databinding.RowItemSecurityFeatureBinding
import com.fictivestudios.demo.utils.Constants
import com.fictivestudios.demo.utils.gone
import com.fictivestudios.demo.utils.show


class RowItemSecurityFeature(
    private val data: Attachments, private val listener: OnItemClick, private val fragment: Fragment
) : ViewType<Attachments> {

    override fun layoutId(): Int {
        return R.layout.row_item_security_feature
    }

    override fun data(): Attachments {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemSecurityFeatureBinding).also { binding ->
            if (data.isMultipleSelectionEnabled) {
                binding.radioButton.show()
            } else {
                binding.radioButton.gone()
            }

            binding.radioButton.isChecked = data.isSelected
            if (data.type == "image") {
                binding.imageViewPause.gone()
                val url = Constants.IMAGE_BASE_URL + data.file
                Log.d("imageUrl", url)
                if (data.file == null) {
                    binding.imageViewScreenShot.setImageResource(R.drawable.place_holder)
                } else {
                    //loadImageByUrl(imageView = binding.imageViewScreenShot, imageLink = url)
                    Glide.with(binding.imageViewScreenShot.context)
                        .load(url).placeholder(R.drawable.person)
                        .into(binding.imageViewScreenShot)
                }
            } else {
                val url = Constants.IMAGE_BASE_URL + data.thumbnail
                binding.imageViewPause.show()
                if (data.thumbnail == null) {
                    binding.imageViewScreenShot.setImageResource(R.drawable.place_holder)
                } else {
                   // loadImageByUrl(imageView = binding.imageViewScreenShot, imageLink = url)
                    Glide.with(binding.imageViewScreenShot.context)
                        .load(url).placeholder(R.drawable.person)
                        .into(binding.imageViewScreenShot)
                }
            }

            binding.materialCardViewMain.setOnLongClickListener {
                listener.invokeMultipleSelection(data)
                data.isSelected = true
                binding.radioButton.isChecked = data.isSelected
                true
            }

            binding.materialCardViewMain.setOnClickListener {
                if (data.isMultipleSelectionEnabled) {
                    if (!data.isSelected) {
                        data.isSelected = true
                        listener.onItemSelected(data)
                    } else {
                        data.isSelected = false
                        listener.onItemRemove(data)
                    }
                    binding.radioButton.isChecked = data.isSelected
                } else {
                    listener.onClick(data)
                }
            }
            binding.radioButton.setOnClickListener {
                if (data.isMultipleSelectionEnabled) {
                    if (!data.isSelected) {
                        data.isSelected = true
                        listener.onItemSelected(data)
                    } else {
                        data.isSelected = false
                        listener.onItemRemove(data)
                    }
                    binding.radioButton.isChecked = data.isSelected
                } else {
                    listener.onClick(data)
                }
            }
        }
    }
}

interface OnItemClick {
    fun invokeMultipleSelection(data: Attachments)
    fun onItemSelected(data: Attachments)
    fun onClick(data: Attachments)
    fun onItemRemove(data: Attachments)

}
