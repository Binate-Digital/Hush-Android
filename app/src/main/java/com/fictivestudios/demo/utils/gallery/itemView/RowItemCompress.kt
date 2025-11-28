package com.fictivestudios.demo.utils.gallery.itemView

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.databinding.ViewDataBinding
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.data.responses.Compress
import com.fictivestudios.demo.databinding.RowItemCompressBinding

class RowItemCompress(
    private val data: Compress
) : ViewType<Compress> {

    override fun layoutId(): Int {
        return R.layout.row_item_compress
    }

    override fun data(): Compress {
        return data
    }

    override fun isUserInteractionEnabled(): Boolean {
        return true
    }

    override fun bind(bi: ViewDataBinding, position: Int, onClickListener: OnItemClickListener<*>) {
        (bi as RowItemCompressBinding).also { binding ->
//            Glide.with(binding.imageViewThumbnail.context)
//                .load(data.uri).placeholder(R.drawable.person)
//                .into(binding.imageViewThumbnail)
            binding.imageViewThumbnail.setImageBitmap(generateVideoThumbnail(binding.imageViewThumbnail.context,data.uri))
            binding.textViewFileName.text = data.fileName
            binding.textViewVideoSize.text = data.newFileSize
            binding.progress.progress = data.progress.toInt()
        }
    }
    private fun generateVideoThumbnail(context: Context,videoUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        return retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }
}

