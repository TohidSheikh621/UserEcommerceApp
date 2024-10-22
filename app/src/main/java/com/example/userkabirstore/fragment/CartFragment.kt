package com.example.userkabirstore.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.activity.PayOutActivity
import com.example.userkabirstore.adapter.CartItemsAdapter
import com.example.userkabirstore.databinding.FragmentCartBinding
import com.example.userkabirstore.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CartFragment : BaseFragment(), CartItemsAdapter.OnCartItemRemovedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var cartItems: ArrayList<String>
    private lateinit var cartItemPrice: ArrayList<String>
    private lateinit var cartImg: ArrayList<String>
    private lateinit var cartDescription: ArrayList<String>
    private lateinit var cartIngredients: ArrayList<String>
    private lateinit var cartItemIds: ArrayList<String>
    private lateinit var cartQuantity: ArrayList<Int>
    private lateinit var productSelectedColor: ArrayList<String>
    private lateinit var productAvailableColors: ArrayList<ArrayList<String>>

    //    private var cartModel: ArrayList<CartModel> = arrayListOf()
    private lateinit var cartItemsAdapter: CartItemsAdapter
    private lateinit var userId: String
    private lateinit var binding: FragmentCartBinding

    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button
    private lateinit var productId: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        showLoader(true)

        binding.proceedBtn.isEnabled = false
        binding.proceedBtn.setOnClickListener {
            checkInternetAndGetOrderItemDetails()
        }

        return binding.root
    }

    private fun checkInternetAndGetOrderItemDetails() {
        if (isInternetOn(requireContext())) {
            getOrderItemDetails()
        } else {
            dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet))
                .setConfirmText("Retry")
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

    private fun getOrderItemDetails() {

        val orderDatabaseReference =
            database.reference.child("user").child(userId).child("CartItems")
        val productId = arrayListOf<String>()
        val productName = arrayListOf<String>()
        val productPrice = arrayListOf<String>()
        val productImage = arrayListOf<String>()
        val productDescription = arrayListOf<String>()
        val productIngredients = arrayListOf<String>()
        val productSelectedColor = arrayListOf<String>()
        val productAvailableColors = arrayListOf<ArrayList<String>>()

        val productQuantity = cartItemsAdapter.getUpdatedItemsQuantity()
        orderDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val orderItem = productSnapshot.getValue(CartModel::class.java)

                    orderItem?.productName?.let { productName.add(it) }
                    orderItem?.productId?.let { productId.add(it) }
                    orderItem?.productPrice?.let { productPrice.add(it) }
                    orderItem?.productIngredients?.let { productIngredients.add(it) }
                    orderItem?.productImage?.let { productImage.add(it) }
                    orderItem?.productDescription?.let { productDescription.add(it) }
                    orderItem?.colors?.let { productSelectedColor.add(it) }
                    orderItem?.availableColors?.let { productAvailableColors.add(ArrayList(it)) }

                }
                orderNow(
                    productId,
                    productName,
                    productPrice,
                    productImage,
                    productIngredients,
                    productDescription,
                    productQuantity,
                    productSelectedColor,
                    productAvailableColors
                )
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun orderNow(
        productId: ArrayList<String>,
        productName: ArrayList<String>,
        productPrice: ArrayList<String>,
        productImage: ArrayList<String>,
        productIngredients: ArrayList<String>,
        productDescription: ArrayList<String>,
        productQuantity: ArrayList<Int>,
        productSelectedColor: ArrayList<String>,
        productAvailableColors: ArrayList<ArrayList<String>>
    ) {

        if (isAdded && context != null) {
            showLoader(true)
            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(requireContext(), PayOutActivity::class.java).apply {

                    putExtra("productItemId", productId)
                    putExtra("productItemName", productName)
                    putExtra("productItemPrice", productPrice)
                    putExtra("productItemImage", productImage)
                    putExtra("productItemIngredients", productIngredients)
                    putExtra("productItemDescription", productDescription)
                    putExtra("productItemQuantity", productQuantity)
                    putExtra("productItemColor", productSelectedColor) // Selected color
                    putExtra(
                        "productItemAvailableColors",
                        productAvailableColors
                    ) // Available colors
                }
                startActivity(intent)
                showLoader(false)

            }, 1000)


        }
    }


    private fun retrieveCartItems() {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        var productReference = database.reference.child("user").child(userId).child("CartItems")

        cartItems = arrayListOf()
        cartImg = arrayListOf()
        cartItemPrice = arrayListOf()
        cartDescription = arrayListOf()
        cartIngredients = arrayListOf()
        cartQuantity = arrayListOf()
        productSelectedColor = arrayListOf()
        productAvailableColors = arrayListOf()
        cartItemIds = arrayListOf()
        productId = arrayListOf()

        productReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()
                cartImg.clear()
                cartItemPrice.clear()
                cartDescription.clear()
                cartIngredients.clear()
                cartQuantity.clear()
                productSelectedColor.clear()
                productAvailableColors.clear()
                cartItemIds.clear()
                for (productSnapshot in snapshot.children) {
                    val cartItem = productSnapshot.getValue(CartModel::class.java)
                    val id = productSnapshot.key

                    cartItem?.let {
                        cartItems.add(it.productName ?: "")
                        productId.add(it.productId ?: "")
                        cartItemPrice.add(it.productPrice ?: "")
                        cartDescription.add(it.productDescription ?: "")
                        cartImg.add(it.productImage ?: "")
                        cartQuantity.add(it.productQuantity ?: 0)
                        cartIngredients.add(it.productIngredients ?: "")
                        productSelectedColor.add(it.colors ?: "")
                        productAvailableColors.add(ArrayList(it.availableColors))
                        id?.let { cartItemIds.add(it) }  // Store the ID
                    }
                }

                if (cartItems.isEmpty()) {
                    onCartItemRemoved()
                } else {

                    setAdapter(cartItemIds)
                    binding.emptyCartTextView.visibility = View.GONE
                    binding.cartRv.visibility = View.VISIBLE
                    binding.proceedBtn.isEnabled = true

                }
                showLoader(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
                snackBarBottom(binding.root, "Data Not Fetch")
            }
        })
    }

    private fun setAdapter(cartItemIds: ArrayList<String>) {
        if (!isAdded) return
        cartItemsAdapter = CartItemsAdapter(
            requireContext(),
            cartItems,
            cartItemPrice,
            cartImg,
            cartIngredients,
            cartDescription,
            cartQuantity,
            productSelectedColor,
            productAvailableColors,
            cartItemIds,
//            cartModel,
            productId,
            this

        )

        binding.cartRv.layoutManager = LinearLayoutManager(context)
        binding.cartRv.adapter = cartItemsAdapter
        cartItemsAdapter.notifyDataSetChanged()
    }


    companion object {

    }

    override fun onCartItemRemoved() {
        // Check if cart is empty and update button state
        if (cartItems.isEmpty()) {
            binding.apply {
                proceedBtn.isEnabled = false
                proceedBtn.setBackgroundResource(R.drawable.proceed_disabled)
                emptyCartTextView.visibility = View.VISIBLE
            }


        }
    }

    override fun onResume() {
        super.onResume()

        if (isInternetOn(requireContext())) {
            retrieveCartItems()
        } else {
            dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet))
                .setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveCartItems()
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()


            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f


        }

        // Reload data when the fragment is resumed
    }
}