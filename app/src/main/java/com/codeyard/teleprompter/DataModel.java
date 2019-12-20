package com.codeyard.teleprompter;


import android.support.annotation.NonNull;

class DataModel implements Comparable<DataModel> {
    private final String name;
    private final String date;

    DataModel(String name, String date) {
        this.name = name;
        this.date = date;
    }
//    public void setMetadata(FileMetadata metadata){
//        this.fileMetadata=metadata;
//    }
//    public FileMetadata getFileMetadata(){
//        return this.fileMetadata;
//    }

    @Override
    public int compareTo(@NonNull DataModel u) {
        if (getDate() == null || u.getDate() == null) {
            return 0;
        }
        return getDate().compareTo(u.getDate());
    }

    public String getName() {
        return name;
    }

    String getDate() {
        return date;
    }

//    public void setDate(String date) {
//        this.date = date;
//    }
}