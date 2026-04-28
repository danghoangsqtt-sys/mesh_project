package com.meshcommand.app.ui.map;

/**
 * Offline Map Manager — manages local MBTiles map packs.
 * Scans the maps directory for available .mbtiles files,
 * provides metadata, and handles file import.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0003\u0018\u0000 \u00172\u00020\u0001:\u0002\u0017\u0018B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\b\u0010\r\u001a\u0004\u0018\u00010\fJ\u0006\u0010\u000e\u001a\u00020\u000fJ!\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u00112\u0006\u0010\u0012\u001a\u00020\u0006\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\u0014J\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00068BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\b\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0019"}, d2 = {"Lcom/meshcommand/app/ui/map/OfflineMapManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "mapsDir", "Ljava/io/File;", "getMapsDir", "()Ljava/io/File;", "deleteMapPack", "", "mapPack", "Lcom/meshcommand/app/ui/map/OfflineMapManager$MapPack;", "getActiveMapPack", "getTotalStorageMB", "", "importMapPack", "Lkotlin/Result;", "sourceFile", "importMapPack-IoAF18A", "(Ljava/io/File;)Ljava/lang/Object;", "listMapPacks", "", "Companion", "MapPack", "app_debug"})
public final class OfflineMapManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "OfflineMapManager";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String MAPS_DIR = "maps";
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.ui.map.OfflineMapManager.Companion Companion = null;
    
    public OfflineMapManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.io.File getMapsDir() {
        return null;
    }
    
    /**
     * List all available .mbtiles map packs.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.meshcommand.app.ui.map.OfflineMapManager.MapPack> listMapPacks() {
        return null;
    }
    
    /**
     * Get the currently active map pack (first available).
     */
    @org.jetbrains.annotations.Nullable()
    public final com.meshcommand.app.ui.map.OfflineMapManager.MapPack getActiveMapPack() {
        return null;
    }
    
    /**
     * Delete a map pack.
     */
    public final boolean deleteMapPack(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.ui.map.OfflineMapManager.MapPack mapPack) {
        return false;
    }
    
    /**
     * Get total storage used by all map packs.
     */
    public final double getTotalStorageMB() {
        return 0.0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/meshcommand/app/ui/map/OfflineMapManager$Companion;", "", "()V", "MAPS_DIR", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\t\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\tH\u00c6\u0003J1\u0010\u0017\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001b\u001a\u00020\u001cH\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001e"}, d2 = {"Lcom/meshcommand/app/ui/map/OfflineMapManager$MapPack;", "", "file", "Ljava/io/File;", "name", "", "sizeMB", "", "lastModified", "", "(Ljava/io/File;Ljava/lang/String;DJ)V", "getFile", "()Ljava/io/File;", "getLastModified", "()J", "getName", "()Ljava/lang/String;", "getSizeMB", "()D", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class MapPack {
        @org.jetbrains.annotations.NotNull()
        private final java.io.File file = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        private final double sizeMB = 0.0;
        private final long lastModified = 0L;
        
        public MapPack(@org.jetbrains.annotations.NotNull()
        java.io.File file, @org.jetbrains.annotations.NotNull()
        java.lang.String name, double sizeMB, long lastModified) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.io.File getFile() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        public final double getSizeMB() {
            return 0.0;
        }
        
        public final long getLastModified() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.io.File component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final double component3() {
            return 0.0;
        }
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.meshcommand.app.ui.map.OfflineMapManager.MapPack copy(@org.jetbrains.annotations.NotNull()
        java.io.File file, @org.jetbrains.annotations.NotNull()
        java.lang.String name, double sizeMB, long lastModified) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}