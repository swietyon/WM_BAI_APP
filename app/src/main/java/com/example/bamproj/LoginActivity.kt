package com.example.bamproj

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            performLogin()
        }

        editTextEmail.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {

                editTextPassword.requestFocus()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // hide keyboard
                hideKeyboard()
            }
            return@setOnTouchListener false
        }
    }

    private fun performLogin() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            // for now toast communicate
            showToast("Logging in...\nEmail: $email\nPassword: $password")
        } else {
            showToast("Please enter both email and password")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
