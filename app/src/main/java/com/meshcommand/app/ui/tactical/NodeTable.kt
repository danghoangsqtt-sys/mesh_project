package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.data.entity.SoldierEntity
import com.meshcommand.app.ui.theme.MeshColors
import com.meshcommand.app.ui.theme.alertColor
import com.meshcommand.app.ui.theme.alertText

@Composable
fun NodeTable(
    soldiers: List<SoldierEntity>,
    selectedNodeId: Int?,
    onSoldierClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(MeshColors.SurfaceDark, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeshColors.BgLight)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderCell("ID", 32.dp)
                HeaderCell("STS", 56.dp)
                HeaderCell("HR", 36.dp)
                HeaderCell("SpO₂", 40.dp)
                HeaderCell("Temp", 48.dp)
                HeaderCell("Bat", 44.dp)
            }
        }

        items(soldiers, key = { it.nodeId }) { soldier ->
            val isSelected = soldier.nodeId == selectedNodeId
            val statusColor = alertColor(soldier.alertLevel, soldier.isOnline)
            val statusText = alertText(soldier.alertLevel, soldier.isOnline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) MeshColors.Accent.copy(alpha = 0.5f)
                        else MeshColors.SurfaceDark
                    )
                    .clickable { onSoldierClick(soldier.nodeId) }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ID
                Text(
                    text = "${soldier.nodeId}",
                    color = MeshColors.TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.size(width = 32.dp, height = 18.dp),
                    textAlign = TextAlign.Center
                )

                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.size(width = 56.dp, height = 18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // HR
                Text(
                    text = "${soldier.heartRate}",
                    color = MeshColors.TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.size(width = 36.dp, height = 18.dp),
                    textAlign = TextAlign.Center
                )

                // SpO2
                Text(
                    text = "${soldier.spo2}%",
                    color = when {
                        soldier.spo2 < 90 -> MeshColors.StatusCrit
                        soldier.spo2 < 95 -> MeshColors.StatusWarn
                        else -> MeshColors.StatusOk
                    },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.size(width = 40.dp, height = 18.dp),
                    textAlign = TextAlign.Center
                )

                // Temp
                Text(
                    text = "${"%.1f".format(soldier.temperature)}°",
                    color = when {
                        soldier.temperature > 38.5f -> MeshColors.StatusCrit
                        soldier.temperature > 37.5f -> MeshColors.StatusWarn
                        else -> MeshColors.StatusOk
                    },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.size(width = 48.dp, height = 18.dp),
                    textAlign = TextAlign.Center
                )

                // Battery
                Text(
                    text = "${"%.1f".format(soldier.batteryVolts)}V",
                    color = if (soldier.batteryVolts < 3.5) MeshColors.StatusWarn else MeshColors.TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.size(width = 44.dp, height = 18.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = MeshColors.TextPrimary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.size(width = width, height = 16.dp),
        textAlign = TextAlign.Center
    )
}
