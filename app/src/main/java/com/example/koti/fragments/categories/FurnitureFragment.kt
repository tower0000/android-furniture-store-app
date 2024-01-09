package com.example.koti.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.koti.data.Category
import com.example.koti.util.Resource
import com.example.koti.viewmodel.CategoryViewModel
import com.example.koti.viewmodel.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FurnitureFragment: BaseCategoryFragment() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory(firestore, Category.Furniture)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.offerProducts.collectLatest {
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

        lifecycleScope.launchWhenStarted {
            viewModel.bestProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showBestProductsLoading()
                    }

                    is Resource.Success -> {
                        hideBestProductsLoading()
                        bestProductsAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        hideBestProductsLoading()
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }

                    else -> Unit
                }

            }
        }
    }

    override fun onBestProductsPagingRequest() {
    }

    override fun onOfferPagingRequest() {
    }
}