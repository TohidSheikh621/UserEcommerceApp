package com.example.userkabirstore.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.activity.CompletedOrderActivity
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.adapter.BuyAgainAdapter
import com.example.userkabirstore.databinding.FragmentHistoryBinding
import com.example.userkabirstore.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryFragment : BaseFragment(), BuyAgainAdapter.OnItemClicked {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    private var listOfOrderDetails: MutableList<OrderModel> = mutableListOf()


    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        showLoader(true)


        binding.swipeToRefresh.setOnRefreshListener {

            checkInternetRetrieveBuyHistory()

        }
        binding.history.setOnClickListener {
            val intent = Intent(requireContext(), CompletedOrderActivity::class.java)
            startActivity(intent)

        }

        checkInternetRetrieveBuyHistory()
        return binding.root
    }

    private fun checkInternetRetrieveBuyHistory() {
        if (isInternetOn(requireContext())) {
            retrieveBuyHistory()
        } else {
            dialog = SweetAlertDialog(
                requireContext(), SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveBuyHistory()
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


//    private fun updateOrderStatus() {
//        if (listOfOrderDetails.isEmpty()) return
//        val itemPushKey = listOfOrderDetails[0].itemPushKey
//        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey!!)
//        completeOrderReference.child("paymentReceived").setValue(true)
//    }
//
//    private fun seeRecentItemBuy() {
//        listOfOrderDetails.firstOrNull()?.let {
//            var intent = Intent(requireContext(), RecentBuyActivity::class.java)
//            intent.putExtra("RecentBuy", ArrayList(listOfOrderDetails))
//            startActivity(intent)
//        }
//    }


    private fun retrieveBuyHistory() {

        listOfOrderDetails.clear()
        if (!isAdded) return
        userId = auth.currentUser?.uid ?: ""

        val buyReference = database.reference.child("user").child(userId).child("BuyHistory")

        val shortingQuery = buyReference.orderByChild("currentTime")

        shortingQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                for (buySnapshot in snapshot.children) {

                    val buyHistoryItem = buySnapshot.getValue(OrderModel::class.java)
                    val productId = buySnapshot.key ?: ""
                    buyHistoryItem?.let {

                        it.productId?.add(productId)
                        listOfOrderDetails.add(it)

                    }
                }
                listOfOrderDetails.reverse()

                if (listOfOrderDetails.isEmpty()) {
                    binding.emptyAllItemOrderTextView.visibility = View.VISIBLE
                    binding.historyRv.visibility = View.GONE
                } else {
                    setupRecyclerView()
                    binding.emptyAllItemOrderTextView.visibility = View.GONE
                    binding.historyRv.visibility = View.VISIBLE
                }
                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
            }

        })
    }

    private fun setupRecyclerView() {
        binding.historyRv.layoutManager = LinearLayoutManager(context)
        val adapter =
            BuyAgainAdapter(requireContext(), listOfOrderDetails, this) { orderItem, position ->
                handleItemClick(orderItem, position)

            }
        binding.historyRv.adapter = adapter
    }

    private fun handleItemClick(orderItem: OrderModel, position: Int) {

        orderItem.itemPushKey?.let { itemPushKey ->
            updateOrderStatus(itemPushKey, position)
        }
    }

    private fun updateOrderStatus(itemPushKey: String, position: Int) {
        if (itemPushKey.isEmpty()) return

        val completeOrderReference = database.reference.child("CompletedOrder").child(itemPushKey)
        completeOrderReference.child("paymentReceived").setValue(true).addOnCompleteListener {

                task ->
            if (task.isSuccessful) {

                (binding.historyRv.adapter as? BuyAgainAdapter)?.removeItem(position)
                (binding.historyRv.adapter as? BuyAgainAdapter)?.notifyDataSetChanged()

                Log.d("OrderStatus", "Order status updated")

            }
        }.addOnFailureListener { exception ->

            Log.d("OrderStatus", "Failed to update order status : ${exception.message}")

        }
    }

    override fun onItemRemoveClickListener(position: Int) {

        val removeItemPushKey = listOfOrderDetails[position].itemPushKey
        val orderDataToMove = listOfOrderDetails[position]

        if (removeItemPushKey != null && orderDataToMove != null) {
            // Set the OrderModel object as the value in the CompletedOrder node
            val dispatchItemReference =
                database.reference.child("CompletedOrderHistory").child(removeItemPushKey)
            dispatchItemReference.setValue(orderDataToMove).addOnSuccessListener {
                // If successful, remove the order from OrderDetails
                deleteThisItemFromOrderDetails(removeItemPushKey, position)
            }.addOnFailureListener {
                // Handle any errors during the move operation
                snackBarBottom(binding.root, "Failed to remove order : ${it.message}")

            }
        } else {
            snackBarBottom(binding.root, "Invalid order key or order data")

        }
    }

    override fun onItemCancelClickListener(position: Int) {

        val removeItemPushKey = listOfOrderDetails[position].itemPushKey
        if (removeItemPushKey != null) {
            deleteThisItemFromOrderDetailsFunction(removeItemPushKey, position)
            retrieveBuyHistory()
        }
    }

    override fun onItemClickListener(orderItem: OrderModel, position: Int) {

        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra("MenuItemId", orderItem.productId?.firstOrNull()) // Pass product name
            putExtra("MenuItemColor", orderItem.colors?.firstOrNull()) // Pass product name
            putExtra("MenuItemName", orderItem.productName?.firstOrNull()) // Pass product name
            putExtra("MenuItemImg", orderItem.productImage?.firstOrNull()) // Pass product image URL
            putExtra(
                "MenuItemDescription",
                orderItem.productDescription?.firstOrNull()
            ) // Pass description
            putExtra(
                "MenuItemIngredients",
                orderItem.productIngredients?.firstOrNull()
            ) // Pass ingredients
            putExtra("MenuItemPrice", orderItem.productPrice?.firstOrNull()) // Pass price
        }
        startActivity(intent)


    }

    private fun deleteThisItemFromOrderDetails(removeItemPushKey: String, position: Int) {

        val orderDetailsReference =
            database.reference.child("user").child(userId).child("BuyHistory")
                .child(removeItemPushKey)
        orderDetailsReference.removeValue().addOnSuccessListener {
            snackBarBottom(binding.root, "Order received successfully")
        }.addOnFailureListener {
            snackBarBottom(binding.root, "Failed to receive order")
        }
    }

    private fun deleteThisItemFromOrderDetailsFunction(removeItemPushKey: String, position: Int) {

        // Data Delete From BuyHistory
        val buyHistoryReference = database.reference.child("user").child(userId).child("BuyHistory")
            .child(removeItemPushKey)

        buyHistoryReference.removeValue().addOnSuccessListener {

            snackBarBottom(binding.root, "Order Cancel")

            // After successful removal from BuyHistory, delete from Admin's OrderDetails
            val orderDetailsReference =
                database.reference.child("OrderDetails").child(removeItemPushKey)

            orderDetailsReference.removeValue().addOnSuccessListener {
                Log.e(
                    "OrderDetailsError", "remove from orderDetails"
                )
            }.addOnFailureListener { exception ->
                Log.e(
                    "OrderDetailsError", "Failed to remove from OrderDetails: ${exception.message}"
                )
            }

        }.addOnFailureListener {
            snackBarBottom(binding.root, "Failed to remove from History")
        }
    }


}