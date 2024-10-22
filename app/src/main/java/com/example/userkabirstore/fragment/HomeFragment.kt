package com.example.userkabirstore.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.userkabirstore.R
import com.example.userkabirstore.activity.DetailsActivity
import com.example.userkabirstore.activity.ProductListActivity
import com.example.userkabirstore.adapter.CategoryAdapter
import com.example.userkabirstore.adapter.PopularAdapter
import com.example.userkabirstore.databinding.FragmentHomeBinding
import com.example.userkabirstore.model.Category
import com.example.userkabirstore.model.PopularModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var popularItem: MutableList<PopularModel>
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        showLoaderWithOverlay(true)

        binding.swipeToRefresh.setOnRefreshListener {

            checkInternetAndRetrievedData()

        }

        checkInternetAndRetrievedData()

        val categories = listOf(
            Category("1", "Cosmetics", R.drawable.cosmetics),
            Category("2", "Jewellery", R.drawable.necklace),
            Category("3", "Keychains", R.drawable.keychains),
            Category("4", "Clature", R.drawable.clature)
            // Add more categories as needed
        )

        setupRecyclerView(categories)


        return binding.root
    }

    private fun checkInternetAndRetrievedData() {
        if (isInternetOn(requireContext())) {
            retrieveAndDisplayPopularItems()
        } else {
            dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE).setTitleText(
                getString(R.string.title_internet)
            ).setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveAndDisplayPopularItems()
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()


            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f


        }

    }


    private fun setupRecyclerView(categories: List<Category>) {
        categoryAdapter = CategoryAdapter(categories) { category ->
            // Handle navigation when a category is clicked
            val intent = Intent(requireContext(), ProductListActivity::class.java)
            intent.putExtra("category", category.name)
            startActivity(intent)
        }
        binding.categoryRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRv.adapter = categoryAdapter
    }

    private fun showLoaderWithOverlay(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayWhiteView.visibility = View.VISIBLE // Show the transparent overlay
            binding.cardView.visibility = View.GONE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayWhiteView.visibility = View.GONE
            binding.cardView.visibility = View.VISIBLE // Hide the transparent overlay
        }
    }


    private fun retrieveAndDisplayPopularItems() {
        database = FirebaseDatabase.getInstance()

        val productRef = database.reference.child("menu")


        popularItem = mutableListOf()
        popularItem.clear()

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapShot in snapshot.children) {

                    val popularMenuItem = productSnapShot.getValue(PopularModel::class.java)
                    popularMenuItem?.let {
                        // Here, assign the key as productId
                        it.productId = productSnapShot.key ?: ""
                        popularItem.add(it)
                    }
                }

                if (popularItem.isEmpty()) {
                    binding.emptyCartTextView.visibility = View.VISIBLE
                    binding.popularRv.visibility = View.GONE
                } else {
                    randomPopularItem()
                    imageSliderPopularItem()
                    binding.emptyCartTextView.visibility = View.GONE
                    binding.popularRv.visibility = View.VISIBLE
                }
                showLoaderWithOverlay(false)
                binding.swipeToRefresh.isRefreshing = false
//

            }

            override fun onCancelled(error: DatabaseError) {
                showLoaderWithOverlay(false)
            }


        })
    }

    private fun randomPopularItem() {
        val index = popularItem.indices.toList().shuffled()
        val numToShow = 6
        val subsetPopularItems = index.take(numToShow).map { popularItem[it] }

        imageSliderSetPopularAdapter(subsetPopularItems)
    }

    private fun imageSliderPopularItem() {
        val index = popularItem.indices.toList().shuffled()
        val numToShow = 6
        val subsetPopularItems = index.take(numToShow).map { popularItem[it] }

        imageSliderSetPopularAdapter(subsetPopularItems)
    }

    private fun imageSliderSetPopularAdapter(subsetPopularItems: List<PopularModel>) {

        val popularAdapter = PopularAdapter(subsetPopularItems, requireContext())
        binding.popularRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularRv.adapter = popularAdapter

        val imageList = ArrayList<SlideModel>()
        for (popularItem in subsetPopularItems) {
            // Assuming PopularModel has a field `imageUrl` or resource reference
            imageList.add(SlideModel(popularItem.productImage, ScaleTypes.FIT))
        }

        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)

        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                // Optionally handle double click
            }

            override fun onItemSelected(position: Int) {
//                // Handle click on the image
//                val selectedItem = subsetPopularItems[position]
//                val itemMessage = "Selected: ${selectedItem.productName}"
//                Toast.makeText(context, itemMessage, Toast.LENGTH_SHORT).show()

                val menuItem = subsetPopularItems[position]

                val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
                    putExtra("MenuItemName", menuItem.productName)
                    putExtra("MenuItemId", menuItem.productId)
                    putExtra("MenuItemImg", menuItem.productImage)
                    putExtra("MenuItemDescription", menuItem.productDescription)
                    putExtra("MenuItemPrice", menuItem.productPrice)
                    putExtra("MenuItemIngredients", menuItem.productIngredients)
                }
                startActivity(intent)

            }
        })
    }



}