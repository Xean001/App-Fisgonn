package com.example.fisgon.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FisgonDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS users (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre   TEXT NOT NULL,
                apellido TEXT NOT NULL,
                email    TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS products (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo      TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                precio      REAL NOT NULL,
                marca       TEXT NOT NULL
            )"""
        )
        // Usuario pre-cargado para pruebas
        db.execSQL(
            """INSERT OR IGNORE INTO users (nombre, apellido, email, password)
               VALUES ('Admin', 'Fisgon', 'admin@fisgon.com', 'admin123')"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "fisgon.db"
        const val DB_VERSION = 1
    }
}
