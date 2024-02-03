package com.example.koti.ui.view.fragments.settings

import android.app.AlertDialog
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
import com.example.koti.R
import com.example.koti.databinding.FragmentAddressBinding
import com.example.koti.databinding.FragmentOrdersBinding
import com.example.koti.databinding.FragmentSavedAddressesBinding
import com.example.koti.domain.model.Address
import com.example.koti.domain.model.Order
import com.example.koti.ui.adapters.AddressAdapter
import com.example.koti.ui.adapters.AllOrdersAdapter
import com.example.koti.ui.adapters.SavedAddressAdapter
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.VerticalItemDecoration
import com.example.koti.ui.viewmodel.AllOrdersViewModel
import com.example.koti.ui.viewmodel.BillingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedAddressesFragment : Fragment() {
    private lateinit var binding: FragmentSavedAddressesBinding
    val viewModel by viewModels<BillingViewModel>()
    private val addressesAdapter by lazy { SavedAddressAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedAddressesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupAddressesRv()

        addressesAdapter.onDeleteClick = {
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Delete address")
                setMessage("Do you want to delete this address?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Delete") { dialog, _ ->
                    viewModel.deleteUserAddress(it)
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()
        }

        binding.buttonAddNewAddress.setOnClickListener {
            findNavController().navigate(R.id.action_savedAddressesFragment_to_addressFragment)
        }

        binding.buttonCloseOrders.setOnClickListener{
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.address.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarAllOrders.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressbarAllOrders.visibility = View.GONE
                            binding.rvAllOrders.visibility = View.VISIBLE
                            addressesAdapter.differ.submitList(it.data)
                            if(it.data == null || it.data == emptyList<Address>()){
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
    }

    private fun setupAddressesRv() {
        binding.rvAllOrders.apply {
            adapter = addressesAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserAddresses()
    }
}
