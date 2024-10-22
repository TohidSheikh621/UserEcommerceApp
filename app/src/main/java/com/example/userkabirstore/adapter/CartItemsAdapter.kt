package com.example.userkabirstore.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity.CONNECTIVITY_SERVICE
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.example.userkabirstore.R
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.databinding.CartItemsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CartItemsAdapter(
    private val requireContext: Context,
    private val cartItems: ArrayList<String>,
    private val cartItemPrice: ArrayList<String>,
    private val cartImg: ArrayList<String>,
    private var cartIngredients: ArrayList<String>,
    private val cartDescription: ArrayList<String>,
    private val cartQuantity: ArrayList<Int>,
    private val productColor: ArrayList<String>,
    private val productAvailableColors: ArrayList<ArrayList<String>>,
    private val cartItemIds: ArrayList<String>,
    private val productId :  ArrayList<String>,
    private val onCartItemRemovedListener: OnCartItemRemovedListener

) : RecyclerView.Adapter<CartItemsAdapter.MyViewHolder>() {

    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    interface OnCartItemRemovedListener {
        fun onCartItemRemoved()
    }

    private val auth = FirebaseAuth.getInstance()


    init {

        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val cartItemNumber = cartItems.size

        itemQuantity = IntArray(cartItemNumber) { 1 }

        cartItemReference = database.reference.child("user").child(userId).child("CartItems")
    }

    companion object {
        private var itemQuantity: IntArray = intArrayOf()
        private lateinit var cartItemReference: DatabaseReference
    }

    inner class MyViewHolder(private val binding: CartItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }

            }
        }


        private fun openDetailsActivity(position: Int) {

            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemId", productId[position])
                putExtra("MenuItemName", cartItems[position])
                putExtra("MenuItemImg", cartImg[position])
                putExtra("MenuItemDescription", cartDescription[position])
                putExtra("MenuItemPrice", cartItemPrice[position])
                putExtra("MenuItemIngredients", cartIngredients[position])
                putExtra("MenuItemColor", productAvailableColors[position].toTypedArray()) // Pass available colors
                putExtra("selectedColor", productColor[position])
            }
            requireContext.startActivity(intent)

        }

        fun bind(position: Int) {
            val id = cartItemIds[position]
            val quantity = itemQuantity[position]

            binding.apply {


                cartProductName.text = cartItems[position]
                cartPriceTv.text = "₹" + cartItemPrice[position]
                val uriString = cartImg[position]
                val uri = Uri.parse(uriString)
                Glide.with(requireContext).load(uri).into(cartProductImg)
                cartQuantityTv.text = quantity.toString()

                cartSubstractImgBtn.setOnClickListener {
                    decreaseQuantity(position)

                }

                cartAddImgBtn.setOnClickListener {
                    increaseQuantity(position)

                }
                cartRemoveImgBtn.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {


                        if (isInternetOn(requireContext)) {
                            removeItem(position, id)
                        } else {
                            dialog = SweetAlertDialog(
                                requireContext, SweetAlertDialog.ERROR_TYPE
                            ).setTitleText(R.string.title_internet)
                                .setContentText("Please check internet connection!")
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

                }


            }

        }


        private fun decreaseQuantity(position: Int) {
            if (itemQuantity[position] > 1) {
                itemQuantity[position]--
                cartQuantity[position] = itemQuantity[position]
                binding.cartQuantityTv.text = itemQuantity[position].toString()
                updateQuantityInFirebase(position, itemQuantity[position])
                notifyItemChanged(position)


            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantity[position] < 10) {
                itemQuantity[position]++
                cartQuantity[position] = itemQuantity[position]
                binding.cartQuantityTv.text = itemQuantity[position].toString()
                updateQuantityInFirebase(position, itemQuantity[position])
                notifyItemChanged(position)
            }
        }

        private fun updateQuantityInFirebase(position: Int, newQuantity: Int) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val databaseReference =
                FirebaseDatabase.getInstance().reference.child("user").child(userId)
                    .child("CartItems")
                    .child(cartItemIds[position])  // Assuming you have a list of item IDs

            databaseReference.child("productQuantity").setValue(newQuantity).addOnSuccessListener {
                //      Toast.makeText(requireContext, "Quantity updated in database", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                //       Toast.makeText(requireContext, "Failed to update quantity", Toast.LENGTH_SHORT).show()
            }
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            cartItemReference.child(uniqueKey).removeValue().addOnSuccessListener {
                if (position >= 0 && position < cartItems.size) {
                    cartItems.removeAt(position)
                    cartImg.removeAt(position)
                    cartItemPrice.removeAt(position)
                    cartQuantity.removeAt(position)
                    cartDescription.removeAt(position)
                    cartIngredients.removeAt(position)
                    productColor.removeAt(position)
                    cartItemIds.removeAt(position)

                    snackBarBottom(binding.root, "Item deleted")
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)

                    onCartItemRemovedListener.onCartItemRemoved()
                }
            }.addOnFailureListener {

                snackBarBottom(binding.root, "Failed to delete")
            }
        }

        private fun snackBarBottom(view: View, msg: String) {

            val snack: Snackbar = Snackbar.make(view, msg, 1000)
            val view = snack.view
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.BOTTOM
            view.layoutParams = params
            snack.setBackgroundTint(getColor(requireContext, R.color.lavender))
                .setTextColor(Color.BLACK)
            snack.show()

        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CartItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(position)

    }

    fun getUpdatedItemsQuantity(): ArrayList<Int> {
        val itemQuantity = arrayListOf<Int>()
        itemQuantity.addAll(cartQuantity)
        return itemQuantity
    }

    fun isInternetOn(context: Context): Boolean {
        try {
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true
                } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    return true
                }
            } else {
                // not connected to the internet
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


}