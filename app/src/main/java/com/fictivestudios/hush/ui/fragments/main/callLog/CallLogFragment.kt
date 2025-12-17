package com.fictivestudios.hush.ui.fragments.main.callLog

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
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.GenericListAdapter
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.base.response.Resource
import com.fictivestudios.hush.databinding.FragmentCallLogBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.ui.fragments.main.callLog.itemView.RowItemCallLog
import com.fictivestudios.hush.utils.PaginationScrollListener
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CallLogFragment : BaseFragment(R.layout.fragment_call_log), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentCallLogBinding? = null
    val binding
        get() = _binding!!


    val viewModel: CallLogViewModel by viewModels()
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
        _binding = FragmentCallLogBinding.inflate(inflater)
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
        viewModel.init {
            it?.let {data->
                viewModel.getAllCallLogs(data.phone!!)
            }
        }

        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
    }

    override fun setObserver() {
        viewModel.callLogResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is Resource.Success -> {
                    requireActivity().runOnUiThread { showToast(it.value.message) }
                    viewTypeArray.clear()
                    it.value.data?.let { data ->
                        if (data.isNotEmpty()) {
                            for (callLogData in data) {
                                viewTypeArray.add(RowItemCallLog(callLogData))
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
                    if (it.message.toString() != "No Contacts found.") {
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
                    if (it.errorCode == 401) {
                        lifecycleScope.launch {
                            (requireActivity() as DashBoardActivity).logout()
                        }
                    }
                }

                is Resource.Loading -> {
                    binding.multiStateView.viewState =
                        MultiStateView.ViewState.LOADING
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.imageViewBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

        }
    }

    override fun onRefresh() {
        viewModel.init {
           it?.let {data->
               viewModel.getAllCallLogs(data.phone!!)
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
            }
        })
    }
}