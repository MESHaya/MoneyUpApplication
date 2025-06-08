package com.example.firebase_test_application

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import data.Badge

class RewardsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rewards)

        setUpBottomNav()



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
