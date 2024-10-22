package com.example.userkabirstore.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.adapter.CompletedOrderAdapter
import com.example.userkabirstore.databinding.ActivityCompletedOrderBinding
import com.example.userkabirstore.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CompletedOrderActivity : BaseActivity() {

    private lateinit var binding: ActivityCompletedOrderBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var listOfCompleteOrder: ArrayList<OrderModel> = arrayListOf()
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()


        binding = ActivityCompletedOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoader(true)
        binding.swipeToRefresh.setOnRefreshListener {
            checkInternetAndRetrieveCompleteOrderDetails()
        }
        checkInternetAndRetrieveCompleteOrderDetails()

        binding.backBtn.setOnClickListener {
            finish()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }

    }

    private fun checkInternetAndRetrieveCompleteOrderDetails() {
        if (isInternetOn(this)) {
            retrieveCompleteOrderDetails()
        } else {
            dialog = SweetAlertDialog(
                this, SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveCompleteOrderDetails()
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun retrieveCompleteOrderDetails() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Reference to the CompletedOrderHistory node for the particular user
        val completeOrderReference =
            database.reference.child("CompletedOrderHistory").orderByChild("userId")
                .equalTo(userId)  // Filtering by user ID

        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfCompleteOrder.clear()

                for (completeOrder in snapshot.children) {
                    val completeOrderList = completeOrder.getValue(OrderModel::class.java)
                    completeOrderList?.let {
                        listOfCompleteOrder.add(it)
                    }
                }
                listOfCompleteOrder.reverse()
                if (listOfCompleteOrder.isEmpty()) {
                    binding.completedOrderRv.visibility = View.GONE
                    binding.emptyAllItemOrderTextView.visibility = View.VISIBLE
                } else {
                    setAdapter()
                    binding.completedOrderRv.visibility = View.VISIBLE
                    binding.emptyAllItemOrderTextView.visibility = View.GONE
                }

                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false
            }
        })
    }


    private fun setAdapter() {

//        val productName = arrayListOf<String>()
//        val productPrice = arrayListOf<String>()
//        val productImg = arrayListOf<String>()
//        for (order in listOfCompleteOrder) {
//
//            order.name?.let {
//                productName.add(it)
//            }
//
//            order.price?.let {
//                productPrice.add(it)
//            }
//            order.image?.let {
//                productImg.add(it)
//            }
//
//        }
        val productNameList = mutableListOf<String>()
        val productPriceList = mutableListOf<String>()
        val productImgList = mutableListOf<String>()
        val productQuantityList = mutableListOf<Int>()

        for (order in listOfCompleteOrder) {
            order.productName?.let { productNameList.addAll(it) }
            order.productPrice?.let { productPriceList.addAll(it) }
            order.productImage?.let { productImgList.addAll(it) }
            order.productQuantity?.let { productQuantityList.addAll(it) }
        }

        val completedOrderAdapter = CompletedOrderAdapter(
            this,
            productNameList,
            productPriceList,
            productImgList,
        )
        binding.completedOrderRv.adapter = completedOrderAdapter
        binding.completedOrderRv.layoutManager = LinearLayoutManager(this)
    }

}
