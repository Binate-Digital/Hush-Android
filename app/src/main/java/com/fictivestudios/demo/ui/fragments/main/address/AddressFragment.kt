package com.fictivestudios.demo.ui.fragments.main.address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.fictivestudios.demo.databinding.FragmentAddressBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.fragments.main.address.itemView.OnItemCLick
import com.fictivestudios.demo.ui.fragments.main.address.itemView.RowItemAddress
import com.fictivestudios.demo.utils.PaginationScrollListener
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddressFragment : BaseFragment(R.layout.fragment_address), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener, OnItemCLick {

    private var _binding: FragmentAddressBinding? = null
    val binding
        get() = _binding!!
    val viewModel: AddressViewModel by viewModels()
    private var isToastShow = false

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
        _binding = FragmentAddressBinding.inflate(inflater)
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
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
        getAllContactList()


    }

    override fun setOnClickListener() {
        binding.cardViewCreateNewAddress.setOnClickListener(this)
        binding.imageViewClearText.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        setTextWatcher(binding.textInputEditTextSearch)
    }

    override fun setObserver() {
        viewModel.contactListResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is Resource.Success -> {
                    if (!isToastShow) {
                        requireActivity().runOnUiThread { showToast(it.value.message) }
                        isToastShow = true
                    }
                    viewTypeArray.clear()
                    it.value.data?.let { data ->
                        if (data.contacts.isNotEmpty()) {
                            for (contactData in data.contacts) {
                                viewTypeArray.add(RowItemAddress(contactData, this))
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
                    if (viewModel.page > 1) {
                        showToast("Loading more items...")
                    }
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
                viewModel.loadNextPage()
            }
        })
    }

    override fun onRefresh() {
        viewModel.getAllContactListApi(0)
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.cardViewCreateNewAddress.id -> {
                findNavController().navigate(AddressFragmentDirections.actionAddressFragmentToCreateNewContactFragment())
            }
            binding.imageViewClearText.id->{
                binding.textInputEditTextSearch.text?.clear()
            }
        }
    }

    private fun setTextWatcher(otpChildEditText: EditText) {
        otpChildEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    getAllContactList(s.toString())
                }

            }
        })
    }

    private fun getAllContactList(query: String = viewModel.searchQuery) {
        viewModel.getAllContactListApi(0, query)
    }

    override fun onClick(id: String) {
        viewModel.searchQuery = ""
        binding.textInputEditTextSearch.text?.clear()
        findNavController().navigate(
            AddressFragmentDirections.actionAddressFragmentToUserProfileFragment(
                id
            )
        )
    }
}