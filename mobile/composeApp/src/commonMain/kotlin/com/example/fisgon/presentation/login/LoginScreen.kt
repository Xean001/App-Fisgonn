package com.example.fisgon.presentation.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.fisgon.domain.entity.User
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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

private val BgColor = Color(0xFF090E1C)
private val Teal = Color(0xFF00C9A0)
private val InputBg = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)
private val TextMuted = Color(0xFF5A6A85)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (user: User, token: String) -> Unit = { _, _ -> },
    onNavigateToRegister: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loggedUser, state.generatedToken) {
        val user = state.loggedUser ?: return@LaunchedEffect
        val token = state.generatedToken ?: return@LaunchedEffect
        onLoginSuccess(user, token)
    }

    LaunchedEffect(state.navigateToRegister) {
        if (state.navigateToRegister) {
            viewModel.onRegisterNavigated()
            onNavigateToRegister()
        }
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
            Spacer(Modifier.height(52.dp))

            ShieldLogo()

            Spacer(Modifier.height(20.dp))

            Text(
                text = "FISGÓN",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp
            )

            Text(
                text = "SEGURIDAD ACTIVA",
                color = Teal,
                fontSize = 12.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // Campo usuario
            FieldLabel("USUARIO")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("nombre@correo.com", color = TextMuted, fontSize = 14.sp) },
                trailingIcon = {
                    PersonIcon(
                        modifier = Modifier.size(20.dp),
                        tint = TextMuted
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(20.dp))

            // Campo contraseña
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
                    visualTransformation = if (state.isPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(InputBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .clickable { viewModel.togglePasswordVisibility() },
                    contentAlignment = Alignment.Center
                ) {
                    EyeIcon(
                        modifier = Modifier.size(22.dp),
                        tint = Teal,
                        crossed = !state.isPasswordVisible
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                color = Teal,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                textAlign = TextAlign.End
            )

            Spacer(Modifier.height(32.dp))

            // Mensaje de error
            state.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFFF6B6B),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            }

            // Botón iniciar sesión
            Button(
                onClick = viewModel::onLogin,
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
                    Text(
                        text = "Iniciar Sesión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = viewModel::onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Teal,
                    disabledContentColor = Teal.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Teal.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Registrarse",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Divisor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(BorderColor)
                )
                Text(
                    "  o continúa con  ",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(BorderColor)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Botón Google
            OutlinedButton(
                onClick = viewModel::onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                GoogleLetterIcon()
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Iniciar con Google",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(40.dp))
        }

        // Logo UPN - esquina superior derecha
        val upnModifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(end = 16.dp, top = 8.dp)

        UpnLogo(
            modifier = upnModifier,
            size = 36.dp * 1.51f
        )

        // Flecha scroll
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF111827))
                .border(1.dp, BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.3f)
                    lineTo(w * 0.5f, h * 0.75f)
                    lineTo(w * 0.85f, h * 0.3f)
                }
                drawPath(
                    path, color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

// ─── Iconos dibujados con Canvas (sin dependencias externas) ─────────────────

@Composable
private fun ShieldLogo() {
    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(110.dp)) {
            val w = size.width
            val h = size.height
            val pad = 8.dp.toPx()

            val shield = Path().apply {
                moveTo(w / 2f, pad)
                lineTo(w - pad, h * 0.22f)
                lineTo(w - pad, h * 0.60f)
                cubicTo(w - pad, h * 0.80f, w * 0.75f, h * 0.93f, w / 2f, h - pad)
                cubicTo(w * 0.25f, h * 0.93f, pad, h * 0.80f, pad, h * 0.60f)
                lineTo(pad, h * 0.22f)
                close()
            }

            drawPath(
                shield,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x4400C9A0), Color.Transparent),
                    center = Offset(w / 2f, h / 2f),
                    radius = w * 0.72f
                )
            )
            drawPath(
                shield,
                color = Teal,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawCircle(
                color = Teal.copy(alpha = 0.12f),
                radius = w * 0.21f,
                center = Offset(w / 2f, h * 0.52f)
            )
            drawCircle(
                color = Teal,
                radius = w * 0.21f,
                center = Offset(w / 2f, h * 0.52f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Ojo dentro del círculo (dibujado directo en Canvas)
            val cx = w / 2f
            val cy = h * 0.52f
            val er = w * 0.13f
            val eyePath = Path().apply {
                moveTo(cx - er * 1.6f, cy)
                cubicTo(cx - er, cy - er * 0.9f, cx + er, cy - er * 0.9f, cx + er * 1.6f, cy)
                cubicTo(cx + er, cy + er * 0.9f, cx - er, cy + er * 0.9f, cx - er * 1.6f, cy)
                close()
            }
            drawPath(eyePath, color = Teal, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(color = Teal, radius = er * 0.45f, center = Offset(cx, cy))
        }
    }
}


@Composable
private fun EyeIcon(modifier: Modifier = Modifier, tint: Color, crossed: Boolean) {
    Canvas(modifier = modifier) {
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
            eyePath, color = if (crossed) tint.copy(alpha = 0.4f) else tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (!crossed) {
            drawCircle(tint, radius = er * 0.38f, center = Offset(cx, cy))
        } else {
            drawLine(
                color = tint,
                start = Offset(w * 0.2f, h * 0.12f),
                end = Offset(w * 0.8f, h * 0.88f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PersonIcon(modifier: Modifier = Modifier, tint: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.8.dp.toPx()
        val cx = w / 2f

        // Cabeza
        drawCircle(
            color = tint,
            radius = w * 0.22f,
            center = Offset(cx, h * 0.30f),
            style = Stroke(width = strokeW)
        )

        // Hombros (arco inferior de una elipse)
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.52f),
            size = Size(w * 0.84f, h * 0.56f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun GoogleLetterIcon() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            color = Color(0xFF4285F4),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            lineHeight = 13.sp
        )
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
