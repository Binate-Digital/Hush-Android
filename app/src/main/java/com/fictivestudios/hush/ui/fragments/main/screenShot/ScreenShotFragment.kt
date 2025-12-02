package com.fictivestudios.hush.ui.fragments.main.screenShot

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
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
import com.fictivestudios.hush.data.responses.Screenshots
import com.fictivestudios.hush.databinding.DialogDeleteItemBinding
import com.fictivestudios.hush.databinding.FragmentScreenShotBinding
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.ui.fragments.main.screenShot.itemView.OnItemClick
import com.fictivestudios.hush.ui.fragments.main.screenShot.itemView.RowItemScreenShot
import com.fictivestudios.hush.utils.PaginationScrollListener
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScreenShotFragment : BaseFragment(R.layout.fragment_screen_shot), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener, OnItemClick {

    private var _binding: FragmentScreenShotBinding? = null
    val binding
        get() = _binding!!

    private var viewTypeArray = ArrayList<ViewType<*>>()
    val viewModel: ScreenShotViewModel by viewModels()

    val selectedItemList = arrayListOf<String>()
    var isMultipleItemSelection = false

    private var dialogDeleteItemBinding: DialogDeleteItemBinding? = null
    private var dialog: AlertDialog? = null

    private val adapter by lazy {
        GenericListAdapter(object : OnItemClickListener<ViewType<*>> {
            override fun onItemClicked(view: View, item: ViewType<*>, position: Int) {
                item.data()?.let { data ->
                    (data as Screenshots).also {
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
        _binding = FragmentScreenShotBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog = null
        dialogDeleteItemBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObserver()
        setOnClickListener()
    }

    override fun initialize() {
        setOnBackPressedListener()
        setRecyclerView()
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
        viewModel.getAllScreenShotApi(1)

    }

    override fun setOnClickListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.imageViewBack.setOnClickListener(this)
        binding.imageViewCancel.setOnClickListener(this)
        binding.imageViewDelete.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.imageViewBack.id -> {
                findNavController().popBackStack()
            }

            binding.imageViewCancel.id -> {
                binding.imageViewBack.show()
                binding.imageViewCancel.gone()
                binding.imageViewDelete.gone()
                selectedItemList.clear()
                for (data in viewModel.screenShotArray) {
                    data.isMultipleSelectionEnabled = false
                    data.isSelected = false
                }
                adapter.notifyDataSetChanged()
            }

            binding.imageViewDelete.id -> {
                showDeleteAccountMessageBox()
            }
        }
    }

    override fun onRefresh() {
        binding.imageViewBack.show()
        binding.imageViewCancel.gone()
        binding.imageViewDelete.gone()
        selectedItemList.clear()
        viewTypeArray.clear()
        adapter.notifyDataSetChanged()

        viewModel.getAllScreenShotApi(1)
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
    }


    override fun setObserver() {
        viewModel.deleteScreenShotResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    enableUserTouch()
                    binding.imageViewCancel.gone()
                    binding.imageViewDelete.gone()
                    binding.imageViewBack.show()
                    lifecycleScope.launch {
                        delay(1500)
                        viewModel.getAllScreenShotApi(1)
                        requireActivity().runOnUiThread {
                            showToast(data.value.message)
                        }
                        binding.progress.hide()
                    }
                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    binding.imageViewCancel.gone()
                    binding.imageViewDelete.gone()
                    binding.imageViewBack.show()
                    for (data in viewModel.screenShotArray) {
                        data.isSelected = false
                        data.isMultipleSelectionEnabled = false
                    }
                    adapter.notifyDataSetChanged()
                    requireActivity().runOnUiThread {
                        showToast(data.message.toString())
                    }
                    enableUserTouch()
                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.screenShotResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is Resource.Success -> {
                    enableUserTouch()
                    showToast(it.value.message)
                    viewTypeArray.clear()
                    for (data in it.value.data?.screenshots ?: arrayListOf()) {
                        viewTypeArray.add(
                            RowItemScreenShot(
                                data, this
                            )
                        )
                    }
                    adapter.items = viewTypeArray

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

                is Resource.Failure -> {

                    showToast(it.message.toString())
                    val imageView = binding.multiStateView.getView(
                        MultiStateView.ViewState.ERROR
                    )?.findViewById<AppCompatImageView>(R.id.imageViewError)
                    if (it.isNetworkError) {
                        imageView?.setImageResource(R.drawable.ic_error)
                    } else {
                        imageView?.setImageResource(R.drawable.ic_no_data)
                    }
                    binding.multiStateView.viewState = MultiStateView.ViewState.ERROR

                    if (it.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
//                    if (viewModel.page > 1) {
//
//                    }
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


    private fun setOnBackPressedListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    override fun invokeMultipleSelection() {
        for (data in viewModel.screenShotArray) {
            data.isMultipleSelectionEnabled = true
            data.isSelected = false
        }
        binding.imageViewBack.gone()
        binding.imageViewCancel.show()
        binding.imageViewDelete.show()
        adapter.notifyDataSetChanged()
    }

    override fun onItemSelected(data: Screenshots) {
        selectedItemList.add(data._id ?: "")
    }

    override fun onClick(data: Screenshots) {
        if (!isMultipleItemSelection) {
            findNavController().navigate(
                ScreenShotFragmentDirections.actionScreenShotFragmentToImagePreviewFragment(
                    data.image ?: "", "screenShot", data._id ?: ""
                )
            )
        }

    }

    override fun onItemRemove(data: Screenshots) {
        selectedItemList.remove(data._id ?: "")
        if (selectedItemList.isEmpty()) {
            binding.imageViewBack.show()
            binding.imageViewCancel.gone()
            binding.imageViewDelete.gone()
            for (data in viewModel.screenShotArray) {
                data.isMultipleSelectionEnabled = false
                data.isSelected = false
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun showDeleteAccountMessageBox() {
        dialog = AlertDialog.Builder(requireContext()).create()
        dialogDeleteItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_delete_item,
            null,
            false
        )
        dialog?.let { dialog ->
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setView(dialogDeleteItemBinding!!.root)
            dialog.setCancelable(false)


            dialogDeleteItemBinding!!.buttonCancel.text = "Cancel"
            dialogDeleteItemBinding!!.buttonDelete.text = "Delete"
            dialogDeleteItemBinding!!.buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogDeleteItemBinding!!.imageViewCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialogDeleteItemBinding!!.buttonDelete.setOnClickListener {
                viewModel.deleteScreenShotByIds(selectedItemList)
                binding.progress.show()
                disableUserTouch()
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}