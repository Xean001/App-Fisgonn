package com.example.fisgon.backend.util

import kotlin.random.Random

/**
 * Genera el username anónimo público (doc §1 "Idea central", §11.3):
 * "adjetivo-animal-####". La unicidad la garantiza el índice UNIQUE de la BD;
 * quien llame a [next] debe reintentar si el INSERT choca (ver AuthRoutes).
 */
object AnonymousUsername {

    private val adjetivos = listOf(
        "veloz", "astuto", "sigiloso", "valiente", "agil", "sereno",
        "audaz", "noble", "fiero", "sabio", "raudo", "tenaz",
    )

    private val animales = listOf(
        "halcon", "zorro", "lobo", "aguila", "puma", "jaguar",
        "condor", "lince", "buho", "tigre", "garza", "ocelote",
    )

    /** Devuelve un candidato como `astuto-halcon-7421` (longitud 12..50). */
    fun next(): String {
        val adj = adjetivos.random()
        val animal = animales.random()
        val numero = Random.nextInt(0, 10_000).toString().padStart(4, '0')
        return "$adj-$animal-$numero"
    }
}
