package com.example.securenotekeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database and Table Info
    private static final String DATABASE_NAME = "notes_db";
    private static final int DATABASE_VERSION = 3; // Updated version
    private static final String TABLE_NAME = "notes";

    // Column Names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_ENCRYPTED = "encrypted";
    private static final String COLUMN_FILE_PATH = "file_path";

    // Create Table Query
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_ENCRYPTED + " INTEGER DEFAULT 0, " +
                    COLUMN_FILE_PATH + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_ENCRYPTED + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_FILE_PATH + " TEXT");
        }
    }

    // Add a new note to the database
    public long addNote(String title, String content, boolean encrypted, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_ENCRYPTED, encrypted ? 1 : 0);
        values.put(COLUMN_FILE_PATH, filePath);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result;
    }

    // Get all notes from the database
    public ArrayList<NoteModel> getAllNotes() {
        ArrayList<NoteModel> notesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                boolean encrypted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED)) == 1;
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));

                // Create NoteModel with 5 parameters
                NoteModel note = new NoteModel(id, title, content, encrypted, filePath);
                notesList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notesList;
    }

    // Get a note by ID
    public NoteModel getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_ENCRYPTED, COLUMN_FILE_PATH},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        NoteModel note = null;
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
            boolean encrypted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENCRYPTED)) == 1;
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));

            // Create NoteModel with 5 parameters
            note = new NoteModel(id, title, content, encrypted, filePath);
            cursor.close();
        }
        db.close();
        return note;
    }

    // Delete a note by ID
    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
    // Update an existing note
    public boolean updateNote(int id, String title, String content, boolean encrypted, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_ENCRYPTED, encrypted ? 1 : 0);
        values.put(COLUMN_FILE_PATH, filePath);

        int rowsUpdated = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsUpdated > 0;
    }


}
