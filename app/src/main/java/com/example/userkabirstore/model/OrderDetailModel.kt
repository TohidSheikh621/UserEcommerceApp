package com.example.userkabirstore.model

 class OrderDetailModel(
     var userId: String? = null,
     var userName: String? = null,
     var address: String? = null,
     var phone: String? = null,
     var totalAmount: String? = null,
     var productName:String? = null,
     var productPrice: String? = null,
     var productImage: String? = null,
     var productQuantity: Int = 0,
     var productDescription : String? = null,
     var productIngredients : String? = null,
     var itemPushKey: String? = null,
     var orderAccepted: Boolean = false,
     var paymentReceived: Boolean = false,
     var currentTime: Long = 0
)

