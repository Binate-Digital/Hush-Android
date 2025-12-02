package com.fictivestudios.hush.ui.fragments.main.notification.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.data.responses.NotificationResponse
import com.fictivestudios.hush.databinding.RowItemNotificationBinding
import com.fictivestudios.hush.utils.convertUtcToLocalPsk

class RowItemNotification(
    private val data: NotificationResponse
) : ViewType<NotificationResponse> {

    override fun layoutId(): Int {
        return R.layout.row_item_notification
    }

    override fun data(): NotificationResponse {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemNotificationBinding).also { binding ->
            binding.textViewDate.text = convertUtcToLocalPsk(data.createdAt)
            binding.textViewTitle.text = data.title
            binding.textViewDescription.text = data.body
        }
    }

}
