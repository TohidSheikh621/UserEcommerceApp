package com.example.userkabirstore.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.userkabirstore.databinding.CategoryListRvBinding
import com.example.userkabirstore.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit,
// Pass the click listener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: CategoryListRvBinding  ) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CategoryListRvBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        // Set the category name and image (if applicable)
        holder.binding.categoryNameTv.text = category.name

        holder.binding.categoryImg.setImageResource(category.imageUrl).toString()

        // Handle item click
        holder.itemView.setOnClickListener {
            onCategoryClick(category)  // Trigger the click event
        }
    }

    override fun getItemCount(): Int = categories.size
}
