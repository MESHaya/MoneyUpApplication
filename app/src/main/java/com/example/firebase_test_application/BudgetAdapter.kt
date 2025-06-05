package com.example.firebase_test_application


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class BudgetAdapter : ListAdapter<Budget, BudgetAdapter.BudgetViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Budget>() {
            override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
                return oldItem.budget_id == newItem.budget_id
            }

            override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val budgetInfoTextView: TextView = itemView.findViewById(R.id.tv_budget_info)

    }

    override fun onBindViewHolder(holder:BudgetViewHolder, position: Int) {
        val budget = getItem(position)
        holder.budgetInfoTextView.text =
            "${budget.budgetName} |${budget.month} | R${budget.min_amount} | R${budget.max_amount}"


    }
}
