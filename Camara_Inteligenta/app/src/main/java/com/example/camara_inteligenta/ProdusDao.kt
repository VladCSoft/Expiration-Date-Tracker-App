package com.example.camara_inteligenta

import androidx.room.*

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProdusDao {

    @Insert
    suspend fun adaugaProdus(produs: Produs): Long

    @Query("SELECT * FROM produs ORDER BY id DESC")
    suspend fun primesteToateProdusele(): List<Produs>

    @Query("SELECT * FROM produs WHERE id = :id")
    suspend fun primesteProdusDupaId(id: Long): Produs?

    @Update
    suspend fun actualizeazaProdus(produs: Produs)

    @Delete
    suspend fun stergeProdus(produs: Produs)
}