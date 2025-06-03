package com.example.firebase_test_application

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseAdapter : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem.expenseId == newItem.expenseId  
            }

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseInfoTextView: TextView = itemView.findViewById(R.id.tv_expense_info)
        val expenseImageView: ImageView = itemView.findViewById(R.id.iv_expense_image)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)
        holder.expenseInfoTextView.text =
            "${expense.expenseName} | R${expense.amount} | ${expense.date}"

        if (!expense.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(expense.imageUrl))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.expenseImageView)
        } else {
            holder.expenseImageView.setImageResource(R.drawable.placeholder)
        }
    }
}
