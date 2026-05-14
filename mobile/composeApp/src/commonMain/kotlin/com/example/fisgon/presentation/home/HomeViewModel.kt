package com.example.fisgon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fisgon.domain.entity.Product
import com.example.fisgon.domain.entity.User
import com.example.fisgon.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val productRepository: ProductRepository,
    val currentUser: User
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(products = productRepository.getAll()) }
        }
    }

    fun onCodigoChange(v: String) = _uiState.update { it.copy(formCodigo = v) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(formDescripcion = v) }
    fun onPrecioChange(v: String) = _uiState.update { it.copy(formPrecio = v) }
    fun onMarcaChange(v: String) = _uiState.update { it.copy(formMarca = v) }
    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }

    fun onProductSelected(product: Product) = _uiState.update {
        it.copy(
            selectedProduct  = product,
            formCodigo       = product.codigo,
            formDescripcion  = product.descripcion,
            formPrecio       = product.precio.toString(),
            formMarca        = product.marca
        )
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun onRegistrar() {
        val s = _uiState.value
        val precio = s.formPrecio.toDoubleOrNull()
        if (s.formCodigo.isBlank() || s.formDescripcion.isBlank() || precio == null || s.formMarca.isBlank()) {
            _uiState.update { it.copy(message = "Completa todos los campos correctamente", messageIsError = true) }
            return
        }
        viewModelScope.launch {
            productRepository.create(Product(0, s.formCodigo, s.formDescripcion, precio, s.formMarca))
                .onSuccess { clearForm(); loadProducts(); _uiState.update { it.copy(message = "Producto registrado correctamente", messageIsError = false) } }
                .onFailure { e -> _uiState.update { it.copy(message = e.message, messageIsError = true) } }
        }
    }

    fun onBuscar() {
        val query = _uiState.value.searchQuery
        viewModelScope.launch {
            val results = if (query.isBlank()) productRepository.getAll()
                          else productRepository.search(query)
            _uiState.update { it.copy(products = results) }
        }
    }

    fun onModificar() {
        val s = _uiState.value
        val selected = s.selectedProduct ?: run {
            _uiState.update { it.copy(message = "Selecciona un producto de la lista primero", messageIsError = true) }
            return
        }
        val precio = s.formPrecio.toDoubleOrNull()
        if (s.formCodigo.isBlank() || precio == null) {
            _uiState.update { it.copy(message = "Completa todos los campos", messageIsError = true) }
            return
        }
        viewModelScope.launch {
            productRepository.update(Product(selected.id, s.formCodigo, s.formDescripcion, precio, s.formMarca))
                .onSuccess { clearForm(); loadProducts(); _uiState.update { it.copy(message = "Producto modificado correctamente", messageIsError = false) } }
                .onFailure { e -> _uiState.update { it.copy(message = e.message, messageIsError = true) } }
        }
    }

    fun onEliminar() {
        val selected = _uiState.value.selectedProduct ?: run {
            _uiState.update { it.copy(message = "Selecciona un producto de la lista primero", messageIsError = true) }
            return
        }
        viewModelScope.launch {
            productRepository.delete(selected.id)
                .onSuccess { clearForm(); loadProducts(); _uiState.update { it.copy(message = "Producto eliminado correctamente", messageIsError = false) } }
                .onFailure { e -> _uiState.update { it.copy(message = e.message, messageIsError = true) } }
        }
    }

    private fun clearForm() = _uiState.update {
        it.copy(formCodigo = "", formDescripcion = "", formPrecio = "", formMarca = "", selectedProduct = null)
    }
}
