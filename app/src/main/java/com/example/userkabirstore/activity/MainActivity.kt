package com.example.userkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    interface CartItemCountCallback {
        fun onCartItemCountReceived(count: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        var navController = findNavController(R.id.navCont)
        var bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)

        getCartItemCount(object : CartItemCountCallback {
            override fun onCartItemCountReceived(count: Int) {
                updateCartBadge(count) // Update the badge with the fetched count
            }
        })

        binding.settingGif.setOnClickListener {
            showBottomSheetForSetting()
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }


        // Check if the Intent contains the flag to show the CartFragment
    }

    private fun showBottomSheetForSetting() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_setting_dialog, null)

//        btnClose.setOnClickListener {
//            // on below line we are calling a dismiss
//            // method to close our dialog.
//            dialog.dismiss()
//        }

        val logout = view.findViewById<TextView>(R.id.logout)
        val privacyPolicy = view.findViewById<TextView>(R.id.privacyPolicy)
        val about = view.findViewById<TextView>(R.id.about)

        logout.setOnClickListener {
            showLogoutDialog()
        }

        privacyPolicy.setOnClickListener {
            showPrivacyPolicy()
        }
        about.setOnClickListener {
            showAbout()
        }


        dialog.setContentView(view)

        dialog.show()
    }

    private fun showPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicyActivity:: class.java)
        startActivity(intent)
    }

    private fun showAbout() {
        val intent = Intent(this, AboutActivity:: class.java)
        startActivity(intent)
    }


    private fun getCartItemCount(callback: CartItemCountCallback) {
        val userId = auth.currentUser?.uid ?: ""
        val cartItemReference = database.reference.child("user").child(userId).child("CartItems")

        cartItemReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItemCount = snapshot.childrenCount.toInt()
                callback.onCartItemCountReceived(cartItemCount) // Pass the count to the callback
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error (optional)
                callback.onCartItemCountReceived(0) // Pass 0 in case of error
            }
        })
    }


    private fun updateCartBadge(cartItemCount: Int) {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.cartFragment)
        if (cartItemCount > 0) {
            badge.isVisible = true
            badge.number = cartItemCount
        } else {
            badge.isVisible = false
        }
    }


    fun showLogoutDialog() {
        var dialog =
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("Are you sure?")
                .setContentText("You won't be able to access your account until you log in again!")
                .setConfirmText("Yes, log out").setCancelText("Cancel")
                .setConfirmClickListener { sweetAlertDialog ->
                    sweetAlertDialog.dismissWithAnimation() // Close the dialog
                    performLogout() // Call the logout function
                }.setCancelClickListener { sweetAlertDialog ->
                    sweetAlertDialog.dismissWithAnimation() // Simply dismiss the dialog on cancel
                }
        dialog.show()

        val confirmButton =
            dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
        val cancelButton =
            dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.cancel_button) as Button

// Change the text color of the buttons
        confirmButton.setTextColor(Color.BLACK)
        confirmButton.textSize = 14f// Confirm button text color
        cancelButton.setTextColor(Color.BLACK)
        cancelButton.textSize = 14f


    }

    fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        // Redirect to login or splash screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close current activity
    }
}