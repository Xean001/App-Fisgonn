package com.example.fisgon.data.repository

import com.example.fisgon.domain.entity.Product
import com.example.fisgon.domain.repository.ProductRepository

class ProductRepositoryImpl : ProductRepository {
    private val products = mutableListOf<Product>()
    private var nextId = 1L

    override suspend fun create(product: Product): Result<Product> {
        val new = product.copy(id = nextId++)
        products.add(new)
        return Result.success(new)
    }

    override suspend fun getAll(): List<Product> = products.toList().reversed()

    override suspend fun search(query: String): List<Product> =
        products.filter {
            it.codigo.contains(query, ignoreCase = true) ||
            it.descripcion.contains(query, ignoreCase = true) ||
            it.marca.contains(query, ignoreCase = true)
        }

    override suspend fun update(product: Product): Result<Unit> {
        val i = products.indexOfFirst { it.id == product.id }
        return if (i >= 0) { products[i] = product; Result.success(Unit) }
        else Result.failure(Exception("Producto no encontrado"))
    }

    override suspend fun delete(id: Long): Result<Unit> {
        val removed = products.removeAll { it.id == id }
        return if (removed) Result.success(Unit)
        else Result.failure(Exception("Producto no encontrado"))
    }
}
