package com.fictivestudios.demo.ui.fragments.main.callLog.itemView

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.CallLogResponse
import com.fictivestudios.demo.databinding.RowItemCallLogBinding
import com.fictivestudios.demo.utils.formatDateToTime

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
