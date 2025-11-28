package com.fictivestudios.demo.ui.fragments.main.screenShot.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.Screenshots
import com.fictivestudios.demo.databinding.RowItemScreenShotBinding
import com.fictivestudios.demo.utils.Constants
import com.fictivestudios.demo.utils.convertUtcToLocalPsk
import com.fictivestudios.demo.utils.gone
import com.fictivestudios.demo.utils.show


class RowItemScreenShot(
    private val data: Screenshots, private val listener: OnItemClick
) : ViewType<Screenshots> {

    override fun layoutId(): Int {
        return R.layout.row_item_screen_shot
    }

    override fun data(): Screenshots {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemScreenShotBinding).also { binding ->
            if (data.isMultipleSelectionEnabled) {
                binding.radioButton.show()
            } else {
                binding.radioButton.gone()
            }

            binding.radioButton.isChecked = data.isSelected
            binding.textViewDate.text = data.date
            binding.textViewTime.text = data.createdAt?.let { convertUtcToLocalPsk(it) }

            val url = Constants.IMAGE_BASE_URL + data.image
            if (data.image == null) {
                binding.imageViewScreenShot.setImageResource(R.drawable.person)
            } else {
                Glide.with(binding.imageViewScreenShot.context)
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.imageViewScreenShot)
            }

            binding.materialCardViewMain.setOnLongClickListener {
                listener.invokeMultipleSelection()
                true
            }

            binding.materialCardViewMain.setOnClickListener {
                if (data.isMultipleSelectionEnabled) {
                    if (!data.isSelected) {
                        listener.onItemSelected(data)
                        data.isSelected = true
                    } else {
                        listener.onItemRemove(data)
                        data.isSelected = false
                    }
                    binding.radioButton.isChecked = data.isSelected

                } else {
                    listener.onClick(data)
                }
            }

            binding.radioButton.setOnClickListener {
                if (data.isMultipleSelectionEnabled) {
                    if (!data.isSelected) {
                        listener.onItemSelected(data)
                        data.isSelected = true
                    } else {
                        listener.onItemRemove(data)
                        data.isSelected = false
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
    fun invokeMultipleSelection()
    fun onItemSelected(data: Screenshots)
    fun onClick(data: Screenshots)
    fun onItemRemove(data: Screenshots)

}
