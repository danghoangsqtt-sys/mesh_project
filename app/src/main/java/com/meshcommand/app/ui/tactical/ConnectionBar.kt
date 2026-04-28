package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.comm.ConnectionState
import com.meshcommand.app.ui.theme.MeshColors

@Composable
fun ConnectionBar(
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (dotColor, statusText, deviceText) = when (connectionState) {
        is ConnectionState.Connected -> Triple(MeshColors.StatusOk, "CONNECTED", connectionState.deviceName)
        is ConnectionState.Connecting -> Triple(MeshColors.StatusWarn, "CONNECTING...", "")
        is ConnectionState.Error -> Triple(MeshColors.StatusCrit, "ERROR", connectionState.message)
        is ConnectionState.Disconnected -> Triple(MeshColors.StatusOffline, "DISCONNECTED", "")
    }

    val isConnected = connectionState is ConnectionState.Connected

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshColors.BgLight, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("📡", fontSize = 16.sp)

        // Status dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Text(
            text = statusText,
            color = dotColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        if (deviceText.isNotEmpty()) {
            Text(
                text = deviceText,
                color = MeshColors.TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        Button(
            onClick = { if (isConnected) onDisconnect() else onConnect() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) MeshColors.StatusCrit else MeshColors.ButtonPrimary
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = if (isConnected) "Disconnect" else "Connect",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
