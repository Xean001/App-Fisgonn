package com.example.fisgon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fisgon.domain.entity.Product
import com.example.fisgon.presentation.map.MapLibreMap
import kotlin.math.roundToInt

private val BgColor    = Color(0xFF090E1C)
private val Teal       = Color(0xFF00C9A0)
private val InputBg    = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)
private val CardBg     = Color(0xFF0F1929)
private val TextMuted  = Color(0xFF5A6A85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenUrl: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .safeContentPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Header ──────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("FISGÓN", color = Teal, fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                Text("SEGURIDAD ACTIVA", color = TextMuted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Spacer(Modifier.height(16.dp))

        // ── Bienvenida ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A1520), RoundedCornerShape(14.dp))
                .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text("¡Bienvenido!", color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${viewModel.currentUser.nombre} ${viewModel.currentUser.apellido}",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Text(viewModel.currentUser.email, color = TextMuted, fontSize = 12.sp)
            Text("Acceso exitoso al sistema", color = Color(0xFF4CAF50), fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        // ── Mapa ───────────────────────────────────────────────
        Text("MAPA", color = Teal, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF0A1520), RoundedCornerShape(14.dp))
                .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            MapLibreMap(modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(24.dp))

        // ── CRUD de Productos ────────────────────────────────────
        Text("GESTIÓN DE PRODUCTOS", color = Teal, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))

        // Formulario
        CrudField("Código del producto", state.formCodigo, viewModel::onCodigoChange)
        Spacer(Modifier.height(10.dp))
        CrudField("Descripción del producto", state.formDescripcion, viewModel::onDescripcionChange)
        Spacer(Modifier.height(10.dp))
        CrudField("Precio del producto", state.formPrecio, viewModel::onPrecioChange,
            keyboard = KeyboardType.Decimal)
        Spacer(Modifier.height(10.dp))
        CrudField("Marca del producto", state.formMarca, viewModel::onMarcaChange)

        Spacer(Modifier.height(20.dp))

        // Mensaje de operación
        state.message?.let { msg ->
            Text(
                text = msg,
                color = if (state.messageIsError) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearMessage()
            }
        }

        // Botones CRUD
        CrudButton("Registrar Producto", Teal) { viewModel.onRegistrar() }
        Spacer(Modifier.height(8.dp))

        // Buscar con campo de búsqueda
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.weight(1f).height(52.dp),
                placeholder = { Text("Buscar...", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal, unfocusedBorderColor = BorderColor,
                    focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Teal
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = viewModel::onBuscar,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InputBg, contentColor = Color.White)
            ) { Text("Buscar", fontSize = 13.sp) }
        }

        Spacer(Modifier.height(8.dp))
        CrudButton("Modificar Producto", Color(0xFF2196F3)) { viewModel.onModificar() }
        Spacer(Modifier.height(8.dp))
        CrudButton("Eliminar Producto", Color(0xFFFF5252)) { viewModel.onEliminar() }

        Spacer(Modifier.height(24.dp))

        // ── Lista de productos ───────────────────────────────────
        if (state.products.isNotEmpty()) {
            Text("LISTA DE PRODUCTOS (${state.products.size})", color = Teal,
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            state.products.forEach { product ->
                ProductItem(
                    product = product,
                    isSelected = state.selectedProduct?.id == product.id,
                    onClick = { viewModel.onProductSelected(product) }
                )
                Spacer(Modifier.height(6.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin productos registrados", color = TextMuted, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Redes Sociales ───────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Spacer(Modifier.height(20.dp))

        Text("REDES SOCIALES", color = Teal, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "f", label = "Facebook",
                color = Color(0xFF1877F2),
                onClick = { onOpenUrl("https://www.facebook.com") }
            )
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "in", label = "Instagram",
                color = Color(0xFFE4405F),
                onClick = { onOpenUrl("https://www.instagram.com") }
            )
            SocialButton(
                modifier = Modifier.weight(1f),
                letter = "▶", label = "YouTube",
                color = Color(0xFFFF0000),
                onClick = { onOpenUrl("https://www.youtube.com") }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Componentes privados ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrudField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label, color = TextMuted, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Teal, unfocusedBorderColor = BorderColor,
            focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            cursorColor = Teal, focusedPlaceholderColor = TextMuted, unfocusedPlaceholderColor = TextMuted
        )
    )
}

@Composable
private fun CrudButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun ProductItem(product: Product, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Teal.copy(alpha = 0.1f) else CardBg,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (isSelected) Teal.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.codigo, color = Teal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(product.descripcion, color = Color.White, fontSize = 13.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.marca, color = TextMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("S/ ${formatPrice(product.precio)}", color = Color.White,
            fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatPrice(value: Double): String {
    val cents = (value * 100).roundToInt()
    val soles = cents / 100
    val decimals = (cents % 100).toString().padStart(2, '0')
    return "$soles.$decimals"
}

@Composable
private fun SocialButton(
    modifier: Modifier = Modifier,
    letter: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letter, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                lineHeight = 16.sp)
            Text(label, color = Color.White, fontSize = 9.sp, lineHeight = 10.sp)
        }
    }
}
