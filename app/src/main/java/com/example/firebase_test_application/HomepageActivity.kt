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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomepageActivity : AppCompatActivity() {

    //add variables for graph
    private lateinit var lineChart: LineChart
    private lateinit var spinnerMonth: Spinner
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String //*replace with actual user session logic*


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)

        //declare ui elements
        val add_expenseBTN = findViewById<Button>(R.id.add_expenseBTN)

        val add_categoryBTN = findViewById<Button>(R.id.btn_add_categoryBTN)

        val calcIcon = findViewById<ImageView>(R.id.calc_icon)


// Add expense button functionality
        add_expenseBTN.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        //add category button functionality
        //when users click on icon they are taken to the Calculator Page
        add_categoryBTN.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        //add calc functionality
        calcIcon.setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        }

        //set up spinner for month selection from user
        val spinnerMonth = findViewById<Spinner>(R.id.spinner_month)    //layout
        //define months in list of values
        val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        //ArrayAdapter is connection between months and spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapter

        //user selection - event handling
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //method one - gets month selected by its position and calls other method for graph (retrieveDataForMonth)
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long)
            {
                val selectedMonth = months[position]
                retrieveDataForMonth(selectedMonth)
            }
            //method two - interface is empty when nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_expenses-> {
                    val intent = Intent(this, AllExpensesActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_budget -> {
                    val intent = Intent(this, BudgetActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                else -> false
            }
        }

        // Highlight current tab
        bottomNav.selectedItemId = R.id.nav_home
    }

    //method for graph
    private fun retrieveDataForMonth(month: String) {
        val categoryTotals = HashMap<String, Double>()
        var minGoal = 0.0
        var maxGoal = 0.0

        dbRef.child("budgets").orderByChild("user_id").equalTo(userId)
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

                    dbRef.child("expenses").orderByChild("user_id").equalTo(userId)
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

    //method
    private fun showLineGraph(categoryTotals: Map<String, Double>, minGoal: Double, maxGoal: Double) {
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        categoryTotals.entries.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key)
        }

        val lineDataSet = LineDataSet(entries, "Category Totals")
        lineDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        lineDataSet.setDrawValues(true)
        lineDataSet.setDrawCircles(true)

        val data = LineData(lineDataSet)
        lineChart.data = data

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        //xAxis.setValueFormatter { value, _ -> labels.getOrNull(value.toInt()) ?: "" }
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
