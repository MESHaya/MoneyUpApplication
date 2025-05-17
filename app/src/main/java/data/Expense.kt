package com.example.firebase_test_application

data class Expense(
    val expenseId: String = "",
    val user_id: String = "",
    val expenseName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val imageUrl: String = ""
)
