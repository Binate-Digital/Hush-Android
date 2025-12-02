package com.fictivestudios.hush.ui.fragments.main.callLog.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.data.responses.CallLogResponse
import com.fictivestudios.hush.databinding.RowItemCallLogBinding
import com.fictivestudios.hush.utils.formatDateToTime

class RowItemCallLog(
    private val data: CallLogResponse
) : ViewType<CallLogResponse> {

    override fun layoutId(): Int {
        return R.layout.row_item_call_log
    }

    override fun data(): CallLogResponse {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemCallLogBinding).also { binding ->
            binding.textViewDate.text = formatDateToTime(data.dateCreated)
            binding.textViewTitle.text = data.to
        }
    }

}
