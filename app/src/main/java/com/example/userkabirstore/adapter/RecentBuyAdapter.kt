package com.example.userkabirstore.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userkabirstore.databinding.RecentBuyRvBinding

class RecentBuyAdapter(
    private var context: Context,
    private var productName: ArrayList<String>,
    private var productPrice: ArrayList<String>,
    private var productImage: ArrayList<String>,
    private var productQuantity: ArrayList<Int>
) : RecyclerView.Adapter<RecentBuyAdapter.MyViewHolder>() {
    inner class MyViewHolder(private val binding : RecentBuyRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                recentProductNameAt.text = productName[position]
                recentPriceAt.text = productPrice[position]
                quantityAt.text = productQuantity[position].toString()
                val uriString  = productImage[position]
                val uri  = Uri.parse(uriString)
                Glide.with(context).load(uri).into(recentBuyProductImgAt)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view  = RecentBuyRvBinding.inflate(LayoutInflater.from(parent.context),parent, false,)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productName.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.bind(position)
    }

}