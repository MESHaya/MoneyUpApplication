package com.example.firebase_test_application

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    // Firebase Realtime Database reference
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes UI edge-to-edge if you use transparent status bars
        setContentView(R.layout.activity_login) // Set the layout of this activity

        // Link all UI elements from XML layout to Kotlin code
        val usernameInput = findViewById<EditText>(R.id.username)
        val passwordInput = findViewById<EditText>(R.id.password)
        val rememberMe = findViewById<CheckBox>(R.id.remember_me)
        val forgotPass = findViewById<TextView>(R.id.forgot_password)
        val loginBTN = findViewById<Button>(R.id.log_in_button)
        val signuplink = findViewById<TextView>(R.id.login_signup_link)

        // Point to the "users" node in your Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("users")

        // Handle the login button click
        loginBTN.setOnClickListener {
            // Get user input from EditTexts and remove any spaces around it
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate input
            if (username.isNotEmpty() && password.isNotEmpty()) {

                // Search Firebase DB for a user with the entered username
                database.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        // This function is called once Firebase returns a result
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // If a user with the entered username exists in DB
                            if (snapshot.exists()) {
                                var loginSuccess = false

                                // Go through each matching user (just in case there's more than one)
                                for (userSnapshot in snapshot.children) {
                                    val user = userSnapshot.getValue(Users::class.java)

                                    // Check if password matches
                                    if (user != null && user.password == password) {
                                        loginSuccess = true
                                        break // We found a match, no need to keep checking
                                    }
                                }

                                if (loginSuccess) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Login successful",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Go to MainActivity after successful login
                                    val intent = Intent(this@LoginActivity, HomepageActivity::class.java)
                                    startActivity(intent)
                                    finish() // Close LoginActivity so user can't go back using back button
                                } else {
                                    // Password is wrong
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Incorrect password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // Username not found in the database
                                Toast.makeText(
                                    this@LoginActivity,
                                    "User not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        // Called if Firebase has trouble reading the data
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Database error: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                // One or both input fields are empty
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle the "Sign up" link → takes user to the SignUpActivity screen
        signuplink.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
