package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.ui.theme.MeshColors

/**
 * Command & Control panel matching desktop app layout.
 * Features: message input, message type (Broadcast/Direct/Command), target node selector.
 */
@Composable
fun CommandPanel(
    onSendCommand: (message: String) -> Unit,
    nodeIds: List<Int> = emptyList(),
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    var msgTypeExpanded by remember { mutableStateOf(false) }
    var selectedMsgType by remember { mutableStateOf("Broadcast") }
    val msgTypes = listOf("Broadcast", "Direct", "Command", "Config")
    var targetExpanded by remember { mutableStateOf(false) }
    var selectedTarget by remember { mutableStateOf("All") }

    // Config states
    var configSf by remember { mutableStateOf("9") }
    val sfOptions = listOf("7", "8", "9", "10", "11", "12")
    var sfExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshColors.BgLight, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Title
        Text(
            text = "📡 COMMAND & CONTROL",
            color = MeshColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Message / Config input
        if (selectedMsgType == "Config") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeshColors.SurfaceDark, RoundedCornerShape(4.dp))
                    .border(1.dp, MeshColors.Border, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spreading Factor:",
                    color = MeshColors.TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Box {
                    OutlinedButton(
                        onClick = { sfExpanded = true },
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeshColors.TextPrimary)
                    ) {
                        Text("SF$configSf", fontSize = 10.sp)
                    }
                    DropdownMenu(
                        expanded = sfExpanded,
                        onDismissRequest = { sfExpanded = false }
                    ) {
                        sfOptions.forEach { sf ->
                            DropdownMenuItem(
                                text = { Text("SF$sf", fontSize = 12.sp) },
                                onClick = {
                                    configSf = sf
                                    sfExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                textStyle = TextStyle(
                    color = MeshColors.TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(MeshColors.TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeshColors.SurfaceDark, RoundedCornerShape(4.dp))
                    .border(1.dp, MeshColors.Border, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                decorationBox = { innerTextField ->
                    if (messageText.isEmpty()) {
                        Text(
                            text = "Type message...",
                            color = MeshColors.TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Row: Message Type + Target dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Message type dropdown
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { msgTypeExpanded = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeshColors.TextPrimary
                    )
                ) {
                    Text(selectedMsgType, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = msgTypeExpanded,
                    onDismissRequest = { msgTypeExpanded = false }
                ) {
                    msgTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, fontSize = 12.sp) },
                            onClick = {
                                selectedMsgType = type
                                msgTypeExpanded = false
                                if (type == "Broadcast") selectedTarget = "All"
                            }
                        )
                    }
                }
            }

            // Target node dropdown
            Box(modifier = Modifier.weight(1f)) {
                val targetOptions = listOf("All") + nodeIds.map { "Node $it" }
                OutlinedButton(
                    onClick = { targetExpanded = true },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MeshColors.TextPrimary
                    ),
                    enabled = selectedMsgType != "Broadcast"
                ) {
                    Text(selectedTarget, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                DropdownMenu(
                    expanded = targetExpanded,
                    onDismissRequest = { targetExpanded = false }
                ) {
                    targetOptions.forEach { target ->
                        DropdownMenuItem(
                            text = { Text(target, fontSize = 12.sp) },
                            onClick = {
                                selectedTarget = target
                                targetExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Send button — full width like desktop app
        Button(
            onClick = {
                val isTargetBroadcast = selectedTarget == "All"
                val prefix = when (selectedMsgType) {
                    "Broadcast" -> "[BROADCAST]"
                    "Direct" -> if (isTargetBroadcast) "[BROADCAST]" else "[DM→$selectedTarget]"
                    "Command" -> if (isTargetBroadcast) "[BROADCAST]" else "[CMD→$selectedTarget]"
                    "Config" -> if (isTargetBroadcast) "[BROADCAST]" else "[CFG→$selectedTarget]"
                    else -> ""
                }
                
                if (selectedMsgType == "Config") {
                    onSendCommand("$prefix CFG:SF=$configSf")
                } else if (messageText.isNotBlank()) {
                    onSendCommand("$prefix $messageText".trim())
                    messageText = ""
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MeshColors.ButtonPrimary
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Text(
                text = if (selectedMsgType == "Config") "Send Config" else "Send Message",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
