package com.example.koti.ui.view.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.koti.R
import com.example.koti.databinding.FragmentFavoritesBinding
import com.example.koti.data.model.QuantityChanging
import com.example.koti.ui.adapters.FavoriteProductAdapter
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.VerticalItemDecoration
import com.example.koti.ui.viewmodel.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private val favAdapter by lazy { FavoriteProductAdapter() }
    private val viewModel by activityViewModels<FavoritesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFavRv()

        binding.buttonRedirectToHome.setOnClickListener{
            findNavController().navigate(R.id.action_favoritesFragment_to_homeFragment)
        }

        favAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_favoritesFragment_to_productDetailsFragment, b)
        }

        favAdapter.onPlusClick = {
            viewModel.changeFavoriteProductState(it, QuantityChanging.INCREASE)
        }

        favAdapter.onMinusClick = {
            viewModel.changeFavoriteProductState(it, QuantityChanging.DECREASE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteDialog.collectLatest {
                    val alertDialog = AlertDialog.Builder(requireContext()).apply {
                        setTitle("Delete item from cart")
                        setMessage("Do you want to delete this item from your cart?")
                        setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        setPositiveButton("Delete") { dialog, _ ->
                            viewModel.deleteFavProduct(it)
                            dialog.dismiss()
                        }
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addDialog.collectLatest {
                    val alertDialog = AlertDialog.Builder(requireContext()).apply {
                        setTitle("Add item to cart")
                        setMessage("Do you want to add this item to your cart?")
                        setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        setPositiveButton("Add") { dialog, _ ->
                            viewModel.addFavToCart(it)
                            viewModel.deleteFavProduct(it)
                            dialog.dismiss()
                        }
                    }
                    alertDialog.create()
                    alertDialog.show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToCart.collectLatest {
                    when (it) {
                        is Resource.Error -> {
                            binding.progressbarFav.visibility = View.INVISIBLE
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
                viewModel.favoriteProducts.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressbarFav.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressbarFav.visibility = View.INVISIBLE
                            if (it.data!!.isEmpty()) {
                                hideFavorites()
                            } else {
                                showFavorites()
                                favAdapter.differ.submitList(it.data)
                            }

                        }

                        is Resource.Error -> {
                            binding.progressbarFav.visibility = View.INVISIBLE
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
    }


    private fun showFavorites() {
        binding.apply {
            layoutCartEmpty.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            buttonRedirectToHome.visibility = View.VISIBLE
        }
    }

    private fun hideFavorites() {
        binding.apply {
            layoutCartEmpty.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
            buttonRedirectToHome.visibility = View.GONE
        }
    }

    private fun setupFavRv() {
        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = favAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}