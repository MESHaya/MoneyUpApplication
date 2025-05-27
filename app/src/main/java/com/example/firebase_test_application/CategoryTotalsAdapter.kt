package com.example.firebase_test_application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import data.CategoryNameTotal


class CategoryTotalsAdapter : RecyclerView.Adapter<CategoryTotalsAdapter.ViewHolder>() {

    private var totalsList = listOf<CategoryNameTotal>()

    fun setData(newList: List<CategoryNameTotal>) {
        totalsList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val totalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = totalsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = totalsList[position]
        holder.categoryName.text = item.category_name
        holder.totalAmount.text = "R ${String.format("%.2f", item.totalAmount)}"
    }
}
