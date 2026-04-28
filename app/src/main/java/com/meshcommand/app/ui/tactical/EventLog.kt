package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.data.entity.EventEntity
import com.meshcommand.app.ui.theme.MeshColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventLog(
    events: List<EventEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshColors.BgLight, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = "📋 EVENT LOG",
            color = MeshColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MeshColors.SurfaceDark, RoundedCornerShape(4.dp))
                .padding(4.dp)
        ) {
            items(events, key = { it.id }) { event ->
                EventRow(event)
            }
        }
    }
}

@Composable
private fun EventRow(event: EventEntity) {
    val severityColor = when (event.severity) {
        2 -> MeshColors.StatusCrit
        1 -> MeshColors.StatusWarn
        else -> MeshColors.TextSecondary
    }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(Date(event.timestampMs))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = timeStr,
            color = MeshColors.TextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = event.message,
            color = severityColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
