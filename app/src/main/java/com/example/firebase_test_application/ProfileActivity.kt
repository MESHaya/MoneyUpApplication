package com.example.firebase_test_application

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebase_test_application.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //connect to db and auth
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference


        setUpRecyclerView()
        fetchBudget()
        setUpBottomNav()
    }

    private fun setUpRecyclerView(){
        budgetAdapter = BudgetAdapter()
        binding.activeBudgetsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activeBudgetsRecyclerView.adapter = budgetAdapter
    }

    private fun fetchBudget() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val budgetsRef = FirebaseDatabase.getInstance().getReference("budget")

        budgetsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val budgetList = mutableListOf<Budget>()

                for (budgetSnap in snapshot.children){
                    val budget = budgetSnap.getValue(Budget ::class.java)
                    if(budget != null && budget.user_id == uid){
                        budgetList.add(budget)
                    }
                }
                budgetAdapter.submitList(budgetList)
            }
            override fun onCancelled( error: DatabaseError){
                Log.e("ProfileActivity,","Database read failed: ${error.message}")
            }
        })
    }



    private fun setUpBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> true
                R.id.nav_home -> {
                    val intent = Intent(this@ProfileActivity, HomepageActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                R.id.nav_budget -> {
                    val intent = Intent(this@ProfileActivity, BudgetActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                R.id.nav_expenses -> {
                    val intent = Intent(this@ProfileActivity, AllExpensesActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                R.id.nav_settings -> {
                    val intent = Intent(this@ProfileActivity, SettingActivity::class.java)
                    val options =
                        ActivityOptions.makeCustomAnimation(this@ProfileActivity, 0, 0)
                    startActivity(intent, options.toBundle())
                    true
                }

                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_profile
    }
}

