package com.meshcommand.app.ui.tactical

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.meshcommand.app.data.entity.SoldierEntity
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class NodeTableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun nodeTable_displaysSoldierData() {
        val mockSoldiers = listOf(
            SoldierEntity(
                nodeId = 101,
                timestamp = System.currentTimeMillis(),
                latitude = 21.0,
                longitude = 105.0,
                heading = 90.0,
                heartRate = 85,
                spo2 = 98,
                temperature = 36.5f,
                humidity = 50.0f,
                pressure = 1000.0f,
                batteryVolts = 4.1f,
                statusFlags = 0,
                alertLevel = 0,
                isOnline = true
            )
        )

        composeTestRule.setContent {
            NodeTable(
                soldiers = mockSoldiers,
                selectedNodeId = null,
                onSoldierClick = {}
            )
        }

        // Verify ID is displayed
        composeTestRule.onNodeWithText("101").assertExists()
        
        // Verify Heart Rate is displayed
        composeTestRule.onNodeWithText("85").assertExists()
        
        // Verify SpO2 is displayed
        composeTestRule.onNodeWithText("98%").assertExists()
        
        // Verify Temperature is displayed
        composeTestRule.onNodeWithText("36.5°").assertExists()
        
        // Verify Battery is displayed
        composeTestRule.onNodeWithText("4.1V").assertExists()
    }

    @Test
    fun nodeTable_triggersOnClick() {
        val mockSoldiers = listOf(
            SoldierEntity(
                nodeId = 202,
                timestamp = System.currentTimeMillis(),
                latitude = 21.0,
                longitude = 105.0,
                heading = 90.0,
                heartRate = 75,
                spo2 = 96,
                temperature = 37.0f,
                humidity = 50.0f,
                pressure = 1000.0f,
                batteryVolts = 3.9f,
                statusFlags = 0,
                alertLevel = 0,
                isOnline = true
            )
        )

        var clickedId: Int? = null

        composeTestRule.setContent {
            NodeTable(
                soldiers = mockSoldiers,
                selectedNodeId = null,
                onSoldierClick = { id -> clickedId = id }
            )
        }

        composeTestRule.onNodeWithText("202").performClick()
        
        assertEquals(202, clickedId)
    }
}
