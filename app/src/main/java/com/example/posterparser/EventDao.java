package com.example.posterparser;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {

        @Query("SELECT * FROM EventEntity")
        List<EventEntity> getAll();

        @Insert
    void insertAll(EventEntity... eventEntities);

        @Delete
    void deleteAll(EventEntity ... eventEntities);

}
