package com.example.koti.ui.view.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.koti.ui.adapters.AllOrdersAdapter
import com.example.koti.databinding.FragmentOrdersBinding
import com.example.koti.model.Order
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.VerticalItemDecoration
import com.example.koti.ui.viewmodel.AllOrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllOrdersFragment : Fragment() {
    private lateinit var binding: FragmentOrdersBinding
    val viewModel by viewModels<AllOrdersViewModel>()
    private val ordersAdapter by lazy { AllOrdersAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOrdersRv()

        binding.buttonCloseOrders.setOnClickListener{
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allOrders.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAllOrders.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressbarAllOrders.visibility = View.GONE
                            binding.rvAllOrders.visibility = View.VISIBLE
                            ordersAdapter.differ.submitList(it.data)
                            if(it.data == null || it.data == emptyList<Order>()){
                                binding.layoutOrdersEmpty.visibility = View.VISIBLE
                                binding.rvAllOrders.visibility = View.GONE
                            }
                        }
                        is Resource.Error -> {
                            binding.progressbarAllOrders.visibility = View.GONE
                            binding.layoutOrdersEmpty.visibility = View.VISIBLE
                            binding.rvAllOrders.visibility = View.GONE
                        }

                        else -> Unit
                    }
                }
            }
        }

        ordersAdapter.onClick = {
            val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToOrderDetailFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupOrdersRv() {
        binding.rvAllOrders.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }
}
