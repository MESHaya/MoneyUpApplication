package com.example.firebase_test_application

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CategoryActivity : AppCompatActivity() {
    // Declare UI elements
    private lateinit var categoryNameEditText: EditText
    private lateinit var categoryDescriptionEditText: EditText
    private lateinit var colorSpinner: Spinner
    private lateinit var iconSpinner: Spinner
    private lateinit var addCategoryButton: Button

    private lateinit var database: DatabaseReference // Firebase DB reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views FIRST
        categoryNameEditText = findViewById(R.id.et_category_name)
        categoryDescriptionEditText = findViewById(R.id.et_category_description)
        colorSpinner = findViewById(R.id.spinner_color)
        iconSpinner = findViewById(R.id.spinner_icon)
        addCategoryButton = findViewById(R.id.btn_add_category)

        // Then set up the adapters for spinners
        val colors = arrayOf("Red", "Green", "Blue", "Orange")
        val icons = arrayOf("Star", "Heart", "Book", "Work")

        colorSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors)
        iconSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, icons)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("categories")

        addCategoryButton.setOnClickListener {
            val name = categoryNameEditText.text.toString().trim()
            val description = categoryDescriptionEditText.text.toString().trim()
            val color = colorSpinner.selectedItem.toString()
            val icon = iconSpinner.selectedItem.toString()

            if (name.isNotEmpty() && description.isNotEmpty()) {
                val category_id = database.push().key
                val user_id = 123 // Replace with actual user ID if using Firebase Auth

                val categoryData = Category(
                    category_id = 0, // Local ID — not used for Firebase key
                    user_id = user_id,
                    category_name = name,
                    category_description = description,
                    category_color = color,
                    category_icon = icon
                )

                if (category_id != null) {
                    database.child(category_id).setValue(categoryData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                            categoryNameEditText.text.clear()
                            categoryDescriptionEditText.text.clear()
                            colorSpinner.setSelection(0)
                            iconSpinner.setSelection(0)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
