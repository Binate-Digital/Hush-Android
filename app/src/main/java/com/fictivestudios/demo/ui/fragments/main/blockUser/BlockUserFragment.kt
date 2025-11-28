package com.fictivestudios.demo.ui.fragments.main.blockUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.GenericListAdapter
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.databinding.FragmentBlockUserBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.fragments.main.blockUser.itenView.OnItemClick
import com.fictivestudios.demo.ui.fragments.main.blockUser.itenView.RowItemBlockUser
import com.fictivestudios.demo.utils.PaginationScrollListener
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockUserFragment : BaseFragment(R.layout.fragment_block_user), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener, OnItemClick {

    private var _binding: FragmentBlockUserBinding? = null
    val binding
        get() = _binding!!

    private var isItemClick = true
    private var viewTypeArray = ArrayList<ViewType<*>>()
    val viewModel: BlockUserViewModel by viewModels()

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
        _binding = FragmentBlockUserBinding.inflate(inflater)
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
        viewModel.getBlockUserList()
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
    }

    override fun setOnClickListener() {
        binding.imageViewBack.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun setObserver() {
        viewModel.blockUserListResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is Resource.Success -> {
                    viewTypeArray.clear()
                    it.value.data?.let { data ->
                        if (data.isNotEmpty()) {

                            for (blockData in data) {
                                viewTypeArray.add(RowItemBlockUser(blockData, this))
                            }
                            adapter.items = viewTypeArray
                        }


                        lifecycleScope.launch {
                            delay(500)
                            _binding?.let { binding ->
                                if (adapter.items.size == 0) {
                                    binding.multiStateView.viewState =
                                        MultiStateView.ViewState.EMPTY
                                } else {
                                    binding.multiStateView.viewState =
                                        MultiStateView.ViewState.CONTENT
                                }
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message.toString())
                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    } else {
                        val imageView = binding.multiStateView.getView(
                            MultiStateView.ViewState.ERROR
                        )?.findViewById<AppCompatImageView>(R.id.imageViewError)
                        if (it.isNetworkError) {
                            imageView?.setImageResource(R.drawable.ic_error)
                        } else {
                            imageView?.setImageResource(R.drawable.ic_no_data)
                        }
                        binding.multiStateView.viewState = MultiStateView.ViewState.ERROR
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
        viewModel.blockUserResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    showToast(data.value.message)
                    binding.progress.hide()
                    viewModel.getBlockUserList()
                }

                is Resource.Failure -> {
                    showToast(data.message.toString())
                    binding.progress.hide()
                }

                is Resource.Loading -> {
                }

                else -> {}
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

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }
        }
    }

    override fun onRefresh() {
        viewModel.getBlockUserList()
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING

    }

    override fun onClick(id: String) {
        viewModel.blockUser(id)
        binding.progress.show()
    }
}