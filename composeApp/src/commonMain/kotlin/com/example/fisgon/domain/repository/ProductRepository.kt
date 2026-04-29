package com.example.fisgon.domain.repository

import com.example.fisgon.domain.entity.Product

interface ProductRepository {
    suspend fun create(product: Product): Result<Product>
    suspend fun getAll(): List<Product>
    suspend fun search(query: String): List<Product>
    suspend fun update(product: Product): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
}
