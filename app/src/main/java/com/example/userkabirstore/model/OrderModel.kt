package com.example.userkabirstore.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import kotlin.collections.ArrayList

class OrderModel() : Serializable {

    var userId: String? = null
    var userName: String? = null
    var address: String? = null
    var phone: String? = null
    var productId :MutableList<String>? = null
    var totalAmount: String? = null
    var productName: MutableList<String>? = null
    var productPrice: MutableList<String>? = null
    var productImage: MutableList<String>? = null
    var productQuantity: MutableList<Int>? = null
    var productDescription : MutableList<String>? = null
    var productIngredients : MutableList<String>? = null
    var colors : MutableList<String>? = null
    var itemPushKey: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var currentTime: Long = 0



    constructor(parcel: Parcel) : this() {
        userId = parcel.readString()
        userName = parcel.readString()
        address = parcel.readString()
        phone = parcel.readString()

        totalAmount = parcel.readString()
        itemPushKey = parcel.readString()
        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        currentTime = parcel.readLong()
    }

    constructor(
        userId: String,
        name: String,
        address: String,
        phone: String,
        productId: ArrayList<String>,
        totalAmount: String,
        productName: ArrayList<String>,
        productPrice: ArrayList<String>,
        productImage: ArrayList<String>,
        productQuantity: ArrayList<Int>,
        productDescription : ArrayList<String>,
        productIngredients : ArrayList<String>,
        colors : ArrayList<String>,
        itemPushKey: String?,
        orderAccepted : Boolean,
        paymentReceived: Boolean,
        time: Long,
    ) : this(){
        this.userId = userId
        this.userName = name
        this.address = address
        this.phone = phone
        this.productId = productId
        this.totalAmount = totalAmount
        this.productName = productName
        this.productPrice = productPrice
        this.productImage = productImage
        this.productQuantity = productQuantity
        this.productDescription = productDescription
        this.productIngredients = productIngredients
        this.colors = colors
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
        this.currentTime = time

    }

    fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeString(totalAmount)
        parcel.writeString(itemPushKey)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeLong(currentTime)
    }

    fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderModel> {
        override fun createFromParcel(parcel: Parcel): OrderModel {
            return OrderModel(parcel)
        }

        override fun newArray(size: Int): Array<OrderModel?> {
            return arrayOfNulls(size)
        }
    }
}
