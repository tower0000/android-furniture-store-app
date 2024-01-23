package com.example.koti.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.koti.databinding.FavoriteProductItemBinding
import com.example.koti.model.Product
import com.example.koti.ui.util.getProductPrice

class FavoriteProductAdapter: RecyclerView.Adapter<FavoriteProductAdapter.FavoriteProductsViewHolder>() {
    inner class FavoriteProductsViewHolder(val binding: FavoriteProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favProduct: Product) {
            binding.apply {
                Glide.with(itemView).load(favProduct.images[0]).into(imgFavoriteProduct)
                tvProductFavoriteName.text = favProduct.name.uppercase()

                val priceAfterPercentage =
                    favProduct.offerPercentage.getProductPrice(favProduct.price)
                tvProductFavoritePrice.text = "$ ${String.format("%.2f", priceAfterPercentage)}"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
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

    var onProductClick: ((Product) -> Unit)? = null
    var onPlusClick: ((Product) -> Unit)? = null
    var onMinusClick: ((Product) -> Unit)? = null
}