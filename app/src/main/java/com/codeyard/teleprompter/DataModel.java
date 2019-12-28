package com.codeyard.teleprompter;


import androidx.annotation.NonNull;

class DataModel implements Comparable<DataModel> {
    private final String name;
    private final String date;
    static final int LOCATION_INTERNAL = 2;
    static final int LOCATION_DROPBOX = 1;
    private final int location;

    DataModel(String name, String date, int location) {
        this.name = name;
        this.date = date;
        this.location = location;
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

    int getLocation() {
        return location;
    }
//    public void setDate(String date) {
//        this.date = date;
//    }
}