package com.example.fisgon.presentation.token

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fisgon.domain.entity.User

private val BgColor = Color(0xFF090E1C)
private val Teal = Color(0xFF00C9A0)
private val InputBg = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenScreen(
    user: User,
    generatedToken: String,
    onVerified: () -> Unit = {}
) {
    val viewModel = remember { TokenViewModel(user, generatedToken) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) onVerified()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeContentPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Mini escudo
            Canvas(modifier = Modifier.size(72.dp)) {
                val w = size.width; val h = size.height; val p = 4.dp.toPx()
                val shield = Path().apply {
                    moveTo(w / 2f, p)
                    lineTo(w - p, h * 0.22f); lineTo(w - p, h * 0.60f)
                    cubicTo(w - p, h * 0.80f, w * 0.75f, h * 0.93f, w / 2f, h - p)
                    cubicTo(w * 0.25f, h * 0.93f, p, h * 0.80f, p, h * 0.60f)
                    lineTo(p, h * 0.22f); close()
                }
                drawPath(shield, color = Teal,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }

            Spacer(Modifier.height(16.dp))

            Text("Verificación de Seguridad", color = Color.White,
                fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Text("Segundo Factor de Autenticación", color = Teal,
                fontSize = 12.sp, letterSpacing = 1.sp)

            Spacer(Modifier.height(32.dp))

            // Código generado (demo)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Código de verificación (demo)", color = Color(0xFF5A6A85),
                    fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text(generatedToken, color = Teal,
                    fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp)
                Spacer(Modifier.height(4.dp))
                Text("Ingresa este código abajo", color = Color(0xFF5A6A85), fontSize = 11.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Input del token
            OutlinedTextField(
                value = state.enteredToken,
                onValueChange = viewModel::onTokenChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("_ _ _ _ _ _", color = Color(0xFF5A6A85),
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = state.isError,
                shape = RoundedCornerShape(14.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 8.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (state.isError) Color(0xFFFF6B6B) else Teal,
                    unfocusedBorderColor = if (state.isError) Color(0xFFFF6B6B) else BorderColor,
                    focusedContainerColor = InputBg,
                    unfocusedContainerColor = InputBg,
                    cursorColor = Teal
                )
            )

            if (state.isError) {
                Spacer(Modifier.height(8.dp))
                Text("Código incorrecto. Intenta de nuevo.",
                    color = Color(0xFFFF6B6B), fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::verify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C1426),
                    contentColor = Color.White
                )
            ) {
                Text("Verificar Token", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text("Hola, ${user.nombre} ${user.apellido}",
                color = Color(0xFF5A6A85), fontSize = 13.sp)
            Text(user.email, color = Color(0xFF5A6A85), fontSize = 12.sp)
        }
    }
}
