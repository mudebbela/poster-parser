package com.example.posterparser;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {EventEntity.class}, version = 1)
public abstract class PPDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
}
