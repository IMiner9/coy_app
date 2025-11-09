package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC, endDate ASC, time ASC")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query(
        "SELECT * FROM events " +
            "WHERE date <= :date AND COALESCE(NULLIF(endDate, ''), date) >= :date " +
            "ORDER BY date ASC, endDate ASC, time ASC"
    )
    fun getEventsByDate(date: String): Flow<List<Event>>
    
    @Query(
        "SELECT * FROM events " +
            "WHERE date <= :endDate AND COALESCE(NULLIF(endDate, ''), date) >= :startDate " +
            "ORDER BY date ASC, endDate ASC, time ASC"
    )
    fun getEventsByDateRange(startDate: String, endDate: String): Flow<List<Event>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
}








