package com.example.userkabirstore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.userkabirstore.databinding.NotificationItemBinding

class NotificationAdapter(
    private val notificationItem: ArrayList<String>,
    private val notificationImg: ArrayList<Int>
) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                notificationName.text = notificationItem[position]
                notificationProductImg.setImageResource(notificationImg[position])
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.MyViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.MyViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return notificationItem.size
    }
}