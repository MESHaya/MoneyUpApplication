package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        database = FirebaseDatabase.getInstance().getReference("budget")
        fetchBudget()





        //  Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> true
                R.id.nav_home -> {
                    val intent = Intent(this@ProfileActivity, HomepageActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_budget -> {
                    val intent = Intent(this@ProfileActivity, BudgetActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_expenses -> {
                    val intent = Intent(this@ProfileActivity, AllExpensesActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this@ProfileActivity, SettingActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_profile
    }

    private fun fetchBudget() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        database.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val budget = snapshot.child("amount").getValue(Double::class.java)
                // You can display this budget value in a TextView or log it for now
                android.util.Log.d("Budget", "Fetched budget: R $budget")
            } else {
                android.util.Log.d("Budget", "No budget found for user")
            }
        }.addOnFailureListener { error ->
            android.util.Log.e("Firebase", "Failed to fetch budget", error)
        }
    }

}
