package com.meshcommand.app.ui.tactical

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshcommand.app.comm.ConnectionState
import com.meshcommand.app.ui.theme.MeshColors

@Composable
fun TacticalPanel(
    viewModel: TacticalViewModel,
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSendCommand: (String) -> Unit,
    onSoldierClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val soldiers by viewModel.soldiers.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val onlineCount by viewModel.onlineCount.collectAsState()
    val lastRxTime by viewModel.lastRxTime.collectAsState()
    val events by viewModel.recentEvents.collectAsState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MeshColors.BgDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Title
        Text(
            text = "⚔ MESH COMMAND",
            color = MeshColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // Connection Bar
        ConnectionBar(
            connectionState = connectionState,
            onConnect = onConnect,
            onDisconnect = onDisconnect
        )

        // Status Bar
        StatusBar(
            onlineCount = onlineCount,
            lastRxTimeMs = lastRxTime
        )

        // Node Table (takes available space)
        NodeTable(
            soldiers = soldiers,
            selectedNodeId = selectedNodeId,
            onSoldierClick = { nodeId ->
                viewModel.selectSoldier(nodeId)
                onSoldierClick(nodeId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Command Panel
        CommandPanel(
            onSendCommand = onSendCommand,
            nodeIds = soldiers.map { it.nodeId }
        )

        // Event Log
        EventLog(
            events = events,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        )
    }
}
