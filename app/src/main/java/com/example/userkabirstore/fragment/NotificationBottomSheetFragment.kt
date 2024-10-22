package com.example.userkabirstore.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.userkabirstore.R
import com.example.userkabirstore.adapter.NotificationAdapter
import com.example.userkabirstore.databinding.FragmentNotificationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class NotificationBottomSheetFragment : BottomSheetDialogFragment() {


    private lateinit var binding:FragmentNotificationBottomSheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBottomSheetBinding.inflate(inflater,container,false)

        setUpRv()

        return binding.root
    }

    private fun setUpRv() {
        val notificationName =
            arrayListOf("Your order has been Canceled Successfully", "Congrats Your Order Placed", "Order has been taken by the driver")

        val notificationImg = arrayListOf(
            R.drawable.sademoji,
            R.drawable.success,
            R.drawable.cart
        )

        val notificationAdapter = NotificationAdapter(notificationName, notificationImg)

        binding.notificationRv.layoutManager = LinearLayoutManager(context)
        binding.notificationRv.adapter = notificationAdapter
    }
    companion object {


    }
}