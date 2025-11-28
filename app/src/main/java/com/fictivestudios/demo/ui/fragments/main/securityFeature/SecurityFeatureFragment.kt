package com.fictivestudios.demo.ui.fragments.main.securityFeature

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.arezoonazer.player.argument.PlayerParams
import com.arezoonazer.player.extension.startPlayer
import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.adapter.GenericListAdapter
import com.fictivestudios.demo.base.adapter.OnItemClickListener
import com.fictivestudios.demo.base.adapter.ViewType
import com.fictivestudios.demo.base.fragment.BaseFragment
import com.fictivestudios.demo.base.response.Resource
import com.fictivestudios.demo.data.responses.Attachments
import com.fictivestudios.demo.databinding.DialogDeleteItemBinding
import com.fictivestudios.demo.databinding.FragmentSecurityFeatureBinding
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import com.fictivestudios.demo.ui.fragments.main.securityFeature.itemView.RowItemSecurityFeature
import com.fictivestudios.demo.utils.Constants.IMAGE_BASE_URL
import com.fictivestudios.demo.utils.PaginationScrollListener
import com.fictivestudios.demo.utils.gallery.ImageVideoBottomSheet
import com.fictivestudios.demo.utils.gallery.OnItemSelect
import com.fictivestudios.demo.utils.setSafeOnClickListener
import com.kennyc.view.MultiStateView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SecurityFeatureFragment : BaseFragment(R.layout.fragment_security_feature),
    View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    com.fictivestudios.demo.ui.fragments.main.securityFeature.itemView.OnItemClick,
    OnItemSelect {

    private var _binding: FragmentSecurityFeatureBinding? = null
    val binding
        get() = _binding!!

    private var fileList: ArrayList<MultipartBody.Part?>? = arrayListOf()
    private lateinit var bottomSheet: ImageVideoBottomSheet

    private var viewTypeArray = ArrayList<ViewType<*>>()
    val viewModel: SecurityFeatureViewModel by viewModels()

    private val selectedItemList = arrayListOf<String>()
    private var isMultipleItemSelection = false

    private var dialogDeleteItemBinding: DialogDeleteItemBinding? = null
    private var dialog: AlertDialog? = null

    private val adapter by lazy {
        GenericListAdapter(object : OnItemClickListener<ViewType<*>> {
            override fun onItemClicked(view: View, item: ViewType<*>, position: Int) {
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityFeatureBinding.inflate(inflater)
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
        viewModel.getAllSecurityFeatureApi(1)
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING

    }

    override fun setObserver() {
        viewModel.addSecurityFeatureAttachmentsResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    requireActivity().runOnUiThread {
                        showToast(data.value.message)
                    }
                    binding.progress.hide()
                    enableUserTouch()
                    fileList = arrayListOf()
                    viewModel.getAllSecurityFeatureApi(1)

                }

                is Resource.Failure -> {
                    Log.d("Failed", data.message.toString())
                    binding.progress.hide()
                    enableUserTouch()
                    fileList = arrayListOf()
                    requireActivity().runOnUiThread {
                        showToast(data.message.toString())
                    }

                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }

        viewModel.deleteSecurityFeatureResponse.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Resource.Success -> {
                    binding.imageViewCancel.gone()
                    binding.imageViewDelete.gone()
                    binding.imageViewBack.show()
                    binding.imageViewAdd.show()
                    lifecycleScope.launch {
                        delay(1500)
                        viewModel.getAllSecurityFeatureApi(1)
                        requireActivity().runOnUiThread {
                            showToast(data.value.message)
                            binding.progress.hide()
                            enableUserTouch()
                        }
                    }

                }

                is Resource.Failure -> {
                    binding.progress.hide()
                    binding.imageViewCancel.gone()
                    binding.imageViewDelete.gone()
                    binding.imageViewBack.show()
                    binding.imageViewAdd.show()
                    for (data in viewModel.securityFeatureList) {
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

                is Resource.Loading -> {}

                else -> {}
            }
        }

        viewModel.securityFeatureResponse.observe(viewLifecycleOwner) { data ->
            binding.swipeRefreshLayout.isRefreshing = false
            when (data) {
                is Resource.Success -> {
                    if (data.value.data?.attachments.isNullOrEmpty()) {
                        requireActivity().runOnUiThread {
                            showToast(data.value.message)
                        }
                        enableUserTouch()
                        binding.multiStateView.viewState =
                            MultiStateView.ViewState.EMPTY
                    } else {
                        data.value.data?.let {
                            requireActivity().runOnUiThread {
                                showToast(data.value.message)
                            }
                            enableUserTouch()
                            viewTypeArray.clear()
                            for (data in it.attachments) {
                                viewTypeArray.add(
                                    RowItemSecurityFeature(
                                        data, this, this
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
                    }

                }

                is Resource.Failure -> {
                    enableUserTouch()
                    requireActivity().runOnUiThread {
                        showToast(data.message.toString())
                    }

                    val imageView = binding.multiStateView.getView(
                        MultiStateView.ViewState.ERROR
                    )?.findViewById<AppCompatImageView>(R.id.imageViewError)
                    if (data.isNetworkError) {
                        imageView?.setImageResource(R.drawable.ic_error)
                    } else {
                        imageView?.setImageResource(R.drawable.ic_no_data)
                    }
                    binding.multiStateView.viewState = MultiStateView.ViewState.ERROR

                    if (data.errorCode == 401) {
                        (requireActivity() as DashBoardActivity).logout()
                    }
                }

                is Resource.Loading -> {
                }

                else -> {}
            }
        }
    }

    override fun setOnClickListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.imageViewBack.setOnClickListener(this)
        binding.imageViewCancel.setOnClickListener(this)
        binding.imageViewDelete.setSafeOnClickListener {
            showDeleteAccountMessageBox()
        }
        binding.imageViewAdd.setSafeOnClickListener {
            bottomSheet = ImageVideoBottomSheet(this)
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
                for (data in viewModel.securityFeatureList) {
                    data.isMultipleSelectionEnabled = false
                    data.isSelected = false
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun invokeMultipleSelection(data: Attachments) {
        for (data in viewModel.securityFeatureList) {
            data.isMultipleSelectionEnabled = true
            data.isSelected = false
        }

        binding.imageViewBack.gone()
        binding.imageViewAdd.gone()
        binding.imageViewCancel.show()
        binding.imageViewDelete.show()
        selectedItemList.add(data._id)
        adapter.notifyDataSetChanged()
    }

    override fun onItemSelected(data: Attachments) {
        selectedItemList.add(data._id)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemRemove(data: Attachments) {
        selectedItemList.remove(data._id)
        if (selectedItemList.isEmpty()) {
            binding.imageViewBack.show()
            binding.imageViewCancel.gone()
            binding.imageViewDelete.gone()
            binding.imageViewAdd.show()
            for (selectedData in viewModel.securityFeatureList) {
                selectedData.isMultipleSelectionEnabled = false
                selectedData.isSelected = false
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onClick(data: Attachments) {
        if (!isMultipleItemSelection) {
            if (data.type == "image") {
                findNavController().navigate(
                    SecurityFeatureFragmentDirections.actionSecurityFeatureFragmentToImagePreviewFragment(
                        data.file ?: "", "securityFeature", data._id
                    )
                )
            } else {
                startPlayer(getPlayerParam(data), requireContext())
            }
        }
    }

    override fun onSelectImageFromSystem(imageLinkList: ArrayList<File>?) {
        for (list in imageLinkList ?: arrayListOf()) {
            lifecycleScope.launch {
                fileList?.add(createImagePart(list))
                delay(2000)
            }

        }
        addAttachments()

    }

    override fun onSelectVideoFromSystem(videoFileList: ArrayList<File>?) {
        for (list in videoFileList ?: arrayListOf()) {
            lifecycleScope.launch {
                fileList?.add(createVideoPart(list))
                delay(2000)
            }
        }
        addAttachments()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRefresh() {
        binding.imageViewBack.show()
        binding.imageViewCancel.gone()
        binding.imageViewDelete.gone()
        binding.imageViewAdd.show()
        selectedItemList.clear()
        viewTypeArray.clear()
        adapter.notifyDataSetChanged()
        viewModel.getAllSecurityFeatureApi(1)
        binding.multiStateView.viewState =
            MultiStateView.ViewState.LOADING
    }

    private fun addAttachments() {
        bottomSheet.dismiss()
        viewModel.addAttachments(fileList)
        binding.progress.show()
        disableUserTouch()
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

    private fun createImagePart(imageFile: File): MultipartBody.Part {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            "files",
            imageFile.name,
            requestFile
        )
    }

    private fun createVideoPart(imageFile: File): MultipartBody.Part {
        val requestFile = imageFile.asRequestBody("video/*".toMediaTypeOrNull())
        Log.d("createVideoPart", requestFile.toString())
        return MultipartBody.Part.createFormData(
            "files",
            imageFile.name,
            requestFile
        )
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
                viewModel.deleteSecurityFeatureByIds(selectedItemList)
                binding.progress.show()
                disableUserTouch()
                dialog.dismiss()
            }
            dialog.show()
        }
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

    private fun getPlayerParam(data: Attachments): PlayerParams {
        return PlayerParams(url = IMAGE_BASE_URL + data.file)
    }
}