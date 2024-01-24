package com.example.koti.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.koti.R
import com.example.koti.databinding.AddressRvItemBinding
import com.example.koti.databinding.SavedAddressRvItemBinding
import com.example.koti.model.Address

class SavedAddressAdapter : Adapter<SavedAddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: SavedAddressRvItemBinding) :
        ViewHolder(binding.root) {
        fun bind(address: Address, isSelected: Boolean) {
            binding.apply {
                tvFullName.text = address.fullName
                tvAddress.text = "${address.street} ${address.city}"
                tvShippingAddress.text = address.addressTitle
                tvPhoneNumber.text = address.phone
            }
        }


    }

    private val diffUtil = object : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.addressTitle == newItem.addressTitle && oldItem.fullName == newItem.fullName
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            SavedAddressRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    var selectedAddress = -1
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = differ.currentList[position]
        holder.bind(address, selectedAddress == position)

        holder.binding.buttonDeleteAddress.setOnClickListener {
            onDeleteClick?.invoke(address)
        }
    }

    init {
        differ.addListListener { _, _ ->
            notifyItemChanged(selectedAddress)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


    var onDeleteClick: ((Address) -> Unit)? = null
}