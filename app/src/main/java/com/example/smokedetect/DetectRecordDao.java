package com.example.smokedetect;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectRecordDao {
    @Query("SELECT * FROM detectrecord")
    List<DetectRecord> getAll();

    @Insert
    void insert(DetectRecord detectRecord);

    @Delete
    void delete(DetectRecord detectRecord);
}
