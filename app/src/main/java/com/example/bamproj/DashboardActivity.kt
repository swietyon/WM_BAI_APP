package com.example.bamproj

import NoteAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class DashboardActivity : AppCompatActivity(), OnNoteClickListener {

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteList: MutableList<Note>
    private lateinit var userName: String
    private lateinit var noteDao: NoteDao

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent.hasExtra("USER_NAME")) {
            userName = intent.getStringExtra("USER_NAME")!!
        }
        noteDao = (applicationContext as BamApplication).database.noteDao()

        setContentView(R.layout.activity_dashboard)

        // Inicjalizacja RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewNotes)
        noteList = mutableListOf()

        lifecycleScope.launch {
            loadListByUserName(noteList, noteDao, userName);
        }

        noteAdapter = NoteAdapter(noteList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteAdapter

        // Przykładowy tekst powitalny
        val textViewWelcome: TextView = findViewById(R.id.textViewWelcome)
        textViewWelcome.text = "Witaj, ${userName}!"

        // Przycisk do wylogowania
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            performLogout()
        }

        // Przycisk do dodawania notatki
        val addNoteButton: Button = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            addNote(noteDao, userName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadListByUserName(noteList: MutableList<Note>, noteDao: NoteDao, userName: String) {
        return withContext(Dispatchers.IO) {
            val result = noteDao.getNoteByUsername(userName)
            for (singleElem in result) {
                noteList.add(
                    Note(
                        title = singleElem.title, content = singleElem.content,
                        date = Date.from(singleElem.creationTime.toInstant(ZoneOffset.ofHours(1)))
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNote(noteDao: NoteDao, userName: String) {
        val titleEditText: EditText = findViewById(R.id.editTextNoteTitle)
        val contentEditText: EditText = findViewById(R.id.editTextNoteContent)

        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            // Dodaj notatkę do listy i odśwież RecyclerView
            addNoteToRecyclerView(title, content, LocalDateTime.now())


            lifecycleScope.launch {
                insertNote(userName, content, title)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun insertNote(userName: String, content: String, title: String) {
        return withContext(Dispatchers.IO) {
            val newNote = NoteEntity(
                userName = userName,
                content = content,
                creationTime = LocalDateTime.now(),
                title = title
            )
            noteDao.insert(newNote)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNoteToRecyclerView(title: String, content: String, now: LocalDateTime) {
        val note = Note(
            title,
            content,
            Date.from(now.toInstant(ZoneOffset.ofHours(1)))
        )
        noteList.add(note)
        noteAdapter.notifyDataSetChanged()
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

    private fun showNoteDetails(note: Note) {
        // Navigacja do NoteDetailsActivity
        // Tutaj możesz otworzyć nową aktywność, aby pokazać szczegóły notatki
        // Przykładowo:
        // val intent = Intent(this, NoteDetailsActivity::class.java)
        // intent.putExtra("NOTE_TITLE", note.title)
        // intent.putExtra("NOTE_CONTENT", note.content)
        // startActivity(intent)
    }

    // Dodana funkcja usuwająca notatkę
    private fun deleteNote(position: Int) {
        noteList.removeAt(position)
        noteAdapter.notifyDataSetChanged()
    }

    override fun onNoteClick(position: Int) {
        // Obsługa kliknięcia na notatkę
        val clickedNote = noteList[position]
        showNoteDetails(clickedNote)
    }

    override fun onNoteDelete(position: Int) {
        // Obsługa kliknięcia na przycisk usuwania
        deleteNote(position)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let { view ->
            val imm =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
