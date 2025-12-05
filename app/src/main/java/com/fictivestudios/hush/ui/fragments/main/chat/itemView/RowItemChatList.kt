package com.fictivestudios.hush.ui.fragments.main.chat.itemView

import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.data.responses.ChatInbox
import com.fictivestudios.hush.databinding.RowItemChatListBinding
import com.fictivestudios.hush.utils.Constants
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RowItemChatList(
    private val data: ChatInbox
) : ViewType<ChatInbox> {

    override fun layoutId(): Int {
        return R.layout.row_item_chat_list
    }

    override fun data(): ChatInbox {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemChatListBinding).also { binding ->
            binding.textViewUserName.text = data.contactName
            binding.textViewLastMessage.text = data.lastMessage
            binding.textViewTimeStamp.text = data.lastMessageAt?.let{formatIsoSmart(it)}
            Glide.with(binding.imageViewUser.context)
                .load(Constants.IMAGE_BASE_URL + data.contactImage).placeholder(R.drawable.person)
                .into(binding.imageViewUser)
        }
    }

    fun formatIsoSmart(isoTime: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoTime)
            val now = LocalDate.now(ZoneId.systemDefault())
            val messageDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

            if (messageDate.isEqual(now)) {
                // Today → show only time
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
                zonedDateTime.format(timeFormatter)
            } else {
                // Not today → show full date + time
                val fullFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                zonedDateTime.format(fullFormatter)
            }
        } catch (e: Exception) {
            isoTime
        }
    }


}

