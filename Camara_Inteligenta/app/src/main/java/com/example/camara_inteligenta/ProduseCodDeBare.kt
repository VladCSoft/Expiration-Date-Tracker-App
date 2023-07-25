package com.example.camara_inteligenta

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ProduseCodDeBare(context: Context) :
    SQLiteOpenHelper(context, BAZA_DE_DATE_NUME, null, BAZA_DE_DATE_VERSIUNE) {

    companion object {
        private const val BAZA_DE_DATE_NUME = "products.db"
        private const val BAZA_DE_DATE_VERSIUNE = 1

        private const val TABEL_NUME = "produse"
        private const val COLOANA_COD_DE_BARE = "codbare"
        private const val COLOANA_DATA_DE_EXPIRARE = "produs"
    }

    override fun onCreate(bazaDeDate: SQLiteDatabase) {
        Log.d("MyDatabaseHelper", "onCreate called")
        val creareTabel =
            "CREATE TABLE $TABEL_NUME ($COLOANA_COD_DE_BARE TEXT PRIMARY KEY, $COLOANA_DATA_DE_EXPIRARE TEXT)"
        bazaDeDate.execSQL(creareTabel)

        bazaDeDate.execSQL("INSERT INTO $TABEL_NUME VALUES ('20520144', '30-06-2023')")
        bazaDeDate.execSQL("INSERT INTO $TABEL_NUME VALUES ('20589509', '02-07-2023')")
    }

    override fun onUpgrade(bazaDeDate: SQLiteDatabase, versiuneaVeche: Int, versiuneaNoua: Int) {
        bazaDeDate.execSQL("DROP TABLE IF EXISTS $TABEL_NUME")
        onCreate(bazaDeDate)
    }

    fun primesteProdus(codbare: String): String? {
        Log.d("MyDatabaseHelper", "primesteProdus chemat cu codul de bare $codbare")
        val bazaDeDate = readableDatabase
        val interogare = "SELECT $COLOANA_COD_DE_BARE, $COLOANA_DATA_DE_EXPIRARE FROM $TABEL_NUME WHERE $COLOANA_COD_DE_BARE = ?"
        val cursor = bazaDeDate.rawQuery(interogare, arrayOf(codbare))
        var produs: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLOANA_DATA_DE_EXPIRARE)
            if (columnIndex >= 0) {
                produs = cursor.getString(columnIndex)
            }
        }
        cursor?.close()
        bazaDeDate.close()
        return produs
    }
}