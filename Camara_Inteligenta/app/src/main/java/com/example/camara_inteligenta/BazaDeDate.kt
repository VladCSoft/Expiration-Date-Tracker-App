package com.example.camara_inteligenta

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Produs::class], version = 1)
abstract class BazaDeDate : RoomDatabase() {

    abstract fun primesteProdusDao(): ProdusDao

    companion object {

        @Volatile
        private var instanta: BazaDeDate? = null
        private var inchis = Any()

        operator fun invoke(context: Context) = instanta ?: synchronized(inchis) {
            instanta ?: construiesteBazaDeDate(context).also {
                instanta = it
            }
        }

        private fun construiesteBazaDeDate(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            BazaDeDate::class.java,
            "bazadedate"
        ).build()
    }
}