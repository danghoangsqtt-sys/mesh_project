package com.meshcommand.app.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.meshcommand.app.data.MeshDatabase
import com.meshcommand.app.data.dao.SoldierDao
import com.meshcommand.app.data.entity.SoldierEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataFlowIntegrationTest {

    private lateinit var db: MeshDatabase
    private lateinit var soldierDao: SoldierDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize an in-memory database that only exists in RAM for testing
        db = Room.inMemoryDatabaseBuilder(
            context, MeshDatabase::class.java
        ).allowMainThreadQueries().build()
        soldierDao = db.soldierDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun simulateEndToEndDataFlow_usbToDatabaseToUi() = runTest {
        // 1. Arrange: Simulate data packet parsed from USB FrameExtractor
        val parsedPacket = SoldierEntity(
            nodeId = 555,
            timestamp = System.currentTimeMillis(),
            lastSeenMs = System.currentTimeMillis(),
            latitude = 21.0285,
            longitude = 105.8542,
            heading = 45.0,
            heartRate = 78,
            spo2 = 99,
            temperature = 36.5f,
            humidity = 55f,
            pressure = 1010f,
            batteryVolts = 4.1f,
            statusFlags = 1,
            alertLevel = 0,
            isOnline = true
        )

        // 2. Act: Repository/Database saves the new packet
        soldierDao.upsert(parsedPacket)

        // 3. Assert: UI ViewModel collects the latest state from the Flow
        val soldiersState = soldierDao.getAll().first()
        
        // Ensure data integrity from "USB" to "UI" state
        assertThat(soldiersState).isNotEmpty()
        assertThat(soldiersState).hasSize(1)
        
        val uiSoldier = soldiersState[0]
        assertThat(uiSoldier.nodeId).isEqualTo(555)
        assertThat(uiSoldier.latitude).isEqualTo(21.0285)
        assertThat(uiSoldier.longitude).isEqualTo(105.8542)
        assertThat(uiSoldier.heartRate).isEqualTo(78)
        assertThat(uiSoldier.isOnline).isTrue()
    }
}
