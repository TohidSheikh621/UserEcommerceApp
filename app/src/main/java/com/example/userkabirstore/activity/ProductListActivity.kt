package com.example.userkabirstore.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.adapter.ProductListAdapter
import com.example.userkabirstore.databinding.ActivityProductListBinding
import com.example.userkabirstore.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductListActivity : BaseActivity() {

    private lateinit var binding: ActivityProductListBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var category: String
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }

        database = FirebaseDatabase.getInstance()
        category = intent.getStringExtra("category") ?: return

        binding.categoryTitleTv.text = category

        showLoader(true)

        binding.backBtn.setOnClickListener {
            finish()
        }




        binding.swipeToRefresh.setOnRefreshListener {

            checkInternetAndFetchProductsByCategory()

        }
        checkInternetAndFetchProductsByCategory()
    }

    private fun checkInternetAndFetchProductsByCategory() {
        if (isInternetOn(this)) {
            fetchProductsByCategory(category)
        } else {
            dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText(
                    getString(R.string.title_internet)
                ).setContentText(getString(R.string.content_internet))
                .setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                fetchProductsByCategory(category)
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


    private fun fetchProductsByCategory(category: String) {
        val database = FirebaseDatabase.getInstance()
        val productRef = database.getReference("menu")

        Log.d("UserApp", "Fetching products for category: $category")

        productRef.orderByChild("category").equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductModel>()
                    if (snapshot.exists()) {
                        for (productSnapshot in snapshot.children) {
                            val product = productSnapshot.getValue(ProductModel::class.java)
                            if (product != null) {
                                product.productId = productSnapshot.key ?: ""
                                products.add(product)

                            } else {

                            }
                        }

                    } else {
                        Log.d("UserApp", "No products found for category: $category")
                    }

                    products.shuffle()

                    if (products.isEmpty()) {
                        binding.emptyAllItemOrderTextView.visibility = View.VISIBLE
                        binding.productListRv.visibility = View.GONE
                    } else {
                        updateRecyclerView(products)
                        binding.emptyAllItemOrderTextView.visibility = View.GONE
                        binding.productListRv.visibility = View.VISIBLE
                    }

                    showLoader(false)
                    binding.swipeToRefresh.isRefreshing = false

                }

                override fun onCancelled(error: DatabaseError) {
                    showLoader(false)
                    Log.e("UserApp", "Error fetching products: ${error.message}")
                }
            })
    }


    private fun updateRecyclerView(products: List<ProductModel>) {
        if (products.isEmpty()) {
            Log.d("UserApp", "No products to display")
        } else {
            Log.d("UserApp", "Updating RecyclerView with ${products.size} products")
        }
        binding.productListRv.layoutManager = GridLayoutManager(this, 2)
        binding.productListRv.adapter = ProductListAdapter(this, products)
    }

}