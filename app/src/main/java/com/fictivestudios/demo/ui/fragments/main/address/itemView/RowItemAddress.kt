package com.fictivestudios.demo.ui.fragments.main.address.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.Contacts
import com.fictivestudios.demo.databinding.RowItemAddressBinding
import com.fictivestudios.demo.utils.Constants
import com.fictivestudios.demo.utils.showToast

class RowItemAddress(
    private val data: Contacts, private val listener: OnItemCLick
) : ViewType<Contacts> {

    override fun layoutId(): Int {
        return R.layout.row_item_address
    }

    override fun data(): Contacts {
        return data
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemAddressBinding).also { binding ->
            binding.textViewUserName.text = data.fname + " " + data.lname
            val url = Constants.IMAGE_BASE_URL + data.contactImage
            if (data.contactImage == null) {
                binding.imageViewUser.setImageResource(R.drawable.person)
            } else {
                Glide.with(binding.imageViewUser.context)
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.imageViewUser)

            }

            binding.imageViewCall.setOnClickListener {
                showToast(binding.imageViewCall.context, "Will be implement in next phase")
            }
            binding.imageViewMessage.setOnClickListener {
                showToast(binding.imageViewCall.context, "Will be implement in next phase")
            }
            binding.cardViewUser.setOnClickListener {
                listener.onClick(data._id)

            }
        }
    }

}

fun interface OnItemCLick {
    fun onClick(id: String)
}
