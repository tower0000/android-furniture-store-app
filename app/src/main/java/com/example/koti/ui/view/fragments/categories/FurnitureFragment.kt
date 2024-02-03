package com.example.koti.ui.view.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.koti.domain.model.Category
import com.example.koti.ui.util.Resource
import com.example.koti.ui.viewmodel.CategoryViewModel
import com.example.koti.ui.viewmodel.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
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
        }
    }

    override fun onOfferPagingRequest() {
    }
}