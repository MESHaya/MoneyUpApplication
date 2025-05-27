package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class BudgetActivity : AppCompatActivity() {


    private lateinit var etBudgetName: EditText
    private lateinit var etMinBudget: EditText
    private lateinit var etMaxBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        etBudgetName = findViewById(R.id.et_budget_name)
        etMinBudget = findViewById(R.id.et_min_budget)
        etMaxBudget = findViewById(R.id.et_max_budget)
        btnSaveBudget = findViewById(R.id.btn_save_budget)

        database = FirebaseDatabase.getInstance().getReference("budget")


        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        setupBottomNav()
    }
    private fun saveBudget() {
        val monthlyBudgetName = etBudgetName.text.toString()
        val minAmount = etMinBudget.text.toString().toDoubleOrNull()
        val maxAmount = etMaxBudget.text.toString().toDoubleOrNull()



        if (monthlyBudgetName.isEmpty() || minAmount == null || maxAmount == null) {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (minAmount > maxAmount) {
            Toast.makeText(
                this,
                "Minimum budget cannot be greater than maximum budget",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Generate unique budget ID
        val budgetId = database.push().key ?: return

        // Assume default user or get from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"

        // Get current month
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault()) ?: "Unknown"


        // Calculate total
        val total = (minAmount + maxAmount).toInt()

        val budget = Budget(
            budget_id = budgetId,
            user_id = userId,
            month = month,
            total_budget = total,
            min_amount = minAmount,
            max_amount = maxAmount
        )

        database.child(budgetId).setValue(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save budget: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

      private  fun setupBottomNav() {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

            bottomNav.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_budget -> true
                    R.id.nav_expenses -> {
                        startActivity(Intent(this, AllExpensesActivity::class.java))
                        true
                    }

                    R.id.nav_home -> {
                        startActivity(Intent(this, HomepageActivity::class.java))
                        true
                    }

                    R.id.nav_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }

                    R.id.nav_settings -> {
                        startActivity(Intent(this, SettingActivity::class.java))
                        true
                    }

                    else -> false
                }
            }

            bottomNav.selectedItemId = R.id.nav_budget
        }
    }

