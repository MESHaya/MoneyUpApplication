package com.example.firebase_test_application

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ExpenseActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            findViewById<ImageView>(R.id.photo_preview).setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        database = FirebaseDatabase.getInstance().getReference("expenses")
        auth = FirebaseAuth.getInstance()

        previewView = findViewById(R.id.preview_view)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)
        val categorySpinner = findViewById<Spinner>(R.id.spinner_category)
        val categoryNames = mutableListOf<String>()

        val categoriesRef = FirebaseDatabase.getInstance().getReference("categories")
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    val categoryName =
                        categorySnapshot.child("category_name").getValue(String::class.java)
                    categoryName?.let { categoryNames.add(it) }
                }

                val adapter = ArrayAdapter(
                    this@ExpenseActivity,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ExpenseActivity,
                    "Failed to load categories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val uploadButton = findViewById<Button>(R.id.upload_photo_button)
        val takePhotoButton = findViewById<Button>(R.id.take_photo_button)
        val saveButton = findViewById<Button>(R.id.btn_save_expense)
        val dateInput = findViewById<EditText>(R.id.et_date)
        val startTimeInput = findViewById<EditText>(R.id.et_start_time)
        val endTimeInput = findViewById<EditText>(R.id.et_end_time)
        val amountInput = findViewById<EditText>(R.id.et_amount)
        val descriptionInput = findViewById<EditText>(R.id.et_description)
        val expenseNameInput = findViewById<EditText>(R.id.et_expense_name)

        backButton.setOnClickListener { finish() }
        uploadButton.setOnClickListener { pickImageLauncher.launch("image/*") }
        takePhotoButton.setOnClickListener { takePhoto() }
        dateInput.setOnClickListener { showDatePickerDialog(dateInput) }
        startTimeInput.setOnClickListener { showTimePickerDialog(startTimeInput) }
        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }

        saveButton.setOnClickListener {
            val expenseName = expenseNameInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val startTime = startTimeInput.text.toString().trim()
            val endTime = endTimeInput.text.toString().trim()
            val amount = amountInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val category = categorySpinner.selectedItem?.toString()?.trim() ?: ""

            if (expenseName.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()
                || amount.isEmpty() || description.isEmpty() || category.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountValue = amount.toDoubleOrNull()
            if (amountValue == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expenseId = database.push().key ?: UUID.randomUUID().toString()
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                expenseId = expenseId,
                user_id = uid,
                expenseName = expenseName,
                date = date,
                startTime = startTime,
                endTime = endTime,
                amount = amountValue,
                description = description,
                category = category,
                imageUrl = imageUri?.toString() ?: ""
            )

            database.child(expenseId).setValue(expense)
                .addOnSuccessListener {
                    updateCurrentSpending(uid, amountValue)

                    Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()

                    val party = Party(
                        speed = 10f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                        emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(50),
                        position = Position.Relative(0.5, 0.3)
                    )

                    konfettiView.bringToFront()
                    konfettiView.invalidate()
                    konfettiView.start(party)

                    Handler(mainLooper).postDelayed({
                        finish()
                    }, 5500)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failed to save expense: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateCurrentSpending(userId: String, newAmount: Double) {
        val userTotalRef = FirebaseDatabase.getInstance().getReference("user_totals").child(userId)
        userTotalRef.child("currentSpending").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentSpending = mutableData.getValue(Double::class.java) ?: 0.0
                mutableData.value = currentSpending + newAmount
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(applicationContext, "Failed to update total: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    imageUri = savedUri
                    findViewById<ImageView>(R.id.photo_preview).setImageURI(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@ExpenseActivity,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showDatePickerDialog(dateInput: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = String.format(
                    "%02d %s %04d",
                    dayOfMonth,
                    calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()),
                    year
                )
                dateInput.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(timeInput: EditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hour, minute ->
                timeInput.setText(String.format("%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
