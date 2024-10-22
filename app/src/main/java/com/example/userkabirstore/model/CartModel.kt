package com.example.userkabirstore.model

data class CartModel(

    var productId: String = "",
    var productName: String? = null,
    var productImage: String? = null,
    var productPrice: String? = null,
    var productDescription: String? = null,
    var productQuantity: Int? = null,
    var productIngredients: String? = null,
    var colors: String?= null,  // Store the selected color
    val availableColors: List<String> = emptyList() // Store all available colors

)
