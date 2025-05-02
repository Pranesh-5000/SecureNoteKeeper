package com.example.securenotekeeper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private ArrayList<NoteModel> notesList;
    private DatabaseHelper databaseHelper;
    private OnDeleteRequestListener deleteRequestListener;
    private OnEditRequestListener editRequestListener;

    // Interface to handle delete request
    public interface OnDeleteRequestListener {
        void onDeleteRequest(NoteModel note);
    }

    // Interface to handle edit request
    public interface OnEditRequestListener {
        void onEditRequest(NoteModel note);
    }

    public NoteAdapter(Context context, ArrayList<NoteModel> notesList, DatabaseHelper databaseHelper,
                       OnDeleteRequestListener deleteRequestListener, OnEditRequestListener editRequestListener) {
        this.context = context;
        this.notesList = notesList;
        this.databaseHelper = databaseHelper;
        this.deleteRequestListener = deleteRequestListener;
        this.editRequestListener = editRequestListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = notesList.get(position);
        holder.noteTitle.setText(note.getTitle());

        // Open note when clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            context.startActivity(intent);
        });

        // Delete note when delete button is clicked
        holder.deleteNoteButton.setOnClickListener(v -> {
            deleteRequestListener.onDeleteRequest(note);
        });

        // Edit note when edit button is clicked
        holder.editNoteButton.setOnClickListener(v -> {
            if (note.isEncrypted()) {
                showPasswordDialogForEdit(note); // Ask for password before editing
            } else {
                openEditActivity(note, note.getContent(), ""); // Pass empty password for unencrypted notes
            }
        });

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        ImageButton deleteNoteButton, editNoteButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            deleteNoteButton = itemView.findViewById(R.id.deleteNoteButton);
            editNoteButton = itemView.findViewById(R.id.editNoteButton);
        }
    }

    // Show password dialog before editing encrypted notes
    private void showPasswordDialogForEdit(NoteModel note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Password to Edit");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString().trim();
            try {
                String decryptedContent = EncryptionHelper.decrypt(note.getContent(), password);
                if (decryptedContent != null) {
                    openEditActivity(note, decryptedContent, note.isEncrypted() ? password : "");

                } else {
                    Toast.makeText(context, "Incorrect password!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error decrypting note!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Open EditNoteActivity with decrypted content for encrypted notes
    private void openEditActivity(NoteModel note, String decryptedContent, String originalPassword) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", decryptedContent);
        intent.putExtra("isEncrypted", true);
        intent.putExtra("originalPassword", originalPassword); // Passing original password
        context.startActivity(intent);
    }

}
