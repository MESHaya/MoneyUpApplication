package com.example.firebase_test_application

import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebase_test_application.databinding.ActivityAllExpensesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import data.CategoryNameTotal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AllExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllExpensesBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryTotalsAdapter: CategoryTotalsAdapter

    private var expensesList = mutableListOf<Expense>()

    private var startDate: String? = null
    private var endDate: String? = null

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        dbRef = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        setupCategoryTotals()
        setupButtons()
        setupBottomNavigation()


    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter()
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(this)
        binding.recyclerExpenses.adapter = expenseAdapter
    }

    private fun setupCategoryTotals() {
        categoryTotalsAdapter = CategoryTotalsAdapter()
        binding.recyclerCategoryTotals.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategoryTotals.adapter = categoryTotalsAdapter
    }

    private fun setupButtons() {
        binding.btnPickDate.setOnClickListener { openDateRangePicker() }
        binding.btnFilter.setOnClickListener {
            loadExpenses()
            loadCategoryTotals()
        }
        binding.clearFiltersButton.setOnClickListener {
            clearFilters()
            loadExpenses()
            loadCategoryTotals()
        }
        binding.addExpenseBTN.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }
    }

    private fun loadExpenses() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val expensesRef = FirebaseDatabase.getInstance().getReference("expenses")

        expensesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expensesList = mutableListOf<Expense>()

                for (expenseSnap in snapshot.children) {
                    val expense = expenseSnap.getValue(Expense::class.java)
                    if (expense != null && expense.user_id == uid) {
                        expensesList.add(expense)
                    }
                }


                expenseAdapter.submitList(expensesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AllExpensesActivity", "Database read failed: ${error.message}")
            }
        })
    }



        private fun loadCategoryTotals() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val expensesRef = FirebaseDatabase.getInstance().getReference("expenses")

            expensesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryTotals = mutableMapOf<String, Double>()  //use a map to store totals per category


                    for (expenseSnap in snapshot.children) {
                        val expense = expenseSnap.getValue(Expense::class.java)
                        if (expense != null && expense.user_id == uid) {
                            val category = expense.category
                            val amount = expense.amount
                            categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                        }
                    }

                   val categoryTotalList = categoryTotals.map{
                       CategoryNameTotal(it.key,it.value)
                   }
                    categoryTotalsAdapter.setData(categoryTotalList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CategoryTotals", "Database read failed: ${error.message}")
                }
            })

        }
            private fun openDateRangePicker() {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val pickedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    if (startDate == null) {
                        startDate = pickedDate
                        Toast.makeText(this, "Start Date Selected: $startDate", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        endDate = pickedDate
                        Toast.makeText(
                            this,
                            "End Date Selected: $startDate to $endDate",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
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
            binding.bottomNavigation.selectedItemId = R.id.nav_expenses

            binding.bottomNavigation.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_home -> {
                        startActivity(
                            Intent(this, HomepageActivity::class.java),
                            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                        )
                        true
                    }

                    R.id.nav_budget -> {
                        startActivity(
                            Intent(this, BudgetActivity::class.java),
                            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                        )
                        true
                    }

                    R.id.nav_profile -> {
                        startActivity(
                            Intent(this, ProfileActivity::class.java),
                            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                        )
                        true
                    }

                    R.id.nav_settings -> {
                        startActivity(
                            Intent(this, SettingActivity::class.java),
                            ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
                        )
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

