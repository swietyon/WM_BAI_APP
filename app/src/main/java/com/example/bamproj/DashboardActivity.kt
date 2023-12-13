package com.example.bamproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Przykładowy tekst powitalny
        val textViewWelcome: TextView = findViewById(R.id.textViewWelcome)
        textViewWelcome.text = "Witaj, ${getUserEmail()}!"

        // Przycisk do wylogowania
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun getUserEmail(): String {
        // Pobierz e-mail użytkownika z zapisanych danych lub innego miejsca
        // Tutaj zakładam, że e-mail został zapisany w SharedPreferences pod kluczem "LOGIN"
        val sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("LOGIN", "") ?: ""
    }

    private fun performLogout() {
        // Usuń zapisane dane (możesz doprecyzować, co chcesz usunąć)
        val sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("LOGIN")
        editor.remove("PASSWORD")
        editor.apply()

        // Przejdź z powrotem do ekranu logowania
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Opcjonalnie możesz zakończyć bieżącą aktywność, aby użytkownik nie mógł wrócić przyciskiem cofania
    }
}



