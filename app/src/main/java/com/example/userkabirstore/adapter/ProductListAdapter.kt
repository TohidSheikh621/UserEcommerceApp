package com.example.userkabirstore.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.databinding.ProductListRvBinding
import com.example.userkabirstore.model.ProductModel

class ProductListAdapter(
    private val requireContext: Context, private val products: List<ProductModel>
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ProductListRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }

            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = products[position]

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemId", menuItem.productId)
                putExtra("MenuItemName", menuItem.productName)
                putExtra("MenuItemImg", menuItem.productImage)
                putExtra("MenuItemDescription", menuItem.productDescription)
                putExtra("MenuItemPrice", menuItem.productPrice)
                putExtra("MenuItemIngredients", menuItem.productIngredients)
            }
            requireContext.startActivity(intent)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProductListRvBinding.inflate(inflater, parent, false)
        return ProductViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Bind the product data to the UI elements
        holder.binding.productName.text = product.productName.toString()
        holder.binding.priceTv.text = "₹${product.productPrice}"

        // Load product image (if applicable) using Glide or any image loading library
        Glide.with(holder.binding.productImg.context).load(product.productImage)
            .into(holder.binding.productImg)

    }

    override fun getItemCount(): Int = products.size
}
