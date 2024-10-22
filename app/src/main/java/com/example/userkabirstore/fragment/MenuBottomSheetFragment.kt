package com.example.userkabirstore.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.userkabirstore.adapter.MenuAdapter
import com.example.userkabirstore.databinding.FragmentMenuBottomSheetBinding
import com.example.userkabirstore.model.MenuModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)





        binding.menuBackImg.setOnClickListener {
            dismiss()
        }

        retrieveMenuItems()

        return binding.root


    }

    private fun retrieveMenuItems() {

        database = FirebaseDatabase.getInstance()
        val productRef : DatabaseReference = database.reference.child("menu")
        menuItems = mutableListOf()

        productRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (productSnapshot in snapshot.children){
                    val menuItem = productSnapshot.getValue(MenuModel::class.java)

                    menuItem?.let {
                        menuItems.add(it)
                    }

                    setAdapter()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setAdapter() {
        val menuAdapter = MenuAdapter(menuItems,requireContext())
        binding.menuRv.layoutManager = LinearLayoutManager(context)
        binding.menuRv.adapter = menuAdapter
    }

    companion object {

    }
}