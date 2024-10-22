package com.example.userkabirstore.model

data class PopularModel(
    var productId: String = "",
    val productName :String? =null,
    val productPrice :String? =null,
    val productImage :String? =null,
    val productDescription :String? =null,
    val productIngredients :String? =null,
    val colors: ArrayList<String>? = null
)