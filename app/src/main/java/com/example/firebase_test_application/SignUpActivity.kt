package com.example.firebase_test_application

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class SignUpActivity : AppCompatActivity() {

    // Reference to the "users" node in Firebase Realtime Database
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Optional: makes app content extend into system bars
        setContentView(R.layout.activity_sign_yp) // Ensure layout file name is correct!

        // Get references to UI elements
        val nameInput = findViewById<EditText>(R.id.name_edit)
        val surnameInput = findViewById<EditText>(R.id.surname_edit)
        val emailInput = findViewById<EditText>(R.id.email_edit)
        val usernameInput = findViewById<EditText>(R.id.username_edit)
        val passwordInput = findViewById<EditText>(R.id.password_edit)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirm_password_edit)
        val signUpButton = findViewById<Button>(R.id.sign_up_button)

        // Initialize Firebase database reference pointing to "users" node
        database = FirebaseDatabase.getInstance().getReference("users")

        // Handle Sign Up button click
        signUpButton.setOnClickListener {
            // Read and trim input values
            val name = nameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validate inputs
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if email looks valid
            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a unique ID for the user (you could also use push() to let Firebase auto-generate it)
            val userId = UUID.randomUUID().toString()

            // Create a user object
            val userData = Users(
                user_id = userId,
                username = username,
                password = password,
                email = email,
                name = name,
                surname = surname
            )

            // Save the user data to Firebase under the unique userId
            database.child(userId).setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    // Optionally: redirect to login screen here
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create account: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
