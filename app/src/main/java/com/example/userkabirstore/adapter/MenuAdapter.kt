package com.example.userkabirstore.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.databinding.MenuItemBinding
import com.example.userkabirstore.model.MenuModel

class MenuAdapter(

    private val menuList: List<MenuModel>,
    private val requireContext: Context,


) : RecyclerView.Adapter<MenuAdapter.MyViewHolder>() {


    inner class MyViewHolder(val binding: MenuItemBinding) : RecyclerView.ViewHolder(
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

        fun bind(position: Int) {
            val menuItem = menuList[position]
            binding.apply {

                menuProductName.text = menuItem.productName
                menuPriceTv.text = "₹" + menuItem.productPrice
                val uri = Uri.parse(menuItem.productImage)
                Glide.with(requireContext).load(uri).into(menuProductImg)

            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)
    }
}

