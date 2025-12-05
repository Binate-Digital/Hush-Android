package com.fictivestudios.hush.ui.fragments.main.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.GenericListAdapter
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.data.responses.ChatInbox
import com.fictivestudios.hush.databinding.FragmentChatBinding
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemChatList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatFragment : BaseFragment(R.layout.fragment_chat), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentChatBinding? = null
    val binding
        get() = _binding!!
    val viewModel: ChatViewModel by viewModels()

    private var isItemClick = true
    private var viewTypeArray = ArrayList<ViewType<*>>()

    private val adapter by lazy {
        GenericListAdapter(object : OnItemClickListener<ViewType<*>> {
            override fun onItemClicked(view: View, item: ViewType<*>, position: Int) {
                lifecycleScope.launch {
                    item.data()?.let { data ->
                        (data as ChatInbox).also {
                            if (isItemClick) {
                                isItemClick = false
                                data.id?.let{
                                    findNavController().navigate(ChatFragmentDirections.actionChatFragmentToChatDetailFragment(it))
                                }
                            }
                            delay(1000)
                            isItemClick = true
                        }
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater)
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
        viewModel.getUserData()
    }

    override fun setOnClickListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun setObserver() {
        lifecycleScope.launch {
            viewModel.messages.collect { list ->
                viewTypeArray.clear()
                for (data in list) {
                    viewTypeArray.add(RowItemChatList(data))
                }
                adapter.items = viewTypeArray
                Log.d("Socket IO Message", "$list")
            }
        }
    }

    private fun setRecyclerView() {
        binding.recyclerView.adapter = adapter
//        val linearLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
//        binding.recyclerView.addOnScrollListener(object :
//            PaginationScrollListener(linearLayoutManager) {
//            override fun onScrolled(dy: Int) {
//            }
//
//            override fun loadMoreItems() {
//                // viewModel.loadNextPage()
//            }
//        })
    }

//    override fun onClick() {
//        findNavController().navigate(ChatFragmentDirections.actionChatFragmentToUserProfileFragment(""))
//    }

    override fun onRefresh() {
        binding.swipeRefreshLayout.isRefreshing = false
    }
}