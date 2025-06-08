package com.example.firebase_test_application

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import android.os.Environment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BudgetActivity : AppCompatActivity() {

    private lateinit var etBudgetName: EditText
    private lateinit var etMinBudget: EditText
    private lateinit var etMaxBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var btnExportExcel: Button

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        // Initialize UI components
        etBudgetName = findViewById(R.id.et_budget_name)
        etMinBudget = findViewById(R.id.et_min_budget)
        etMaxBudget = findViewById(R.id.et_max_budget)
        btnSaveBudget = findViewById(R.id.btn_save_budget)
        btnExportExcel = findViewById(R.id.btn_export_excel)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("budget")
        auth = FirebaseAuth.getInstance()


        btnExportExcel.setOnClickListener {
            val goal = etBudgetName.text.toString()
            val min = etMinBudget.text.toString()
            val max = etMaxBudget.text.toString()

            if (goal.isNotEmpty() && min.isNotEmpty() && max.isNotEmpty()) {
                exportToExcel(this, goal, min, max)
            } else {
                Toast.makeText(this, "Please fill in all fields before exporting", Toast.LENGTH_SHORT).show()
            }
        }

        // Example call to test the progress bar
       // setBudgetProgress(currentSpending = 1200.0, min = 2000.0, max = 10000.0)

        // Save budget button click listener
        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        fetchAndUpdateBudgetProgress()
        // Set up bottom navigation
        setupBottomNav()
    }

    private fun saveBudget() {
        val monthlyBudgetName = etBudgetName.text.toString()
        val minAmount = etMinBudget.text.toString().toDoubleOrNull()
        val maxAmount = etMaxBudget.text.toString().toDoubleOrNull()

        // Validation
        if (monthlyBudgetName.isEmpty() || minAmount == null || maxAmount == null) {
            Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (minAmount > maxAmount) {
            Toast.makeText(this, "Minimum budget cannot be greater than maximum budget", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetId = database.push().key ?: return
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val month = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Unknown"
        val total = (minAmount + maxAmount).toInt()

        val budget = Budget(
            budget_id = budgetId,
            user_id = uid,
            budgetName = monthlyBudgetName,
            month = month,
            total_budget = total,
            min_amount = minAmount,
            max_amount = maxAmount,
            current_spending = 0.0
        )

        // Save to Firebase
        database.child(budgetId).setValue(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save budget: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setBudgetProgress(currentSpending: Double, min: Double, max: Double) {
        val clampedSpending = currentSpending.coerceIn(min, max)
        val progress = ((clampedSpending - min) / (max - min) * 100).toInt()
        budgetProgressBar.progress = progress
        tvProgressPercent.text = "$progress% used"
    }

    private fun fetchAndUpdateBudgetProgress() {
        val uid = auth.currentUser?.uid ?: return

        // Step 1: Fetch budget
        database.orderByChild("user_id").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(budgetSnapshot: DataSnapshot) {
                    var latestBudget: Budget? = null
                    for (child in budgetSnapshot.children) {
                        val budget = child.getValue(Budget::class.java)
                        if (budget != null) {
                            latestBudget = budget
                            break  // You could change this logic if needed
                        }
                    }

                    if (latestBudget != null) {
                        // Step 2: Fetch currentSpending
                        val userTotalsRef = FirebaseDatabase.getInstance()
                            .getReference("user_totals")
                            .child(uid)
                            .child("currentSpending")

                        userTotalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(spendingSnapshot: DataSnapshot) {
                                val currentSpending = spendingSnapshot.getValue(Double::class.java) ?: 0.0
                                setBudgetProgress(
                                    currentSpending = currentSpending,
                                    min = latestBudget.min_amount,
                                    max = latestBudget.max_amount
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@BudgetActivity, "Failed to load spending", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BudgetActivity, "Failed to load budget", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun setupBottomNav() {
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
                R.id.nav_rewards -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_budget
    }

    // Function to create and save an Excel fileAdd commentMore actions
    fun exportToExcel(context: Context, goal: String, min: String, max: String) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Budget")

        // Header
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Goal")
        header.createCell(1).setCellValue("Min Budget")
        header.createCell(2).setCellValue("Max Budget")

        // Data row
        val row = sheet.createRow(1)
        row.createCell(0).setCellValue(goal)
        row.createCell(1).setCellValue(min)
        row.createCell(2).setCellValue(max)

        // File path
        val fileName = "BudgetData.xlsx"
        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            val outputStream = FileOutputStream(filePath)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

            Toast.makeText(context, "Excel saved: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save Excel", Toast.LENGTH_SHORT).show()
        }
    }
}

