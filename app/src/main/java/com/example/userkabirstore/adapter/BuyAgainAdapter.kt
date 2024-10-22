package com.example.userkabirstore.adapter

import android.content.Context

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide

import com.example.userkabirstore.R

import com.example.userkabirstore.databinding.BuyAgainRvBinding
import com.example.userkabirstore.model.OrderModel

class BuyAgainAdapter(
    private val context: Context,
    private val orderItems: MutableList<OrderModel>,
    private val itemClicked: OnItemClicked,
    private val onItemClick: (OrderModel, position: Int) -> Unit
) : RecyclerView.Adapter<BuyAgainAdapter.MyViewHolder>() {

    interface OnItemClicked {
        fun onItemRemoveClickListener(position: Int)
        fun onItemCancelClickListener(position: Int)
        fun onItemClickListener(orderItem: OrderModel, position: Int)
    }

    inner class MyViewHolder(val binding: BuyAgainRvBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(orderItem: OrderModel) {
            binding.apply {
                historyProductName.text = orderItem.productName?.firstOrNull() ?: ""
                historyPriceTv.text =
                    "₹" + orderItem.productPrice?.firstOrNull()?.toString() ?: "N/A"

                val image = orderItem.productImage?.firstOrNull() ?: ""
                Glide.with(context).load(image).into(historyProductImg)

                val currentTime = System.currentTimeMillis()
                val timeDifference = currentTime - orderItem.currentTime
                val is24HoursPassed = timeDifference > 24 * 60 * 60 * 1000

                receivedBtn.apply {

                    if (orderItem.orderAccepted) {
                        // If the order is accepted, show "Received" and set green color
                        receivedBtn.text = "Received"
                        receivedBtn.background.setTint(
                            ContextCompat.getColor(
                                context, R.color.green
                            )
                        )
                        recentSuccessImg.background.setTint(
                            ContextCompat.getColor(
                                context, R.color.green
                            )
                        )

                        // Set click listener for removing the item
                        receivedBtn.setOnClickListener {
                            onItemClick(orderItem, adapterPosition)
                            itemClicked.onItemRemoveClickListener(adapterPosition)
                        }


                    } else {
                        // If the order is not accepted, show "Cancel" and red color
                        receivedBtn.text = "Cancel"
                        receivedBtn.background.setTint(Color.RED)



                        if (is24HoursPassed) {
                            // Disable the cancel button if more than 24 hours have passed
                            receivedBtn.isEnabled = false
                            receivedBtn.visibility = View.GONE
                        } else {
                            // Enable the cancel button if within 24 hours
                            receivedBtn.isEnabled = true
                            receivedBtn.visibility = View.VISIBLE
                            receivedBtn.setOnClickListener {


                                var dialog = SweetAlertDialog(
                                    context, SweetAlertDialog.WARNING_TYPE
                                ).setTitleText("Cancel Order").setContentText("Are you Sure ?")
                                    .setConfirmText("Yes").setCancelText("No")
                                    .setConfirmClickListener { sweetAlertDialog ->
                                        sweetAlertDialog.dismissWithAnimation() // Close the dialog
                                        itemClicked.onItemCancelClickListener(adapterPosition)
                                    }.setCancelClickListener { sweetAlertDialog ->
                                        sweetAlertDialog.dismissWithAnimation() // Simply dismiss the dialog on cancel
                                    }
                                dialog.show()

                                val confirmButton =
                                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                                val cancelButton =
                                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.cancel_button) as Button

// Change the text color of the buttons
                                confirmButton.setTextColor(Color.BLACK)
                                confirmButton.textSize = 14f// Confirm button text color
                                cancelButton.setTextColor(Color.BLACK)
                                cancelButton.textSize = 14f
                            }
                        }
                    }


                    // Handle button click

                    itemView.setOnClickListener {
                        itemClicked.onItemClickListener(orderItem, adapterPosition)
                    }

                }

            }


        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = BuyAgainRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(orderItems[position])
    }


    fun removeItem(position: Int) {
        if (position >= 0 && position < orderItems.size) {
            orderItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }


}