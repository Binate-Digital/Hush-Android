package com.fictivestudios.hush.ui.fragments.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.GenericListAdapter
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.databinding.FragmentChatDetailBinding
import com.fictivestudios.hush.ui.activities.CallActivity
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemMyChat
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemOtherChat
import com.fictivestudios.hush.utils.Constants
import com.fictivestudios.hush.utils.PaginationScrollListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class ChatDetailFragment : BaseFragment(R.layout.fragment_chat_detail), View.OnClickListener {

    private var _binding: FragmentChatDetailBinding? = null
    val binding
        get() = _binding!!

    private var isItemClick = true
    private var viewTypeArray = ArrayList<ViewType<*>>()

    private val args by navArgs<ChatDetailFragmentArgs>()
    val viewModel: ChatDetailViewModel by viewModels()

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
        Glide.with(requireContext())
            .load(Constants.IMAGE_BASE_URL + args.userImage)
            .placeholder(R.drawable.person)
            .into(binding.imageViewUser)
        binding.textViewUserName.text = args.userName
        setRecyclerView()
        viewModel.getUserData(args.userId)
    }


    override fun setObserver() {

        lifecycleScope.launch {
            viewModel.messages.collect { list ->
                requireActivity().runOnUiThread {
                    viewTypeArray.clear()
                    for (data in list) {
                        if (data.sender == "user") {
                            viewTypeArray.add(
                                RowItemMyChat(
                                    data,
                                    viewModel.userData?.profileImage ?: ""
                                )
                            )
                        } else {
                            viewTypeArray.add(RowItemOtherChat(data, args.userImage))
                        }
                    }
                    adapter.items = viewTypeArray
                    // 🔥 Scroll to bottom after items updated
                    binding.recyclerView.post {
                        if (adapter.itemCount > 0) {
                            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                }

            }
        }
    }

    override fun setOnClickListener() {
        binding.imageViewCall.setOnClickListener(this)
        binding.imageViewSent.setOnClickListener(this)
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

            binding.imageViewSent.id -> {
                if(binding.textInputEditTextMessage.text.toString().isNotEmpty()){
                    viewModel.sendSms(
                        binding.textInputEditTextMessage.text.toString(),
                        viewModel.userData!!._id!!,
                        args.userId,
                        {
                            if (isAdded) {
                                activity?.runOnUiThread {
                                    binding.textInputEditTextMessage.text?.clear()
                                }
                            }
                        },
                        {
                            if (isAdded) {
                                activity?.runOnUiThread {
                                    Toast.makeText(requireContext(), "SomeThing Went Wrong", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
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