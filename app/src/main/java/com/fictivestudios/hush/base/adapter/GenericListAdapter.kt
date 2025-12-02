package com.fictivestudios.hush.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView


interface OnItemClickListener<E> {
    fun onItemClicked(view: View, item: E, position: Int)
}

class GenericListAdapter<E : ViewType<*>> internal constructor(
    private val onClickListener: OnItemClickListener<E>
) : RecyclerView.Adapter<GenericViewHolder<E>>() {

    var items: ArrayList<E> = ArrayList()
        set(value) {
            field = value
            val diffCallback =
                GenericViewHolder.ChildViewDiffUtils(filteredItems, items)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            diffResult.dispatchUpdatesTo(this)
            if (filteredItems.size > 0)
                filteredItems.clear()
            filteredItems.addAll(value)
        }

    private var filteredItems: ArrayList<E> = ArrayList()
        set(value) {
            field = value
            val diffCallback =
                GenericViewHolder.ChildViewDiffUtils(filteredItems, items)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            diffResult.dispatchUpdatesTo(this)
        }

    fun clearItems() {
        items.clear()

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GenericViewHolder<E> {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, i, viewGroup, false)
        return GenericViewHolder(binding, onClickListener)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<E>, position: Int) {
        holder.bind(filteredItems[position], position)
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun getItemViewType(position: Int) = items[position].layoutId()

}
