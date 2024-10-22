package com.example.userkabirstore.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.userkabirstore.adapter.RecentBuyAdapter
import com.example.userkabirstore.databinding.ActivityRecentBuyBinding
import com.example.userkabirstore.model.OrderModel


class RecentBuyActivity : AppCompatActivity() {
    private val binding: ActivityRecentBuyBinding by lazy {

        ActivityRecentBuyBinding.inflate(layoutInflater)

    }

    private lateinit var productNames: ArrayList<String>
    private lateinit var productPrice: ArrayList<String>
    private lateinit var productImage: ArrayList<String>
    private lateinit var productQuantity: ArrayList<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val receiveOrderDetails = intent.getSerializableExtra("RecentBuy") as OrderModel
        receiveOrderDetails.let {

            productNames = receiveOrderDetails.productName as ArrayList<String>
            productPrice = receiveOrderDetails.productPrice as ArrayList<String>
            productImage = receiveOrderDetails.productImage as ArrayList<String>
            productQuantity = receiveOrderDetails.productQuantity as ArrayList<Int>

            setAdapter()
        }
    }

    private fun setAdapter() {
        var rv = binding.recentBuyRv
        rv.layoutManager = LinearLayoutManager(this)
        val recentBuyAdapter =
            RecentBuyAdapter(this, productNames, productPrice, productImage, productQuantity)

        rv.adapter = recentBuyAdapter
        Log.d("AdapterData", "Adapter Set : $recentBuyAdapter")
    }


}