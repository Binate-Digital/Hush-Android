package com.fictivestudios.hush.ui.fragments.main.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.adapter.GenericListAdapter
import com.fictivestudios.hush.base.adapter.OnItemClickListener
import com.fictivestudios.hush.base.adapter.ViewType
import com.fictivestudios.hush.base.fragment.BaseFragment
import com.fictivestudios.hush.databinding.FragmentChatDetailBinding
import com.fictivestudios.hush.ui.activities.CallActivity
import com.fictivestudios.hush.ui.fragments.auth.otp.OtpVerificationFragmentArgs
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemChatList
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemMyChat
import com.fictivestudios.hush.ui.fragments.main.chat.itemView.RowItemOtherChat
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
        Log.d("ChatDetail",args.userId)
        viewModel.getUserData(args.userId)
        setRecyclerView()
    }


    override fun setObserver() {

        lifecycleScope.launch {
            viewModel.messages.collect { list ->
                viewTypeArray.clear()
                Log.d("UserId","${viewModel.userData?._id}")
                for (data in list) {
                    if(data.sender == "user"){
                        viewTypeArray.add(RowItemMyChat(data))
                    }else{
                        viewTypeArray.add(RowItemOtherChat(data))
                    }
                }
                adapter.items = viewTypeArray
                Log.d("Socket IO Message", "$list")
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
                viewModel.sendSms(binding.textInputEditTextMessage.text.toString(),viewModel.userData!!._id!!,args.userId)
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