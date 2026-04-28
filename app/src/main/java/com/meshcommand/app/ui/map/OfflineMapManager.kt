package com.meshcommand.app.ui.map

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Offline Map Manager — manages local MBTiles map packs.
 * Scans the maps directory for available .mbtiles files,
 * provides metadata, and handles file import.
 */
class OfflineMapManager(private val context: Context) {

    companion object {
        private const val TAG = "OfflineMapManager"
        private const val MAPS_DIR = "maps"
    }

    data class MapPack(
        val file: File,
        val name: String,
        val sizeMB: Double,
        val lastModified: Long
    )

    private val mapsDir: File
        get() = File(context.filesDir, MAPS_DIR).also { it.mkdirs() }

    /**
     * List all available .mbtiles map packs.
     */
    fun listMapPacks(): List<MapPack> {
        return mapsDir.listFiles()
            ?.filter { it.extension.equals("mbtiles", ignoreCase = true) }
            ?.map { file ->
                MapPack(
                    file = file,
                    name = file.nameWithoutExtension,
                    sizeMB = file.length().toDouble() / (1024 * 1024),
                    lastModified = file.lastModified()
                )
            }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /**
     * Get the currently active map pack (first available).
     */
    fun getActiveMapPack(): MapPack? {
        return listMapPacks().firstOrNull()
    }

    /**
     * Import a .mbtiles file from an external path into the maps directory.
     */
    fun importMapPack(sourceFile: File): Result<MapPack> {
        return try {
            val destFile = File(mapsDir, sourceFile.name)
            sourceFile.copyTo(destFile, overwrite = true)
            Log.i(TAG, "Imported map pack: ${destFile.name} (${destFile.length() / 1024 / 1024}MB)")
            Result.success(
                MapPack(
                    file = destFile,
                    name = destFile.nameWithoutExtension,
                    sizeMB = destFile.length().toDouble() / (1024 * 1024),
                    lastModified = destFile.lastModified()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import map pack: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Delete a map pack.
     */
    fun deleteMapPack(mapPack: MapPack): Boolean {
        return try {
            mapPack.file.delete().also {
                if (it) Log.i(TAG, "Deleted map pack: ${mapPack.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete: ${e.message}")
            false
        }
    }

    /**
     * Get total storage used by all map packs.
     */
    fun getTotalStorageMB(): Double {
        return listMapPacks().sumOf { it.sizeMB }
    }
}
