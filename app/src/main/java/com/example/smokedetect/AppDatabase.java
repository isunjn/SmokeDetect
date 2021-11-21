package com.example.smokedetect;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DetectRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DetectRecordDao detectRecordDao();
}
