package com.example.runalyzerapp;

public class FilePathHolder {
    private static FilePathHolder instance;
    private String filePath;

    private FilePathHolder() { }

    public static FilePathHolder getInstance() {
        if (instance == null) {
            instance = new FilePathHolder();
        }
        return instance;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
