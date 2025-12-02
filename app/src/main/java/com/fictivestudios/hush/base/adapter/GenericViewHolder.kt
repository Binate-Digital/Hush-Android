package com.fictivestudios.hush.base.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


class GenericViewHolder<E : ViewType<*>>(
    private val bi: ViewDataBinding,
    private val onClickListener: OnItemClickListener<E>,
) : RecyclerView.ViewHolder(bi.root) {

    fun bind(item: ViewType<*>, position: Int) {
        item.bind(bi, position, onClickListener)
        bi.apply {
            bi.setVariable(BR.itemViewModel, item.data())
            if (item.isUserInteractionEnabled()) {

                bi.setVariable(BR.position, position)
                bi.setVariable(BR.viewItemType, item)
                bi.setVariable(BR.actionItemListener, onClickListener)
            }
            bi.executePendingBindings()
        }
    }

    class ChildViewDiffUtils<E : ViewType<*>>(
        private val oldList: ArrayList<E>,
        private val newList: ArrayList<E>
    ) :
        DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].data().toString() == newList[newItemPosition].data()
                .toString()
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].data() == newList[newItemPosition].data()
        }
    }
}