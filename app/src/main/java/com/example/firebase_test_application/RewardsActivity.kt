package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RewardsActivity : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rewards)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            checkAndAwardFirstExpenseBadge(userId)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }

        setUpBottomNav()
    }


    private fun checkAndAwardFirstExpenseBadge(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val expensesRef = database.getReference("expenses")
        val badgesRef = database.getReference("badges").child(userId)

        // Query to check if the user already has expenses
        expensesRef.orderByChild("user_id").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenseCount = snapshot.childrenCount

                    if (expenseCount == 1L) { // just added their first one
                        badgesRef.child("firstExpense").setValue(true)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "🎉 You've earned your First Expense badge!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BADGE", "Failed to check expenses: ${error.message}")
                }
            })
    }


    private fun setUpBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_rewards -> true
                R.id.nav_home -> {
                    val intent = Intent(this@RewardsActivity, HomepageActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@RewardsActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }


                R.id.nav_budget -> {
                    val intent = Intent(this@RewardsActivity, BudgetActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@RewardsActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                R.id.nav_expenses -> {
                    val intent = Intent(this@RewardsActivity, AllExpensesActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@RewardsActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                R.id.nav_rewards -> {
                    val intent = Intent(this@RewardsActivity, RewardsActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@RewardsActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_rewards
    }
}
