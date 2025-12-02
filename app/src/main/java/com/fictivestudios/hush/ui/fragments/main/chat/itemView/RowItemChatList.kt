package com.fictivestudios.hush.ui.fragments.main.chat.itemView

import androidx.databinding.ViewDataBinding
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.data.responses.ChatListResponse
import com.fictivestudios.hush.databinding.RowItemChatListBinding

class RowItemChatList(
    private val data: ChatListResponse
) : ViewType<ChatListResponse> {

    override fun layoutId(): Int {
        return R.layout.row_item_chat_list
    }

    override fun data(): ChatListResponse {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemChatListBinding).also { binding ->
            binding.textViewUserName.text = data.userName
            binding.textViewLastMessage.text = data.lastMessage
            binding.textViewTimeStamp.text = data.timeStamp
            binding.imageViewUser.setImageResource(data.userImage)
        }
    }

}

