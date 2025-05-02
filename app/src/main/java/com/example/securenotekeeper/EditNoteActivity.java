package com.example.securenotekeeper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditNoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, passwordEditText;
    private Button updateNoteButton;
    private DatabaseHelper databaseHelper;
    private int noteId;
    private boolean isEncrypted;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        updateNoteButton = findViewById(R.id.updateNoteButton);
        databaseHelper = new DatabaseHelper(this);

        // Get note details from intent
        noteId = getIntent().getIntExtra("noteId", -1);
        String noteTitle = getIntent().getStringExtra("title");
        String noteContent = getIntent().getStringExtra("content");
        isEncrypted = getIntent().getBooleanExtra("isEncrypted", false);
        filePath = getIntent().getStringExtra("filePath");

        // Load data into views
        titleEditText.setText(noteTitle);
        contentEditText.setText(noteContent);

        updateNoteButton.setOnClickListener(v -> updateNote());
    }

    private void updateNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and Content cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean shouldEncrypt = !newPassword.isEmpty();
        boolean wasEncrypted = isEncrypted; // Was the note encrypted before?

        // Check if note was previously encrypted
        if (wasEncrypted) {
            if (newPassword.isEmpty()) {
                // Retain the original password if no new password is provided
                String originalPassword = getIntent().getStringExtra("originalPassword");
                try {
                    content = EncryptionHelper.encrypt(content, originalPassword);
                    isEncrypted = true; // Keep encryption enabled
                } catch (Exception e) {
                    Toast.makeText(this, "Error retaining original encryption!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Encrypt with new password if provided
                try {
                    content = EncryptionHelper.encrypt(content, newPassword);
                    isEncrypted = true;
                } catch (Exception e) {
                    Toast.makeText(this, "Error encrypting with new password!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else if (shouldEncrypt) {
            // If the note was not previously encrypted but a new password is provided, encrypt it
            try {
                content = EncryptionHelper.encrypt(content, newPassword);
                isEncrypted = true;
            } catch (Exception e) {
                Toast.makeText(this, "Error encrypting new note!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Save as plain text if no encryption is required
            isEncrypted = false;
        }

        boolean result = databaseHelper.updateNote(noteId, title, content, isEncrypted, filePath);

        if (result) {
            Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update note!", Toast.LENGTH_SHORT).show();
        }
    }

}
