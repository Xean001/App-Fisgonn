package com.example.fisgon.domain.usecase

import com.example.fisgon.domain.repository.PanicRepository

class SendPanicUseCase(private val panicRepository: PanicRepository) {
    suspend operator fun invoke(latitude: Double, longitude: Double) =
        panicRepository.sendPanic(latitude, longitude)
}
