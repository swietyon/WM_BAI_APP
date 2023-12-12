package com.example.bamproj

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bamproj.services.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var userService: UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)

        userService = UserService((applicationContext as BamApplication).database.userDao()) // Inicjalizacja serwisu

        loginButton.setOnClickListener {
            goToLoginActivity()
        }
        signUpButton.setOnClickListener {
            lifecycleScope.launch {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    lifecycleScope.launch {
                        if (userService.isUserNameExists(email)) {
                            showToast("Użytkownik o podanej nazwie już istnieje")
                        } else {
                            val success = userService.registerUser(email, password, "", "")
                            if (success) {
                                showToast("Użytkownik został zarejestrowany")
                                goToLoginActivity()
                            } else {
                                showToast("Wystąpił problem podczas rejestracji użytkownika")
                            }
                        }
                    }
                } else {
                    showToast("Wypełnij oba pola: email i hasło")
                }
            }
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

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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
