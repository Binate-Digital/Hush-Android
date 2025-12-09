package com.fictivestudios.hush.ui.fragments.main.chat.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.data.responses.Message
import com.fictivestudios.hush.data.responses.SmsMessage
import com.fictivestudios.hush.databinding.RowItemMyMessageBinding
import com.fictivestudios.hush.utils.Constants

class RowItemMyChat (
    private val data: SmsMessage,
    private val image: String,
) : ViewType<SmsMessage> {

    override fun layoutId(): Int {
        return R.layout.row_item_my_message
    }

    override fun data(): SmsMessage {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemMyMessageBinding).also { binding ->
//            binding.imageViewUser.setImageResource(data.userImage)
            binding.textViewMessage.text = data.message
            Glide.with(binding.imageViewUser.context)
                .load(Constants.IMAGE_BASE_URL + image)
                .placeholder(R.drawable.person)
                .into(binding.imageViewUser)
        }
    }

}
