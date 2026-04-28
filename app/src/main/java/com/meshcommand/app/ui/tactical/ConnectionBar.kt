package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
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
        is ConnectionState.Connected -> Triple(MeshColors.StatusOk, "ONLINE", connectionState.deviceName)
        is ConnectionState.Connecting -> Triple(MeshColors.StatusWarn, "CONNECTING...", "")
        is ConnectionState.Error -> Triple(MeshColors.StatusCrit, "ERROR", connectionState.message)
        is ConnectionState.Disconnected -> Triple(MeshColors.StatusOffline, "OFFLINE", "")
    }

    val isConnected = connectionState is ConnectionState.Connected
    var expanded by remember { mutableStateOf(false) }
    var selectedGateway by remember { mutableStateOf("USB OTG") }
    val gatewayOptions = listOf("USB OTG", "WiFi AP")
    var baudExpanded by remember { mutableStateOf(false) }
    var selectedBaud by remember { mutableStateOf("115200") }
    val baudOptions = listOf("9600", "19200", "57600", "115200", "230400")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshColors.BgLight, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header: GATEWAY CONNECTION
        Text(
            text = "⚡ GATEWAY CONNECTION",
            color = MeshColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Row 1: Gateway type + Baud rate selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gateway type dropdown
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(width = 100.dp, height = 32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeshColors.TextPrimary
                    )
                ) {
                    Text(selectedGateway, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    gatewayOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 12.sp) },
                            onClick = {
                                selectedGateway = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Baud label + dropdown
            Text("Baud:", color = MeshColors.TextSecondary, fontSize = 10.sp)
            Box {
                OutlinedButton(
                    onClick = { baudExpanded = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(width = 80.dp, height = 32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeshColors.TextPrimary
                    )
                ) {
                    Text(selectedBaud, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = baudExpanded,
                    onDismissRequest = { baudExpanded = false }
                ) {
                    baudOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 12.sp) },
                            onClick = {
                                selectedBaud = option
                                baudExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Row 2: Connect button + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connect/Disconnect button
            Button(
                onClick = { if (isConnected) onDisconnect() else onConnect() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) MeshColors.StatusCrit else MeshColors.ButtonPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(width = 100.dp, height = 32.dp)
            ) {
                Text(
                    text = if (isConnected) "Disconnect" else "Connect",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status dot + text
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = statusText,
                color = dotColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            // Device name if connected
            if (deviceText.isNotEmpty()) {
                Text(
                    text = deviceText,
                    color = MeshColors.TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
