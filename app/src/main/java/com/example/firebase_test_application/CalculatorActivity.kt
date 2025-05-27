//declare package name of project
package com.example.calculatoractivity

//imports
import android.os.Bundle    //stores data between activities
import android.widget.Button    //ui
import android.widget.TextView  //ui
import androidx.activity.enableEdgeToEdge   //handle ui layout
import androidx.appcompat.app.AppCompatActivity //base clas for activities
import androidx.core.view.ViewCompat    //handle ui layout
import androidx.core.view.WindowInsetsCompat    //handle ui layout
import com.example.firebase_test_application.R

//MainActivity Class defined - extends AppCompat which gives access to core Android methods
//define class - inheriting functionality from AppCompat
class CalculatorActivity : AppCompatActivity() {
    //class variables
    private lateinit var txtResult: TextView
    private lateinit var currentInputTextView: TextView
    private var currentInput: String = ""
    private var operator: String? = null
    private var firstNumber: Double? = null
    //onCreate method - called when activity starts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //connect ui elements with ids to variables
        txtResult = findViewById(R.id.txtResult)
        currentInputTextView = findViewById(R.id.currentInput)

        //textview display and answer
        //val textViewAnswer = findViewById<TextView>(R.id.txtResult)
        //val textViewDisplay = findViewById<TextView>(R.id.currentInput)
        //connect buttons to views

        val buttonLeft = findViewById<Button>(R.id.btn_left)
        val buttonRight = findViewById<Button>(R.id.btn_right)
        val buttonDecimal = findViewById<Button>(R.id.btn_decimal)

        //number buttons
        val numberButtons = listOf(
            findViewById<Button>(R.id.btn0),
            findViewById<Button>(R.id.btn1),
            findViewById<Button>(R.id.btn2),
            findViewById<Button>(R.id.btn3),
            findViewById<Button>(R.id.btn4),
            findViewById<Button>(R.id.btn5),
            findViewById<Button>(R.id.btn6),
            findViewById<Button>(R.id.btn7),
            findViewById<Button>(R.id.btn8),
            findViewById<Button>(R.id.btn9)
        )

        //set click listeners for numbers
        numberButtons.forEach{
                button ->
            button.setOnClickListener{
                currentInput += button.text
                currentInputTextView.text = currentInput
            }
        }


        //operation buttons
        //when operator clicked, it saves the first number and clears the input
        val operatorButtons = mapOf(
            findViewById<Button>(R.id.btnAdd) to "+",
            findViewById<Button>(R.id.btnSubtract) to "-",
            findViewById<Button>(R.id.btnMultiply) to "*",
            findViewById<Button>(R.id.btnDivide) to "/"
        )
        operatorButtons.forEach{ (button, op) ->
            button.setOnClickListener{
                if(currentInput.isNotEmpty()){
                    firstNumber = currentInput.toDoubleOrNull()
                    operator = op
                    currentInput = ""
                    currentInputTextView.text = ""
                }
            }
        }


        //equal button
        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            //check if both numbers are valid
            if (firstNumber != null && currentInput.isNotEmpty()) {
                val secondNumber = currentInput.toDoubleOrNull()
                //error handling
                if (secondNumber == null) {
                    txtResult.text = "Invalid Input. Please enter a valid number."
                } else if (operator == "/" && secondNumber == 0.0) {
                    txtResult.text = "Undefined: Cannot divide by zero. Please try again."
                } else {
                    //
                    val result = when (operator) {
                        "+" -> firstNumber!! + secondNumber
                        "-" -> firstNumber!! - secondNumber
                        "*" -> firstNumber!! * secondNumber
                        "/" -> firstNumber!! / secondNumber
                        else -> 0.0
                    }
                    //display result
                    txtResult.text = "$result"
                }
                //reset
                firstNumber = null
                currentInput = ""
                operator = null
            }
        }

        //clear both textviews
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            currentInput = ""
            firstNumber = null
            operator = null
            currentInputTextView.text = ""
            txtResult.text = "0"
        }

        //add functionality for left bracket (
        buttonLeft.setOnClickListener {
            currentInput += "("
            currentInputTextView.text = currentInput
        }

        //add functionality for right bracket )
        buttonRight.setOnClickListener {
            currentInput += ")"
            currentInputTextView.text = currentInput
        }

        //add functionality for decimal point .
        buttonDecimal.setOnClickListener {
            if (currentInput.isEmpty() || currentInput.last() in listOf('+', '-', '*', '/', '(')) {
                currentInput += "0."
            } else if (!currentInput.takeLastWhile { it.isDigit() || it == '.' }.contains(".")) {
                currentInput += "."
            }
            currentInputTextView.text = currentInput
        }
    }
}


//buttonClear.setOnClickListener{
//     textViewDisplay.text = "";
//    textViewAnswer.text = "";
//}

//arithmetic operations
//buttonLeft.setOnClickListener{
//    textViewDisplay.append("(")
//}

//buttonRight.setOnClickListener{
//    textViewDisplay.append(")")
//}

