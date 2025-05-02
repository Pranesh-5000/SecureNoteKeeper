package com.example.securenotekeeper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ViewNoteActivity extends AppCompatActivity {

    private TextView noteTitle, noteContent;
    private DatabaseHelper databaseHelper;
    private int noteId;
    private NoteModel note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        // Enable back button in ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("View Note");
        }

        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);
        databaseHelper = new DatabaseHelper(this);

        // Get noteId passed from NoteAdapter
        noteId = getIntent().getIntExtra("noteId", -1);

        if (noteId != -1) {
            note = databaseHelper.getNoteById(noteId);
            if (note != null) {
                noteTitle.setText(note.getTitle());
                if (note.isEncrypted()) {
                    // Request password to decrypt the note
                    requestPasswordToDecrypt(note.getContent());
                } else {
                    noteContent.setText(note.getContent());
                }
            } else {
                Toast.makeText(this, "Note not found!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Request password to decrypt note if encrypted
    private void requestPasswordToDecrypt(String encryptedContent) {
        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter Password");

        new AlertDialog.Builder(this)
                .setTitle("Password Required")
                .setMessage("This note is encrypted. Enter the password to view it.")
                .setView(passwordInput)
                .setPositiveButton("Decrypt", (dialog, which) -> {
                    String enteredPassword = passwordInput.getText().toString().trim();
                    try {
                        String decryptedContent = EncryptionHelper.decrypt(encryptedContent, enteredPassword);
                        noteContent.setText(decryptedContent); // Show content if password is correct
                    } catch (Exception e) {
                        Toast.makeText(this, "Incorrect password or decryption failed!", Toast.LENGTH_SHORT).show();
                        finish(); // Exit if wrong password
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // Handle back button press in ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
