package com.example.koti.ui.view.fragments.shopping

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.koti.R
import com.example.koti.databinding.FragmentSearchResultsBinding
import com.example.koti.ui.adapters.BestProductsAdapter
import com.example.koti.ui.util.BestProductsItemDecoration
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.showBottomNavigationView
import com.example.koti.ui.viewmodel.SearchResultViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchResultFragment: Fragment() {
    private lateinit var binding: FragmentSearchResultsBinding
    private val offerAdapter: BestProductsAdapter by lazy { BestProductsAdapter() }
    private val request by navArgs<SearchResultFragmentArgs>()
    private val viewModel by viewModels<SearchResultViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchRequest = request.search
        viewModel.getResultProducts(searchRequest)
        setupOfferRv()

        binding.buttonCloseSearch.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvResults.text = "Results for: '$searchRequest'"

        offerAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, b)
        }

        binding.rvOfferProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollHorizontally(1) && dx != 0) {
                    viewModel.getResultProducts(searchRequest)
                }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.bestProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showOfferLoading()
                        }

                        is Resource.Success -> {
                            hideOfferLoading()
                            offerAdapter.differ.submitList(it.data)
                        }

                        is Resource.Error -> {
                            hideOfferLoading()
                            Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                                .show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    fun showOfferLoading() {
        binding.bestProductsProgressBar.visibility = View.VISIBLE
    }

    fun hideOfferLoading() {
        binding.bestProductsProgressBar.visibility = View.GONE
    }

    private fun setupOfferRv() {
        binding.rvOfferProducts.apply {
            layoutManager =
                GridLayoutManager(
                    requireContext(),
                    2,
                    GridLayoutManager.VERTICAL,
                    false
                )
            adapter = offerAdapter
            addItemDecoration(BestProductsItemDecoration())
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}