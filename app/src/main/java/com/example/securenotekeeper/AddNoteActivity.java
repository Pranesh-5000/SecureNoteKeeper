package com.example.securenotekeeper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class AddNoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText, passwordEditText;
    private Button saveNoteButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Link views to IDs
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        databaseHelper = new DatabaseHelper(this);

        saveNoteButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and Content cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if a password was entered to mark note as encrypted
        boolean isEncrypted = !password.isEmpty();
        String filePath = ""; // Default for unencrypted notes

        // Encrypt content and save to a file if a password is provided
        if (isEncrypted) {
            try {
                content = EncryptionHelper.encrypt(content, password);
                filePath = saveEncryptedFile(title, content); // Save encrypted content to a file
            } catch (Exception e) {
                Toast.makeText(this, "Error encrypting note!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add note to the database with file path (empty for unencrypted)
        long result = databaseHelper.addNote(title, content, isEncrypted, filePath);

        if (result != -1) {
            Toast.makeText(this, "Note added successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Return to MainActivity after saving note
        } else {
            Toast.makeText(this, "Failed to add note!", Toast.LENGTH_SHORT).show();
        }
    }

    // Save encrypted content to a file and return the file path
    private String saveEncryptedFile(String title, String content) {
        try {
            File dir = new File(getFilesDir(), "encrypted_notes");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dir, title + ".enc");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(content);
            writer.close();
            fos.close();

            return file.getAbsolutePath(); // Return the file path
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving encrypted file!", Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}
