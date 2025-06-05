package com.example.firebase_test_application

data class Budget(
    val budget_id: String = "",
    val user_id: String = "",
    val budgetName: String = "",
    val month: String = "",
    val total_budget: Int = 0,
    val min_amount: Double = 0.0,
    val max_amount: Double = 0.0
)
