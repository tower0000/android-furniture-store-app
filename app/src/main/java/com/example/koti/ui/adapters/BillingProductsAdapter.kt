package com.example.koti.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.koti.databinding.BillingProductsRvItemBinding
import com.example.koti.domain.model.CartProduct
import kotlin.math.roundToInt

class BillingProductsAdapter : Adapter<BillingProductsAdapter.BillingProductsViewHolder>() {

    inner class BillingProductsViewHolder(val binding: BillingProductsRvItemBinding) :
        ViewHolder(binding.root) {
        fun bind(billingProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(billingProduct.product.images[0]).into(imgCartProduct)
                billingProduct.product.offerPercentage?.let {
                    val remainingPricePercentage = 1f - it
                    val priceAfterOffer = remainingPricePercentage * billingProduct.product.price
                    tvCartProductPrice.text = "$${String.format("%.2f", priceAfterOffer)}"

                    val discountInPercent = (it * 100).roundToInt()
                }
                tvCartProductOldPrice.text = "$${String.format("%.2f", billingProduct.product.price)}"
                tvCartProductOldPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                tvCartProductQuantity.text = billingProduct.quantity.toString()

                tvProductCartName.text = billingProduct.product.name.uppercase()
            }

        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillingProductsViewHolder {
        return BillingProductsViewHolder(
            BillingProductsRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BillingProductsViewHolder, position: Int) {
        val billingProduct = differ.currentList[position]

        holder.bind(billingProduct)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}