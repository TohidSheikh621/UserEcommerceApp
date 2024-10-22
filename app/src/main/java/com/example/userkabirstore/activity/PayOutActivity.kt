package com.example.userkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivityPayOutBinding
import com.example.userkabirstore.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PayOutActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phone: String
    private  lateinit var productId: ArrayList<String>
    private lateinit var totalAmount: String
    private lateinit var productName: ArrayList<String>
    private lateinit var productPrice: ArrayList<String>
    private lateinit var productImage: ArrayList<String>
    private lateinit var productQuantity: ArrayList<Int>
    private lateinit var productIngredients: ArrayList<String>
    private lateinit var productDescription: ArrayList<String>
    private lateinit var productColor: ArrayList<String>


    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button


    private lateinit var binding: ActivityPayOutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoader(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        SaveUserData()

        binding.placeOrderBtn.setOnClickListener {
            showLoaderTransparent(true)
            name = binding.nameEditText.text.toString()
            phone = binding.phoneEditText.text.toString()
            address = binding.addressEditText.text.toString()

            val namePattern = "^[A-Za-z\\s]{2,50}$"
            val mobilePattern = "^(?:\\+91|91)?[789]\\d{9}$"
            val addressPattern = "^[a-zA-Z0-9\\s,.#-]+$"

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                snackBarBottom(binding.root, "Please fill all the details")

            } else if (!name.matches(Regex(namePattern))) {
                snackBarBottom(
                    binding.root,
                    "Please enter a valid name (2-50 alphabetic characters)"
                )
            } else if (!address.matches(Regex(addressPattern))) {
                snackBarBottom(binding.root, "Please enter a valid address.")
            } else if (!phone.matches(Regex(mobilePattern))) {
                snackBarBottom(binding.root, "Please enter a valid mobile number.")
            } else {
                checkInternetAndPlaceOrder()
            }


        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        val intent = intent


        productId = intent.getStringArrayListExtra("productItemId") as ArrayList<String>
        productName = intent.getStringArrayListExtra("productItemName") as ArrayList<String>
        productPrice = intent.getStringArrayListExtra("productItemPrice") as ArrayList<String>
        productImage = intent.getStringArrayListExtra("productItemImage") as ArrayList<String>
        productDescription =
            intent.getStringArrayListExtra("productItemDescription") as ArrayList<String>
        productIngredients =
            intent.getStringArrayListExtra("productItemIngredients") as ArrayList<String>
        productQuantity = intent.getIntegerArrayListExtra("productItemQuantity") as ArrayList<Int>
        productColor = intent.getStringArrayListExtra("productItemColor") as ArrayList<String>



        totalAmount = "₹ " + calculateTotalAmount().toString()

        binding.totalAmountTv.text = totalAmount

    }

    private fun checkInternetAndPlaceOrder() {
        if (isInternetOn(this)) {
            placeOrder()
        } else {
            dialog = SweetAlertDialog(
                this,
                SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()


            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f
        }

    }

    private fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayView.visibility = View.VISIBLE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayView.visibility = View.GONE // Hide the transparent overlay
        }
    }
    private fun showLoaderTransparent(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayViewTransparent.visibility = View.VISIBLE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayViewTransparent.visibility = View.GONE // Hide the transparent overlay
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()

        // Loop through each product and create a separate order entry
        for (i in productId.indices) {
            // Generate a new push key for each order item
            val itemPushKey = databaseReference.child("OrderDetails").push().key
            val orderDetails = OrderModel(
                userId,
                name,
                address,
                phone,
                arrayListOf(productId[i]), // Only include the current productId
                totalAmount, // You might want to calculate a separate total for each item
                arrayListOf(productName[i]), // Only include the current productName
                arrayListOf(productPrice[i]), // Only include the current productPrice
                arrayListOf(productImage[i]), // Only include the current productImage
                arrayListOf(productQuantity[i]), // Only include the current productQuantity
                arrayListOf(productDescription[i]), // Only include the current productDescription
                arrayListOf(productIngredients[i]), // Only include the current productIngredients
                arrayListOf(productColor[i]), // Only include the current productColor
                itemPushKey, // Unique push key for each order
                false,
                false,
                time
            )

            val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)

            orderReference.setValue(orderDetails).addOnSuccessListener {
                // Optionally handle successful order placement for each item here
                // e.g., show a success message or log it
            }.addOnFailureListener {
                showLoaderTransparent(false)
                snackBarBottom(binding.root, "Failed to order item: ${productName[i]}")
            }

            addOrderToHistory(orderDetails)
        }

        // After processing all items, navigate to the OrderPlacedActivity
        val intent = Intent(this, OrderPlacedActivity::class.java)
        startActivity(intent)
        finish()
        showLoaderTransparent(false)
        removeFromCart()

        // You may also want to handle adding orders to history separately if needed
    }


    private fun addOrderToHistory(orderDetails: OrderModel) {

        databaseReference.child("user").child(userId).child("BuyHistory").child(orderDetails.itemPushKey!!).setValue(orderDetails).addOnSuccessListener {

        }


    }


    private fun removeFromCart() {
        val cartReference = databaseReference.child("user").child(userId).child("CartItems")
        cartReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0

        for (i in 0 until productPrice.size) {

            var price = productPrice[i]
            var firstChar = price.first()
            var priceIntValue = if (firstChar == '₹') {
                price.drop(1).toInt()
            } else {
                price.toInt()
            }

            val quantity = productQuantity[i]
            totalAmount += priceIntValue * quantity


        }
        return totalAmount
    }

    private fun SaveUserData() {
        showLoader(true)
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val names = snapshot.child("name").getValue(String::class.java) ?: ""
                        val phones = snapshot.child("phone").getValue(String::class.java) ?: ""
                        val addresses = snapshot.child("address").getValue(String::class.java) ?: ""

                        binding.apply {
                            nameEditText.setText(names)
                            phoneEditText.setText(phones)
                            addressEditText.setText(addresses)
                        }
                        showLoader(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoader(false)
                    snackBarBottom(binding.root, "Failed to load user data")

                }

            })
        }
    }
}