package com.example.fisgon.presentation.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fisgon.domain.entity.User

private val BgColor = Color(0xFF090E1C)
private val Teal = Color(0xFF00C9A0)
private val InputBg = Color(0xFF111827)
private val BorderColor = Color(0xFF1E2D47)
private val TextMuted = Color(0xFF5A6A85)
private val Warning = Color(0xFFFFC857)

@Composable
fun PermissionsScreen(
    user: User,
    controller: PermissionsController,
    onContinue: () -> Unit
) {
    val hasMissingPermissions =
        controller.cameraStatus == PermissionGrantStatus.Missing ||
            controller.fineLocationStatus == PermissionGrantStatus.Missing ||
            controller.backgroundLocationStatus == PermissionGrantStatus.Missing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .safeContentPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        Text(
            text = "PERMISOS",
            color = Teal,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )
        Text(
            text = "Seguridad activa",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hola, ${user.nombre}. Activa los permisos para usar todas las funciones.",
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        PermissionRow(
            title = "Cámara",
            description = "Captura evidencia cuando reportes una alerta.",
            status = controller.cameraStatus,
            buttonText = "Permitir cámara",
            onRequest = controller::requestCamera
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            title = "Ubicación precisa",
            description = "Ubica reportes y zonas de riesgo en el mapa.",
            status = controller.fineLocationStatus,
            buttonText = "Permitir ubicación",
            onRequest = controller::requestFineLocation
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            title = "Ubicación en segundo plano",
            description = "Mantiene protección activa fuera de la app.",
            status = controller.backgroundLocationStatus,
            buttonText = "Permitir segundo plano",
            enabled = controller.canRequestBackgroundLocation,
            helperText = if (controller.canRequestBackgroundLocation) null else "Primero permite la ubicación precisa.",
            onRequest = controller::requestBackgroundLocation
        )

        if (hasMissingPermissions) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF201B0D), RoundedCornerShape(12.dp))
                    .border(1.dp, Warning.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Puedes continuar, pero algunas funciones quedarán limitadas hasta conceder permisos.",
                    color = Warning,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Teal,
                contentColor = Color(0xFF06110F)
            )
        ) {
            Text("Continuar al inicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    status: PermissionGrantStatus,
    buttonText: String,
    onRequest: () -> Unit,
    enabled: Boolean = true,
    helperText: String? = null
) {
    val granted = status == PermissionGrantStatus.Granted

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(InputBg, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (granted) "Concedido" else "Pendiente",
                color = if (granted) Teal else Warning,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = description,
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        helperText?.let {
            Text(
                text = it,
                color = Warning,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (!granted) {
            Button(
                onClick = onRequest,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C1426),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF0C1426).copy(alpha = 0.45f),
                    disabledContentColor = TextMuted
                )
            ) {
                Text(buttonText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
