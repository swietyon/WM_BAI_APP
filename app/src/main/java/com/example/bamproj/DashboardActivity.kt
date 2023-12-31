package com.example.bamproj

import NoteAdapter
import android.content.ContentValues.TAG
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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class DashboardActivity : AppCompatActivity(), OnNoteClickListener {

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteList: MutableList<Note>
    private lateinit var userName: String
    private lateinit var noteDao: NoteDao
    private lateinit var keyStore: KeyStore
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var addNoteButton: Button
    private lateinit var editedNote: Note
    private lateinit var mainCipher: Cipher
    private var editedNoteId: Long? = null;
    private var editedNotePosition: Int? = null;


    @RequiresApi(Build.VERSION_CODES.M)
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

        noteAdapter = NoteAdapter(noteList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteAdapter

        val textViewWelcome: TextView = findViewById(R.id.textViewWelcome)
        textViewWelcome.text = "Witaj, $userName!"

        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            performLogout()
        }

        titleEditText = findViewById(R.id.editTextNoteTitle)
        contentEditText = findViewById(R.id.editTextNoteContent)
        addNoteButton = findViewById(R.id.addNoteButton)

        addNoteButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addOrEditNote(noteDao, userName)
            }
        }

        // Automatyczne odświeżanie notatek
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadListByUserName(noteList, noteDao, userName)
            }
            noteAdapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadListByUserName(noteList: MutableList<Note>, noteDao: NoteDao, userName: String) {
        return withContext(Dispatchers.IO) {
            val result = noteDao.getNoteByUsername(userName)
            for (singleElem in result) {
                Log.d(TAG, "DATA: Title: " + singleElem.title +  " Content :" + singleElem.content);
                noteList.add(
                    Note(
                        uid = singleElem.uid!!,
                        title = singleElem.title,
                        content = decryptString(singleElem.content),
                        date = Date.from(singleElem.creationTime.toInstant(ZoneOffset.ofHours(1)))
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addOrEditNote(noteDao: NoteDao, userName: String) {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            lifecycleScope.launch {
                if (editedNoteId == null) {
                    processInsertingNewNote(title, content, noteDao, userName);
                    showToast("Notatka została dodana!")
                } else {
                    processEditingNote(title, content, noteDao, userName);
                    showToast("Notatka została zedytowana!")
                }
                noteAdapter.notifyDataSetChanged()
            }
        } else {
            showToast("Wprowadź tytuł i treść notatki.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun processEditingNote(title: String, content: String, noteDao: NoteDao, userName: String) {
        lifecycleScope.launch {
            var note: NoteEntity = getNote(editedNoteId!!)

            note.title = title
            note.content = encryptString(content)
            updateNote(note);

            editNoteInRecyclerView(editedNoteId!!, title, content, LocalDateTime.now())
            noteAdapter.notifyDataSetChanged()

            titleEditText.setText("")
            contentEditText.setText("")
            addNoteButton.setText("Dodaj notatkę")
            editedNoteId = null
            editedNotePosition = null
        }
    }

    private suspend fun getNote(editedNoteId: Long): NoteEntity {
        return withContext(Dispatchers.IO) {
            return@withContext noteDao.getByUid(editedNoteId);
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun processInsertingNewNote(title: String, content: String, noteDao: NoteDao, userName: String) {
        lifecycleScope.launch {
            val createdUid = insertNote(userName, content, title)
            addNoteToRecyclerView(createdUid, title, content, LocalDateTime.now())
            noteAdapter.notifyDataSetChanged()

            titleEditText.setText("")
            contentEditText.setText("")
        }
    }

    // Funkcja pomocnicza do wyświetlania Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun insertNote(userName: String, content: String, title: String): Long {
        return withContext(Dispatchers.IO) {
            val newNote = NoteEntity(
                userName = userName,
                content = encryptString(content),
                creationTime = LocalDateTime.now(),
                title = title
            )
            return@withContext noteDao.insert(newNote)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateNote(note: NoteEntity) {
        return withContext(Dispatchers.IO) {
            noteDao.update(note)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNoteToRecyclerView(createdUid: Long, title: String, content: String, now: LocalDateTime) {
        val note = Note(
            createdUid,
            title,
            content,
            Date.from(now.toInstant(ZoneOffset.ofHours(1)))
        )
        noteList.add(note)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editNoteInRecyclerView(createdUid: Long, title: String, content: String, now: LocalDateTime) {
        val note = Note(
            createdUid,
            title,
            content,
            Date.from(now.toInstant(ZoneOffset.ofHours(1)))
        )
        noteList.set(editedNotePosition!!, note);
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("MySharedPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("LOGIN")
        editor.remove("PASSWORD")
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun editNote(note: Note, position: Int) {
        addNoteButton.text = "Edytuj notatkę"
        titleEditText.setText(note.title)
        contentEditText.setText(note.content)
        editedNoteId = note.uid;
        editedNotePosition = position;
    }

    private fun deleteNote(position: Int) {
        val deletedNote = noteList[position]

        lifecycleScope.launch {
            deleteNoteFromDatabase(deletedNote)
        }

        noteList.removeAt(position)
        noteAdapter.notifyDataSetChanged()

        showToast("Notatka została usunięta!")
    }

    private suspend fun deleteNoteFromDatabase(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteByTitleAndContent(note.title, note.content)
        }
    }

    override fun onNoteClick(position: Int) {
        // Obsługa kliknięcia na notatkę
        val clickedNote = noteList[position]
        editNote(clickedNote, position)
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

    private fun initializeCipher() {
    }

    private fun stringToByteArray(text: String): ByteArray {
        return text.let { Base64.decode(it, Base64.DEFAULT) }
    }

    private fun byteArrayToString(byteArray: ByteArray): String {
        return byteArray.let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val KEY_ALIAS = "KEYALIAS"
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGeneratorSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
            keyGenerator.init(keyGeneratorSpec)
            keyGenerator.generateKey()
        } else {
            Log.d(TAG, "Posiada");
        }

        val secreteKeyEntry: KeyStore.SecretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return secreteKeyEntry.secretKey
    }

    private fun encryptString(data: String): String {
        mainCipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        mainCipher.init(Cipher.ENCRYPT_MODE, getKey())
        val ivString = byteArrayToString(mainCipher.iv)
        return ivString + ":::" + byteArrayToString(mainCipher.doFinal(data.toByteArray(Charsets.UTF_8)));
    }

    private fun decryptString(encData: String): String {
        val ivStringWithEncryptedData = encData.split(":::")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec = IvParameterSpec(stringToByteArray(ivStringWithEncryptedData.get(0)))
        cipher.init(Cipher.DECRYPT_MODE, getKey(), keySpec)
        return cipher.doFinal(stringToByteArray(ivStringWithEncryptedData.get(1))).toString(UTF_8)

    }
}
