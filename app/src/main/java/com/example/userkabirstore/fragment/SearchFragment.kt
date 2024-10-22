package com.example.userkabirstore.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.adapter.MenuAdapter
import com.example.userkabirstore.databinding.FragmentSearchBinding
import com.example.userkabirstore.model.MenuModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : BaseFragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var productId: String
    private var originalMenuItem = mutableListOf<MenuModel>()


    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)


        showLoader(true)
        setUpSearch()

        checkInternetAndRetrieveMenuItem()
        return binding.root
    }

    private fun checkInternetAndRetrieveMenuItem() {
        if (isInternetOn(requireContext())) {
            retrieveMenuItem()
        } else {
            dialog = SweetAlertDialog(
                requireContext(),
                SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveMenuItem()
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

    private fun retrieveMenuItem() {

        val productReference = database.reference.child("menu")
        productReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (productSnapshot in snapshot.children) {
                    val searchItem = productSnapshot.getValue(MenuModel::class.java)
                    searchItem?.let {
                        it.productId = productSnapshot.key ?: ""
                        originalMenuItem.add(it)
                    }
                }
                showAllMenu()
                showLoader(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
            }

        })
    }

    private fun showAllMenu() {
        val filteredMenuItem = ArrayList(originalMenuItem).take(3)

        if (filteredMenuItem.isEmpty()) {
            binding.noResultsTextView.visibility = View.VISIBLE
        } else {
            binding.noResultsTextView.visibility = View.GONE
            setAdapter(filteredMenuItem)
        }

    }

    private fun setAdapter(filteredMenuItem: List<MenuModel>) {
        if (!isAdded) return
        menuAdapter = MenuAdapter(
            filteredMenuItem, requireContext()
        )
        binding.menuRv.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRv.adapter = menuAdapter
    }


    private fun setUpSearch() {
        binding.searchView.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMenuItems(newText)
                return true
            }


        })
    }

    private fun filterMenuItems(query: String) {

        CoroutineScope(Dispatchers.Default).launch {
            val filteredMenu = if (query.isEmpty()) {
                originalMenuItem // Show all items if the query is empty
            } else {
                originalMenuItem.filter {
                    it.productName?.contains(query, ignoreCase = true) == true
                }
            }.take(3)

            withContext(Dispatchers.Main) {
                if (filteredMenu.isEmpty()) {
                    binding.noResultsTextView.visibility = View.VISIBLE
                } else {
                    binding.noResultsTextView.visibility = View.GONE
                }
                setAdapter(filteredMenu)
            }
        }
    }

}