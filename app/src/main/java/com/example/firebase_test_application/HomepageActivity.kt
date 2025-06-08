package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.VideoView
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
import android.os.Handler
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


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
                R.id.nav_rewards -> {
                    startActivityWithAnimation(RewardsActivity::class.java)
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
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val selectedMonthIndex = months.indexOf(month) + 1

        val categoryTotals = HashMap<String, Double>()
        Log.d("CategoryTotals", categoryTotals.toString())


        val uid = auth.currentUser?.uid ?: return
        Log.d("UID_CHECK", "Querying for UID: $uid")


        val expensesRef = dbRef.child("expenses")
        expensesRef.orderByChild("user_id").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FIREBASE", "onDataChange triggered")



                    for (expenseSnap in snapshot.children) {
                        Log.d("FIREBASE", "ForLoop triggered")


                        val expense = expenseSnap.getValue(Expense::class.java)
                        Log.d("DEBUG", "Expense snapshot count: ${snapshot.childrenCount}")
                        Log.d("DEBUG", "Expense: $expenseSnap")
                        Log.d("DEBUG", "Expense ${expenseSnap.key} skipped")
                        if (expense != null) {
                            try {
                                //Parse the expense date
                                val date = LocalDate.parse(
                                    expense.date,
                                    DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)

                                )
                                if (date.monthValue == selectedMonthIndex) {
                                    categoryTotals[expense.category] =
                                        categoryTotals.getOrDefault(
                                            expense.category,
                                            0.0
                                        ) + expense.amount
                                }
                            } catch (e: Exception) {
                                Log.e("DateParseError", "Could not parse date: ${expense.date}", e)

                            }
                        }
                    }
                    val budgetsRef = dbRef.child("budget")
                    budgetsRef.orderByChild("user_id").equalTo(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(budgetSnapshot: DataSnapshot) {

                                Log.e("DEBUG", "Budget - OnDataChange triggered")
                                var min_amount: Float? = null
                                var max_amount: Float? = null

                                for (budgetSnap in budgetSnapshot.children) {
                                    val budgetMonth =
                                        budgetSnap.child("month").getValue(String::class.java)
                                    if (budgetMonth.equals(month, ignoreCase = true)) {
                                        min_amount = budgetSnap.child("min_amount")
                                            .getValue(Double::class.java)?.toFloat()
                                        Log.d(
                                            "DEBUG",
                                            "Budgetmin snapshot count: ${snapshot.childrenCount}"
                                        )
                                        Log.d("DEBUG", "Budget: $budgetSnap")


                                        max_amount = budgetSnap.child("max_amount")
                                            .getValue(Double::class.java)?.toFloat()
                                        Log.d(
                                            "DEBUG",
                                            "BudgetMax snapshot count: ${snapshot.childrenCount}"
                                        )
                                        Log.d("DEBUG", "Budget: $budgetSnap")
                                        break
                                    }
                                }

                                showLineGraph(categoryTotals, min_amount, max_amount)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("BUDGET_FETCH", "Error fetching budget: ${error.message}")
                                showLineGraph(categoryTotals, null, null) // Fallback
                            }
                        })


                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }



    private fun showLineGraph(categoryTotals: Map<String, Double>,min_amount: Float?, max_amount: Float?) {
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


        lineChart.axisRight.isEnabled = false

        val leftAxis = lineChart.axisLeft
        leftAxis.removeAllLimitLines()

// Set Y-axis min/max range to ensure budget lines are visible
        val allValues = categoryTotals.values.map { it.toFloat() }.toMutableList()

        min_amount?.let { allValues.add(it) }
        max_amount?.let { allValues.add(it) }

        if (allValues.isNotEmpty()) {
            val yMin = (allValues.minOrNull() ?: 0f) * 0.9f
            val yMax = (allValues.maxOrNull() ?: 0f) * 1.1f
            leftAxis.axisMinimum = yMin
            leftAxis.axisMaximum = yMax

        }

// Now add the lines
        min_amount?.let {
            val minLine = LimitLine(it, "Min Budget")
            minLine.lineWidth = 2f
            minLine.lineColor = ColorTemplate.COLORFUL_COLORS[0]
            minLine.textColor = ColorTemplate.COLORFUL_COLORS[0]
            leftAxis.addLimitLine(minLine)
        }

        max_amount?.let {
            val maxLine = LimitLine(it, "Max Budget")
            maxLine.lineWidth = 2f
            maxLine.lineColor = ColorTemplate.COLORFUL_COLORS[1]
            maxLine.textColor = ColorTemplate.COLORFUL_COLORS[1]
            leftAxis.addLimitLine(maxLine)
        }


        lineChart.invalidate()
    }
}
