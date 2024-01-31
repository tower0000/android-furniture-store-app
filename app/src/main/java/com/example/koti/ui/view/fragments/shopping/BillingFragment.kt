package com.example.koti.ui.view.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.koti.R
import com.example.koti.databinding.FragmentBillingBinding
import com.example.koti.model.Address
import com.example.koti.model.CartProduct
import com.example.koti.model.Order
import com.example.koti.model.OrderStatus
import com.example.koti.ui.adapters.AddressAdapter
import com.example.koti.ui.adapters.BillingProductsAdapter
import com.example.koti.ui.util.HorisontalItemDecoration
import com.example.koti.ui.util.Resource
import com.example.koti.ui.viewmodel.BillingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val viewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var selectedAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.product.toList()
        totalPrice = args.totalPrice
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        binding.buttonCloseBilling.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAddress.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            addressAdapter.differ.submitList(it.data)
                            binding.progressbarAddress.visibility = View.GONE
                        }

                        is Resource.Error -> {
                            binding.progressbarAddress.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.order.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonPlaceOrder.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            findNavController().navigateUp()
                            Toast.makeText(
                                requireContext(),
                                "Your order was placed",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonPlaceOrder.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        billingProductsAdapter.differ.submitList(products)

        binding.tvTotalPrice.text = "$${String.format("%.2f", totalPrice)}"

        addressAdapter.onClick = {
            selectedAddress = it
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order items")
            setMessage("Do you want to order your cart items?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )
                viewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorisontalItemDecoration())
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
            addItemDecoration(HorisontalItemDecoration())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserAddresses()
    }
}