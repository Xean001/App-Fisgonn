package com.example.fisgon.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.fisgon.domain.entity.User

private val BgColor = Color(0xFF090E1C)
private val Teal = Color(0xFF00C9A0)
private val InputBg = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)
private val TextMuted = Color(0xFF5A6A85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (user: User, token: String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registeredUser, state.generatedToken) {
        val user = state.registeredUser ?: return@LaunchedEffect
        val token = state.generatedToken ?: return@LaunchedEffect
        onRegisterSuccess(user, token)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeContentPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Encabezado con flecha atrás y título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(InputBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(w * 0.65f, h * 0.15f)
                            lineTo(w * 0.25f, h * 0.5f)
                            lineTo(w * 0.65f, h * 0.85f)
                        }
                        drawPath(
                            path, color = Color.White.copy(alpha = 0.8f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "CREAR CUENTA",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "SEGURIDAD ACTIVA",
                color = Teal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().padding(start = 56.dp)
            )

            Spacer(Modifier.height(36.dp))

            // NOMBRE
            FieldLabel("NOMBRE")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Juan", color = TextMuted, fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            // APELLIDO
            FieldLabel("APELLIDO")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.apellido,
                onValueChange = viewModel::onApellidoChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pérez (opcional)", color = TextMuted, fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            // USUARIO
            FieldLabel("USUARIO")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("nombre@correo.com", color = TextMuted, fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            // CONTRASEÑA
            FieldLabel("CONTRASEÑA")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mínimo 6 caracteres", color = TextMuted, fontSize = 13.sp) },
                    visualTransformation = if (state.isPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                Spacer(Modifier.width(10.dp))
                EyeToggleButton(visible = state.isPasswordVisible, onClick = viewModel::togglePasswordVisibility)
            }

            Spacer(Modifier.height(16.dp))

            // CONFIRMAR CONTRASEÑA
            FieldLabel("CONFIRMAR CONTRASEÑA")
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Repite la contraseña", color = TextMuted, fontSize = 13.sp) },
                    visualTransformation = if (state.isConfirmPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                Spacer(Modifier.width(10.dp))
                EyeToggleButton(visible = state.isConfirmPasswordVisible, onClick = viewModel::toggleConfirmPasswordVisibility)
            }

            Spacer(Modifier.height(28.dp))

            // Error
            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            }

            // Botón Crear Cuenta
            Button(
                onClick = viewModel::onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C1426),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF0C1426).copy(alpha = 0.6f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Teal,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ya tengo cuenta
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                color = Teal,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() }
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EyeToggleButton(visible: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(InputBg)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val er = w * 0.38f
            val strokeW = 1.8.dp.toPx()
            val eyePath = Path().apply {
                moveTo(cx - er * 1.55f, cy)
                cubicTo(cx - er, cy - er * 0.85f, cx + er, cy - er * 0.85f, cx + er * 1.55f, cy)
                cubicTo(cx + er, cy + er * 0.85f, cx - er, cy + er * 0.85f, cx - er * 1.55f, cy)
                close()
            }
            drawPath(
                eyePath,
                color = if (visible) Teal else Teal.copy(alpha = 0.4f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            if (visible) {
                drawCircle(Teal, radius = er * 0.38f, center = androidx.compose.ui.geometry.Offset(cx, cy))
            } else {
                drawLine(
                    color = Teal,
                    start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.12f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.88f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Teal,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.5.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Teal,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = InputBg,
    unfocusedContainerColor = InputBg,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Teal,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)
