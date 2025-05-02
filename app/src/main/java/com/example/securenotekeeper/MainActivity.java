package com.example.securenotekeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<NoteModel> notesList;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton addNoteButton;

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // Reload notes when returning to MainActivity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addNoteButton = findViewById(R.id.addNoteButton);
        databaseHelper = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadNotes(); // Initial load of notes

        addNoteButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
        });
    }

    private void loadNotes() {
        notesList = databaseHelper.getAllNotes();
        if (notesList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noteAdapter = new NoteAdapter(this, notesList, databaseHelper, this::handleDeleteRequest, this::onEditRequest);
            recyclerView.setAdapter(noteAdapter);
        }
    }

    // Handle delete request for a note
    private void handleDeleteRequest(NoteModel note) {
        if (note.isEncrypted()) {
            showPasswordPrompt(note, true); // true for delete request
        } else {
            databaseHelper.deleteNote(note.getId());
            loadNotes(); // Reload the list after deletion
        }
    }

    // Handle edit request for a note
    private void onEditRequest(NoteModel note) {
        if (note.isEncrypted()) {
            showPasswordPrompt(note, false); // false for edit request
        } else {
            openEditActivity(note);
        }
    }

    // Show password prompt for encrypted note
    private void showPasswordPrompt(NoteModel note, boolean isDeleteRequest) {
        EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");
        builder.setView(passwordInput);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String enteredPassword = passwordInput.getText().toString().trim();
            try {
                String decryptedContent = EncryptionHelper.decrypt(note.getContent(), enteredPassword);
                if (decryptedContent != null) {
                    if (isDeleteRequest) {
                        databaseHelper.deleteNote(note.getId());
                        loadNotes();
                    } else {
                        openEditActivity(note, decryptedContent, note.isEncrypted() ? "originalPassword" : "");


                    }
                } else {
                    Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error decrypting note.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Open Edit Activity after verifying password
    private void openEditActivity(NoteModel note, String decryptedContent, String originalPassword) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", decryptedContent);
        intent.putExtra("isEncrypted", true);
        intent.putExtra("originalPassword", originalPassword); // Passing original password
        startActivity(intent);
    }


    // Open Edit Activity for non-encrypted notes
    private void openEditActivity(NoteModel note) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());
        intent.putExtra("isEncrypted", false);
        startActivity(intent);
    }
}
