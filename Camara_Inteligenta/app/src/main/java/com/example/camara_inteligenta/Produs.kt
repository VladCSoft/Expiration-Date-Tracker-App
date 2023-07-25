package com.example.camara_inteligenta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Produs (
    var NumeProdus:String ="",
    var DataExpirare:String =""
):java.io.Serializable{
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}