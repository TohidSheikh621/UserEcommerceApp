package com.example.userkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivityDetailsBinding
import com.example.userkabirstore.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var colors: String? = null
    private var productId: String? = null
    private var productName: String? = null
    private var productImage: String? = null
    private var productPrice: String? = null
    private var productDescription: String? = null
    private var productIngredients: String? = null
    private lateinit var productColors: List<String>
    private var cartItemKey: String? = null
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoaderWithOverlay(true)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }
        binding.buyNowBtn.setOnClickListener {
            // Show loader when Buy Now button is clicked
            showLoader(true)

            if (isInternetOn(this)) {
                // Simulate a delay for smoother transition (optional, for user experience)
                Handler(Looper.getMainLooper()).postDelayed({
                    // Create an intent to start PayOutActivity
                    val intent = Intent(this@DetailsActivity, PayOutActivity::class.java).apply {
                        putExtra("productItemName", arrayListOf(productName))
                        putExtra("productItemId", arrayListOf(productId))
                        putExtra("productItemPrice", arrayListOf(productPrice))
                        putExtra("productItemImage", arrayListOf(productImage))
                        putExtra("productItemDescription", arrayListOf(productDescription))
                        putExtra("productItemIngredients", arrayListOf(productIngredients))
                        putExtra("productItemColor", arrayListOf(colors))
                        putExtra(
                            "productItemQuantity", arrayListOf(1)
                        ) // Adjust quantity if necessary
                    }

                    // Hide the loader after navigating
                    showLoader(false)
                    // Start PayOutActivity
                    startActivity(intent)
                }, 1000)
            } else {
                dialog = SweetAlertDialog(
                    this, SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                    // Optionally check again when the user presses the confirm button
                    getDataFromIntent()
                    sweetAlertDialog.dismissWithAnimation()
                }
                dialog.show()


                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f

                showLoader(false)


            }

            // Optional: delay for smoother loader effect (e.g., 500ms)
        }


        getDataFromIntent()



        binding.menuBackImg.setOnClickListener {
            finish()
        }

        binding.addToCartDetailsBtn.setOnClickListener {
            Log.d("Add to Cart", "Current selected color: $colors")
            if (isInternetOn(this)) {
                if (colors!!.equals("Choose Color")) {
                    snackBarTop(binding.root, "Please Select Color")
                } else {
                    // Toggle between adding or removing from the cart
                    if (!atc) {
                        addItemToCart(colors!!)
                    } else {
                        removeItemFromCart()
                    }
                }
            } else {
                dialog = SweetAlertDialog(
                    this, SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                    // Optionally check again when the user presses the confirm button
                    getDataFromIntent()
                    sweetAlertDialog.dismissWithAnimation()
                }
                dialog.show()


                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f
            }

        }
        // Setup AutoCompleteTextView for color selection
        binding.listOfColor.setOnItemClickListener { parent, view, position, id ->
            // Get the selected color
            val selectedColor = parent.getItemAtPosition(position).toString()

            // Enable the Add to Cart button
            binding.addToCartDetailsBtn.isEnabled = true

            // Optionally store this selected color in a variable for further use
            this.colors = selectedColor
        }

         productId = intent.getStringExtra("MenuItemId") // Retrieve the push key here
        Log.d("DetailsActivity", "Opening DetailsActivity with productId: $productId")

        loadColorsWithoutProductId(productId!!)
    }

    private fun totalCartItemCount() {
        val userId = auth.currentUser?.uid ?: ""
        var cartItemReference = database.reference.child("user").child(userId).child("CartItems")
        var cartItemCount = 0
        cartItemReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemCount = snapshot.childrenCount.toInt()
                if (cartItemCount > 0) {
                    binding.totalCartItemTv.visibility = View.VISIBLE
                    binding.totalCartItemTv.text = cartItemCount.toString()
                } else {
                    binding.totalCartItemTv.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun getDataFromIntent() {

        productName = intent.getStringExtra("MenuItemName")
        productImage = intent.getStringExtra("MenuItemImg")
        productDescription = intent.getStringExtra("MenuItemDescription")
        productIngredients = intent.getStringExtra("MenuItemIngredients")
        productPrice = intent.getStringExtra("MenuItemPrice")

        val productColor = intent.getStringArrayExtra("MenuItemColor")// Receive colors as array

        totalCartItemCount()

        with(binding) {
            productDetailsNameTv.text = productName ?: "No Name"
            descriptionTv.text = productDescription ?: "No Description"
            ingredientsTv.text = productIngredients ?: "No Ingredients"
            priceDetailsTv.text = "₹" + productPrice ?: "No Price"

            productColors = productColor?.toList() ?: emptyList()  // Set colors array
            Log.d("Colors", "Available Colors: ${productColors.joinToString(", ")}")
            colors = intent.getStringExtra("selectedColor")


            with(binding) {
                if (productImage != null) {
                    Glide.with(this@DetailsActivity).load(Uri.parse(productImage))
                        .error(R.drawable.ic_launcher_background) // Fallback image if loading fails
                        .into(productDetailsImg)
                } else {
                    productDetailsImg.setImageResource(R.drawable.ic_launcher_background) // Default image if URI is null
                }
            }


        }

    }


    private fun populateColors(
        autoCompleteTextView: AutoCompleteTextView,
        selectedColor: String?,
        availableColors: List<String>
    ) {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availableColors)
        autoCompleteTextView.setAdapter(adapter)

        // Set the previously selected color as the default, if any
        selectedColor?.let {
            autoCompleteTextView.setText(it, false)
        }

        // Enable the "Add to Cart" button if a color is already selected
        binding.addToCartDetailsBtn.isEnabled = selectedColor != null
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

    private fun showLoaderWithOverlay(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayWhiteView.visibility = View.VISIBLE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayWhiteView.visibility = View.GONE // Hide the transparent overlay
        }
    }

    var atc = false

    private fun addItemToCart(selectedColor: String) {
        val userId = auth.currentUser?.uid ?: ""
        val cartItems = CartModel(
            productId.toString(),
            productName.toString(),
            productImage.toString(),
            productPrice.toString(),
            productDescription.toString(),
            1,
            productIngredients,
            selectedColor,  // Ensure selected color is passed
            productColors
        )

        // Add the item to the cart
        val newCartItemRef = database.reference.child("user").child(userId).child("CartItems").push()
        cartItemKey = newCartItemRef.key

        newCartItemRef.setValue(cartItems).addOnSuccessListener {
            snackBarTop(binding.root, "Item added to cart")
            binding.addToCartDetailsBtn.text = "Remove From Cart"
            atc = true // Update flag
        }.addOnFailureListener {
            snackBarTop(binding.root, "Failed to add item")
        }
    }

    private fun removeItemFromCart() {
        val userId = auth.currentUser?.uid ?: ""

        // Check if cartItemKey is valid
        if (cartItemKey.isNullOrEmpty()) {
            snackBarTop(binding.root, "Item not found in cart")
            return
        }

        // Remove the item from the cart
        database.reference.child("user").child(userId).child("CartItems").child(cartItemKey!!)
            .removeValue().addOnSuccessListener {
                snackBarTop(binding.root, "Item removed from cart")
                binding.addToCartDetailsBtn.text = "Add To Cart"
                atc = false // Update flag
            }.addOnFailureListener {
                snackBarTop(binding.root, "Failed to remove item")
            }
    }



    private fun loadColorsWithoutProductId(productId : String) {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""


        // Fetch product details using productId
        val productReference = database.child("menu").child(productId).child("colors")
        productReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {

                val allAvailableColors = mutableSetOf<String>()
                val colors = snapshot.value as? List<String> ?: emptyList()
                allAvailableColors.addAll(colors)

                // Now fetch the selected color from the user's cart
                val cartReference = database.child("user").child(userId).child("CartItems")
                cartReference.orderByChild("productName").equalTo(productName).get()
                    .addOnSuccessListener { cartSnapshot ->
                        var selectedColor: String? = null
                        if (cartSnapshot.exists()) {
                            cartSnapshot.children.forEach { itemSnapshot ->
                                val cartItem = itemSnapshot.getValue(CartModel::class.java)
                                selectedColor =
                                    cartItem?.colors // Get the selected color from the cart
                            }
                        }

                        // Populate AutoCompleteTextView with all available colors and selected color
                        populateColors(
                            binding.listOfColor,
                            selectedColor,
                            allAvailableColors.toList()
                        )
                    }.addOnFailureListener {
                    snackBarTop(
                        binding.root,
                        "Failed to load selected color from cart: ${it.message}"
                    )
                }
            } else {
                snackBarTop(binding.root, "No products found")
            }
        }.addOnFailureListener {
            snackBarTop(binding.root, "Failed to load product colors: ${it.message}")
        }
    }

    override fun onResume() {
        super.onResume()

        showLoaderWithOverlay(true)
        // Check cart status when returning to the screen
        checkCartStatus()
    }

    private fun checkCartStatus() {
        val userId = auth.currentUser?.uid ?: ""
        val cartReference = database.reference.child("user").child(userId).child("CartItems")

        cartReference.get().addOnSuccessListener { snapshot ->
            var found = false
            for (item in snapshot.children) {
                val product = item.child("productName").value.toString()
                if (product == productName) {
                    found = true
                    cartItemKey = item.key
                    colors = item.child("colors").value.toString() // Set selected color from cart
                    break
                }
            }

            if (found) {
                atc = true
                binding.addToCartDetailsBtn.text = "Remove From Cart"
            } else {
                atc = false
                binding.addToCartDetailsBtn.text = "Add To Cart"
            }

            showLoaderWithOverlay(false)
        }
    }



}

