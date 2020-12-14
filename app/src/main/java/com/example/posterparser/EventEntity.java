package com.example.posterparser;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class EventEntity {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "url")
    public String imageUrl;

    @ColumnInfo(name = "timestamp")
    public long timestamp;
}
