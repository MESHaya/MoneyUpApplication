package com.example.firebase_test_application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity(){

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    // Find buttons by their IDs
    val signUpBtn = findViewById<Button>(R.id.Sign_up)
    val loginBtn = findViewById<Button>(R.id.Log_in)

    // Handle Sign Up button click
    signUpBtn.setOnClickListener{
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    // Handle Log In button click
    loginBtn.setOnClickListener{
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
}
