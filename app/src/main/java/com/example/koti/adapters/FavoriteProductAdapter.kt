package com.example.koti.adapters

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koti.data.CartProduct
import com.example.koti.databinding.CartProductItemBinding
import com.example.koti.databinding.FavoriteProductItemBinding
import com.example.koti.helper.getProductPrice

class FavoriteProductAdapter: RecyclerView.Adapter<FavoriteProductAdapter.FavoriteProductsViewHolder>() {
    inner class FavoriteProductsViewHolder(val binding: FavoriteProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(favProduct.product.images[0]).into(imageFavoriteProduct)
                tvProductFavoriteName.text = favProduct.product.name.uppercase()
                tvAddToCart.paintFlags = Paint.UNDERLINE_TEXT_FLAG

                val priceAfterPercentage =
                    favProduct.product.offerPercentage.getProductPrice(favProduct.product.price)
                tvProductFavoritePrice.text = "$ ${String.format("%.2f", priceAfterPercentage)}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductsViewHolder {
        return FavoriteProductsViewHolder(
            FavoriteProductItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FavoriteProductsViewHolder, position: Int) {
        val favProduct = differ.currentList[position]
        holder.bind(favProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(favProduct)
        }

        holder.binding.linearAddFavToCart.setOnClickListener {
            onPlusClick?.invoke(favProduct)
        }

        holder.binding.imageDeleteFavorite.setOnClickListener {
            onMinusClick?.invoke(favProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
}