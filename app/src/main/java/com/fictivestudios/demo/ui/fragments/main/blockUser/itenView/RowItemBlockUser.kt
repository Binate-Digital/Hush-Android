package com.fictivestudios.demo.ui.fragments.main.blockUser.itenView

import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.UserBlockList
import com.fictivestudios.demo.databinding.RowItemBlockUserBinding
import com.fictivestudios.demo.utils.Constants

class RowItemBlockUser(
    private val data: UserBlockList,private val listener: OnItemClick
) : ViewType<UserBlockList> {

    override fun layoutId(): Int {
        return R.layout.row_item_block_user
    }

    override fun data(): UserBlockList {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemBlockUserBinding).also { binding ->
            binding.textViewUserName.text = data.name

            if (data.contactImage == null) {
                binding.imageViewUser.setImageResource(R.drawable.person)
            } else {
                val url = Constants.IMAGE_BASE_URL + data.contactImage
                Glide.with(binding.imageViewUser.context)
                    .load(url).placeholder(R.drawable.person)
                    .into(binding.imageViewUser)

            }

            binding.imageViewBlockUser.setOnClickListener {
                listener.onClick(data.contactId)
            }
        }
    }

}

fun interface OnItemClick{
    fun onClick(id:String)
}
