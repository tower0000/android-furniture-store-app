package com.example.koti.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koti.databinding.CartProductItemBinding
import com.example.koti.domain.model.CartProduct
import kotlin.math.roundToInt

class CartProductAdapter :
    RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imgCartProduct)
                tvCartProductName.text = cartProduct.product.name.uppercase()
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                cartProduct.product.offerPercentage?.let {
                    val remainingPricePercentage = 1f - it
                    val priceAfterOffer = remainingPricePercentage * cartProduct.product.price
                    tvCartProductPrice.text = "$${String.format("%.2f", priceAfterOffer)}"
                    val discountInPercent = (it * 100).roundToInt()
                    tvDiscountPercentProduct.text = "${discountInPercent}% OFF"
                }
                tvCartProductOldPrice.text = "$${String.format("%.2f", cartProduct.product.price)}"
                tvCartProductOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        return CartProductsViewHolder(
            CartProductItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }

        holder.binding.buttonPlus.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }

        holder.binding.buttonMinus.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
}