package com.example.fisgon.data.repository

import android.content.ContentValues
import com.example.fisgon.data.db.FisgonDatabaseHelper
import com.example.fisgon.domain.entity.Product
import com.example.fisgon.domain.repository.ProductRepository

class AndroidProductRepositoryImpl(private val db: FisgonDatabaseHelper) : ProductRepository {

    override suspend fun create(product: Product): Result<Product> {
        val values = ContentValues().apply {
            put("codigo", product.codigo)
            put("descripcion", product.descripcion)
            put("precio", product.precio)
            put("marca", product.marca)
        }
        val id = db.writableDatabase.insert("products", null, values)
        return if (id != -1L) Result.success(product.copy(id = id))
        else Result.failure(Exception("Error al registrar el producto"))
    }

    override suspend fun getAll(): List<Product> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM products ORDER BY id DESC", null
        )
        val list = mutableListOf<Product>()
        while (cursor.moveToNext()) {
            list += Product(
                id          = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                codigo      = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                precio      = cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                marca       = cursor.getString(cursor.getColumnIndexOrThrow("marca"))
            )
        }
        cursor.close()
        return list
    }

    override suspend fun search(query: String): List<Product> {
        val q = "%$query%"
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM products WHERE codigo LIKE ? OR descripcion LIKE ? OR marca LIKE ? ORDER BY id DESC",
            arrayOf(q, q, q)
        )
        val list = mutableListOf<Product>()
        while (cursor.moveToNext()) {
            list += Product(
                id          = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                codigo      = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                precio      = cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                marca       = cursor.getString(cursor.getColumnIndexOrThrow("marca"))
            )
        }
        cursor.close()
        return list
    }

    override suspend fun update(product: Product): Result<Unit> {
        val values = ContentValues().apply {
            put("codigo", product.codigo)
            put("descripcion", product.descripcion)
            put("precio", product.precio)
            put("marca", product.marca)
        }
        val rows = db.writableDatabase.update(
            "products", values, "id = ?", arrayOf(product.id.toString())
        )
        return if (rows > 0) Result.success(Unit)
        else Result.failure(Exception("Producto no encontrado"))
    }

    override suspend fun delete(id: Long): Result<Unit> {
        val rows = db.writableDatabase.delete(
            "products", "id = ?", arrayOf(id.toString())
        )
        return if (rows > 0) Result.success(Unit)
        else Result.failure(Exception("Producto no encontrado"))
    }
}
