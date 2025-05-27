package com.example.firebase_test_application

import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import data.CategoryNameTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AllExpensesActivity : AppCompatActivity() {

    private lateinit var pickDateButton: Button
    private lateinit var applyFilterButton: Button
    private lateinit var clearFiltersButton: Button
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryTotalsAdapter: CategoryTotalsAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private var startDate: String? = null
    private var endDate: String? = null

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)

        // Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // UI setup
        pickDateButton = findViewById(R.id.btn_pick_date)
        applyFilterButton = findViewById(R.id.btn_filter)
        clearFiltersButton = findViewById(R.id.clear_filters_button)
        expensesRecyclerView = findViewById(R.id.recycler_expenses)
        val addExpenseButton = findViewById<Button>(R.id.add_expenseBTN)

        // Expense list
        expenseAdapter = ExpenseAdapter()
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)
        expensesRecyclerView.adapter = expenseAdapter

        // Category totals
        categoryTotalsAdapter = CategoryTotalsAdapter()
        val categoryTotalsRecyclerView = findViewById<RecyclerView>(R.id.recycler_category_totals)
        categoryTotalsRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryTotalsRecyclerView.adapter = categoryTotalsAdapter

        // Button Listeners
        pickDateButton.setOnClickListener { openDateRangePicker() }
        applyFilterButton.setOnClickListener {
            loadExpenses()
            loadCategoryTotals()
        }
        clearFiltersButton.setOnClickListener {
            clearFilters()
            loadExpenses()
            loadCategoryTotals()
        }
        addExpenseButton.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        setupBottomNavigation()
    }

    private fun loadExpenses() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            val expenses = mutableListOf<Expense>()
            val expensesRef = dbRef.child("expenses").child(uid)


            expensesRef.get().addOnSuccessListener { snapshot ->
                Log.d("Expenses", "Snapshot has ${snapshot.childrenCount} items")
                if (!snapshot.exists()) {
                    Log.d("Expenses", "No expenses found in the database")
                }
                for (child in snapshot.children) {
                    Log.d("Child", child.toString())
                    val expense = child.getValue(Expense::class.java)
                    if (expense != null) {
                        Log.d("Parsed Expense", expense.toString())
                        expenses.add(expense)
                    } else {
                        Log.d("Expense", "Null after parsing")
                    }
                }
                Log.d("Expenses", "Total expenses parsed: ${expenses.size}")
                expenseAdapter.submitList(expenses)
            }.addOnFailureListener {
                Toast.makeText(
                    this@AllExpensesActivity,
                    "Failed to load expenses",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Firebase", "Error loading expenses", it)
            }
        }
    }

            private fun loadCategoryTotals() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            val categoryMap = mutableMapOf<String, Double>()
            val expensesRef = dbRef.child("expenses").child(uid)

            expensesRef.get().addOnSuccessListener { snapshot ->
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    if (expense != null && (startDate == null || endDate == null || isWithinSelectedRange(expense.date))) {
                        categoryMap[expense.category] = categoryMap.getOrDefault(expense.category, 0.0) + expense.amount
                    }
                }

                val categoryTotals = categoryMap.map { CategoryNameTotal(it.key, it.value) }

                categoryTotalsAdapter.setData(categoryTotals)

            }.addOnFailureListener {
                Toast.makeText(this@AllExpensesActivity, "Failed to load category totals", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Error loading categories", it)
            }
        }
    }

    private fun openDateRangePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val pickedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            if (startDate == null) {
                startDate = pickedDate
                Toast.makeText(this, "Start Date Selected: $startDate", Toast.LENGTH_SHORT).show()
            } else {
                endDate = pickedDate
                Toast.makeText(this, "End Date Selected: $startDate to $endDate", Toast.LENGTH_SHORT).show()
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun clearFilters() {
        startDate = null
        endDate = null
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show()
    }

    private fun isWithinSelectedRange(dateString: String): Boolean {
        if (startDate == null && endDate == null) return true
        val date = dateFormatter.parse(dateString) ?: return false
        val start = dateFormatter.parse(startDate!!) ?: return false
        val end = dateFormatter.parse(endDate!!) ?: return false
        return (date == start || date == end || (date.after(start) && date.before(end)))
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_expenses

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomepageActivity::class.java),
                        ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java),
                        ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java),
                        ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingActivity::class.java),
                        ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
        loadCategoryTotals()
    }
}
