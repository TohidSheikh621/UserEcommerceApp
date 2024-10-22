package com.example.userkabirstore.model

data class CompleteOrderModel(

    val orderId: String? = null,        // Unique order ID
    val userId: String? = null,         // User ID to whom the order belongs
    val name: String? = null,           // Product name
    val price: String? = null,          // Product price
    val image: String? = null,          // Product image URL
    val quantity: Int? = null,          // Quantity of items ordered
    val orderDate: String? = null,      // Date when the order was placed
    val deliveryStatus: String? = null, // Status of the order (e.g., delivered, pending)
    val address: String? = null
)
