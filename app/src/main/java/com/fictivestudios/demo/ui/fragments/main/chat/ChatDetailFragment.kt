package com.fictivestudios.demo.ui.fragments.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.GenericListAdapter
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.databinding.FragmentChatDetailBinding
import com.fictivestudios.demo.ui.activities.CallActivity
import com.fictivestudios.demo.ui.fragments.main.chat.itemView.RowItemMyChat
import com.fictivestudios.demo.ui.fragments.main.chat.itemView.RowItemOtherChat
import com.fictivestudios.demo.utils.PaginationScrollListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatDetailFragment : BaseFragment(R.layout.fragment_chat_detail), View.OnClickListener {

    private var _binding: FragmentChatDetailBinding? = null
    val binding
        get() = _binding!!
    val viewModel: ChatViewModel by viewModels()

    private var isItemClick = true
    private var viewTypeArray = ArrayList<ViewType<*>>()

    private val adapter by lazy {
        GenericListAdapter(object : OnItemClickListener<ViewType<*>> {
            override fun onItemClicked(view: View, item: ViewType<*>, position: Int) {
                lifecycleScope.launch {
                    if (isItemClick) {
                        isItemClick = false
                        item.data()?.let { data ->

                        }
                    }
                    delay(1000)
                    isItemClick = true
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        setRecyclerView()
    }


    override fun setObserver() {
        viewTypeArray.clear()
        for (data in viewModel.messageList) {
            if (data.id == 1) {
                viewTypeArray.add(RowItemMyChat(data))
            } else {
                viewTypeArray.add(RowItemOtherChat(data))
            }
        }
        adapter.items = viewTypeArray
    }

    override fun setOnClickListener() {
        binding.imageViewCall.setOnClickListener(this)
        binding.imageViewBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewCall.id -> {
                val intent = Intent(requireContext(), CallActivity::class.java)
                requireActivity().startActivity(intent)
            }

            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }
        }
    }


    private fun setRecyclerView() {
        binding.recyclerView.adapter = adapter
        val linearLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        binding.recyclerView.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun onScrolled(dy: Int) {
            }

            override fun loadMoreItems() {
                // viewModel.loadNextPage()
            }
        })
    }


}