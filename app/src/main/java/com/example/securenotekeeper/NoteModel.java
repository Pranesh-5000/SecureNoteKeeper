package com.example.securenotekeeper;

public class NoteModel {
    private int id;
    private String title;
    private String content;
    private boolean isEncrypted;
    private String filePath;

    // Constructor with all parameters
    public NoteModel(int id, String title, String content, boolean isEncrypted, String filePath) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isEncrypted = isEncrypted;
        this.filePath = filePath;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public String getFilePath() {
        return filePath;
    }
}
