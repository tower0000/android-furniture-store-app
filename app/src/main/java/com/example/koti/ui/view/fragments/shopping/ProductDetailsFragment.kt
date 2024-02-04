package com.example.koti.ui.view.fragments.shopping

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.koti.R
import com.example.koti.databinding.FragmentProductsDetailsBinding
import com.example.koti.data.model.CartProduct
import com.example.koti.ui.adapters.ViewPager2Images
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.hideBottomNavigationView
import com.example.koti.ui.util.showBottomNavigationView
import com.example.koti.ui.viewmodel.DetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private val args by navArgs<ProductDetailsFragmentArgs>()
    private lateinit var binding: FragmentProductsDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentProductsDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        viewModel.checkIfProductInFavorites(product)
        setupImageIndicators()
        setupViewpager()

        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
            showBottomNavigationView()
        }


        binding.buttonAddToCartFixed.setOnClickListener {
            viewModel.addUpdateProductsInCart(CartProduct(product, 1, selectedColor, selectedSize))
        }

        binding.buttonAddToFav.setOnClickListener {
            viewModel.addDeleteFavoriteProduct(product)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFavorite.collectLatest {
                    if (it)
                        binding.buttonAddToFav.setImageResource(R.drawable.ic_favorite_clicked)
                    else
                        binding.buttonAddToFav.setImageResource(R.drawable.ic_favorite)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToCart.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonAddToCartFixed.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonAddToCartFixed.revertAnimation()
                            binding.buttonAddToCartFixed.setBackgroundDrawable(
                                resources.getDrawable(
                                    R.drawable.green_button_background
                                )
                            )
                            Toast.makeText(requireContext(), "Product added!", Toast.LENGTH_SHORT)
                                .show()
                        }

                        is Resource.Error -> {
                            binding.buttonAddToCartFixed.revertAnimation()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addDeleteProductFavorites.collectLatest {
                    when (it) {
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        binding.apply {
            product.offerPercentage?.let {
                val remainingPricePercentage = 1f - it
                val priceAfterOffer = remainingPricePercentage * product.price
                tvProductPrice.text = "$${String.format("%.2f", priceAfterOffer)}"

                val discountInPercent = (it * 100).roundToInt()
                tvDiscountPercentProduct.text = "${discountInPercent}% OFF"
            }
            tvProductOldPrice.text = "$${String.format("%.2f", product.price)}"
            tvProductOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            tvBestProductName.text = product.name.uppercase()
            tvProductDescription.text = product.description
        }

        viewPagerAdapter.differ.submitList(product.images)
    }


    private fun setupImageIndicators() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        val product = args.product
        val dotsImage = Array(product.images.size) { ImageView(requireContext()) }

        dotsImage.forEach {
            it.setImageResource(R.drawable.non_active_dot)
            binding.slideDotLL.addView(it, params)
        }

        // default first dot selected
        dotsImage[0].setImageResource(R.drawable.active_dot)

        val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dotsImage.mapIndexed { index, imageView ->
                    if (position == index) {
                        imageView.setImageResource(
                            R.drawable.active_dot
                        )
                    } else {
                        imageView.setImageResource(R.drawable.non_active_dot)
                    }
                }
                super.onPageSelected(position)
            }
        }
        binding.viewPagerProductImages.registerOnPageChangeCallback(pageChangeListener)
    }


    private fun setupViewpager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }
}