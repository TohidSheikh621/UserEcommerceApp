package com.example.userkabirstore.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.databinding.PopularItemRvBinding
import com.example.userkabirstore.model.PopularModel

class PopularAdapter(

    private val menuList: List<PopularModel>,
    private val requireContext: Context,

    ) : RecyclerView.Adapter<PopularAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: PopularItemRvBinding) : RecyclerView.ViewHolder(
        binding.root

    ) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }
            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = menuList[position]
            val product = menuItem.productId

            if (product.isEmpty()) {
                Log.e("DetailsActivity", "Product ID is null or empty")
            } else {
                Log.d("DetailsActivity", "Product ID: $product")
            }

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemId", product)
                putExtra("MenuItemName", menuItem.productName)
                putExtra("MenuItemImg", menuItem.productImage)
                putExtra("MenuItemDescription", menuItem.productDescription)
                putExtra("MenuItemPrice", menuItem.productPrice)
                putExtra("MenuItemIngredients", menuItem.productIngredients)
                putExtra("MenuItemColor", menuItem.colors?.toTypedArray())
            }
            requireContext.startActivity(intent)

        }

        fun bind(position: Int) {
            val menuItem = menuList[position]
            binding.apply {

                productName.text = menuItem.productName
                priceTv.text = "₹" + menuItem.productPrice
                val uri = Uri.parse(menuItem.productImage)

                Glide.with(requireContext).load(uri).into(productImg)

            }


        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            PopularItemRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }
}

private fun OnClickListener?.onItemClick(position: Int) {

}