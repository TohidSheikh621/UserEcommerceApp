package com.example.userkabirstore.adapter


import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userkabirstore.databinding.CompletedListRvBinding


class CompletedOrderAdapter(
    private val context: Context,
    private val productName : MutableList<String>,
    private val productPrice: MutableList<String>,
    private val productImg : MutableList<String>,
) : RecyclerView.Adapter<CompletedOrderAdapter.OutForDeliveryViewHolder>() {
    inner class OutForDeliveryViewHolder(private val binding: CompletedListRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                completedProductName.text = productName[position]
                completedPriceTv.text = productPrice[position]
                val uriString = productImg[position]

                val uri = Uri.parse(uriString.toString())

                Glide.with(context).load(uri).into(completedProductImg)

            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutForDeliveryViewHolder {
        val view =
            CompletedListRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OutForDeliveryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productName.size
    }

    override fun onBindViewHolder(holder: OutForDeliveryViewHolder, position: Int) {
        holder.bind(position)
    }
}