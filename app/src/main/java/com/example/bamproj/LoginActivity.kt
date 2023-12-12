package com.example.bamproj

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bamproj.services.SharedPreferencesManager
import com.example.bamproj.services.UserService
import kotlinx.coroutines.launch
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var userService: UserService
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        rememberMeCheckbox = findViewById(R.id.checkBoxRememberMe)
        userService = UserService((applicationContext as BamApplication).database.userDao()) // Inicjalizacja serwisu
        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        loginButton.setOnClickListener {
            performLogin()
        }

        signUpButton.setOnClickListener {
            changeToSignUpActivity()
        }

        val rememberedLogin = sharedPreferences.getString("LOGIN", null);
        if (rememberedLogin != null) {
            showToast("Zalogowano bez logowania!")
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

    private fun changeToSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun performLogin() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            lifecycleScope.launch {
                val isValid: Boolean = userService.validatePassword(email, password)
                if (isValid) {
                    showToast(rememberMeCheckbox.isChecked.toString())
                    if (rememberMeCheckbox.isChecked) {
                        editor.putString("LOGIN", email)
                        editor.putString("PASSWORD", password)
                        editor.apply()
                    }
                    showToast("Poprawny login i hasło!")
                } else {
                    showToast("Niepoprawny login bądź hasło")
                }
            }
        } else {
            showToast("Please wprowadz login i haslo")
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
