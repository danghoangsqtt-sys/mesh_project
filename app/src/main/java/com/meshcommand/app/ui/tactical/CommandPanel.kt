package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.ui.theme.MeshColors

@Composable
fun CommandPanel(
    onSendCommand: (message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshColors.BgLight, RoundedCornerShape(4.dp))
            .border(1.dp, MeshColors.Accent, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "📡 COMMAND & CONTROL",
            color = MeshColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        BasicTextField(
            value = messageText,
            onValueChange = { messageText = it },
            textStyle = TextStyle(
                color = MeshColors.TextPrimary,
                fontSize = 12.sp
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendCommand(messageText.trim())
                        messageText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeshColors.ButtonPrimary
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Send",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
