package com.example.smokedetect;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DetectRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String path; // 图片路径（剪切后的图片）

    public int result;  // 林格曼黑度结果

    DetectRecord(String path, int result) {
        this.path = path;
        this.result = result;
    }
}
