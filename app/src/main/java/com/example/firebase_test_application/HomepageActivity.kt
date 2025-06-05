package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.calculatoractivity.CalculatorActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomepageActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var spinnerMonth: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)

        // UI element initializations
        val add_expenseBTN = findViewById<Button>(R.id.add_expenseBTN)
        val add_categoryBTN = findViewById<Button>(R.id.btn_add_categoryBTN)
        val calcIcon = findViewById<ImageView>(R.id.calc_icon)
        lineChart = findViewById(R.id.lineChart)
        spinnerMonth = findViewById(R.id.spinner_month)

        // Firebase instances
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // Expense button
        add_expenseBTN.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        // Category button
        add_categoryBTN.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        // Calculator icon
        calcIcon.setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        // Spinner setup
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedMonth = months[position]
                retrieveDataForMonth(selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_expenses -> {
                    startActivityWithAnimation(AllExpensesActivity::class.java)
                    true
                }
                R.id.nav_budget -> {
                    startActivityWithAnimation(BudgetActivity::class.java)
                    true
                }
                R.id.nav_profile -> {
                    startActivityWithAnimation(ProfileActivity::class.java)
                    true
                }
                R.id.nav_settings -> {
                    startActivityWithAnimation(SettingActivity::class.java)
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_home
    }

    private fun startActivityWithAnimation(target: Class<*>) {
        val intent = Intent(this, target)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
    }

    private fun retrieveDataForMonth(month: String) {
        val categoryTotals = HashMap<String, Double>()
        var minGoal = 0.0
        var maxGoal = 0.0

        val uid = auth.currentUser?.uid ?: return
        val budgetRef = dbRef.child("budget")

        budgetRef.orderByChild("user_id").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (budgetSnap in snapshot.children) {
                        val budget = budgetSnap.getValue(Budget::class.java)
                        if (budget != null && budget.month == month) {
                            minGoal = budget.min_amount
                            maxGoal = budget.max_amount
                            break
                        }
                    }

                    val expensesRef = dbRef.child("expense")
                    expensesRef.orderByChild("user_id").equalTo(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (expenseSnap in snapshot.children) {
                                    val expense = expenseSnap.getValue(Expense::class.java)
                                    if (expense != null && expense.date.contains(month, true)) {
                                        categoryTotals[expense.category] =
                                            categoryTotals.getOrDefault(expense.category, 0.0) + expense.amount
                                    }
                                }
                                showLineGraph(categoryTotals, minGoal, maxGoal)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showLineGraph(categoryTotals: Map<String, Double>, minGoal: Double, maxGoal: Double) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        categoryTotals.entries.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        val lineDataSet = LineDataSet(entries, "Category Totals").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            setDrawValues(true)
            setDrawCircles(true)
        }

        lineChart.data = LineData(lineDataSet)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return labels.getOrNull(value.toInt()) ?: ""
            }
        }

        val leftAxis = lineChart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.addLimitLine(LimitLine(minGoal.toFloat(), "Min Goal"))
        leftAxis.addLimitLine(LimitLine(maxGoal.toFloat(), "Max Goal"))

        lineChart.axisRight.isEnabled = false
        lineChart.invalidate()
    }
}
