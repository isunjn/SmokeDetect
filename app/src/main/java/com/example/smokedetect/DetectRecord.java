package com.example.smokedetect;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DetectRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String path;

    public int result;

    DetectRecord(String path, int result) {
        this.path = path;
        this.result = result;
    }
}
