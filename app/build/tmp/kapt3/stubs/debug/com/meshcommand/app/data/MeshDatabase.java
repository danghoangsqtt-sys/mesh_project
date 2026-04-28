package com.meshcommand.app.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\t"}, d2 = {"Lcom/meshcommand/app/data/MeshDatabase;", "Landroidx/room/RoomDatabase;", "()V", "eventDao", "Lcom/meshcommand/app/data/dao/EventDao;", "positionHistoryDao", "Lcom/meshcommand/app/data/dao/PositionHistoryDao;", "soldierDao", "Lcom/meshcommand/app/data/dao/SoldierDao;", "app_debug"})
@androidx.room.Database(entities = {com.meshcommand.app.data.entity.SoldierEntity.class, com.meshcommand.app.data.entity.EventEntity.class, com.meshcommand.app.data.entity.PositionHistoryEntity.class}, version = 2, exportSchema = false)
public abstract class MeshDatabase extends androidx.room.RoomDatabase {
    
    public MeshDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.meshcommand.app.data.dao.SoldierDao soldierDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.meshcommand.app.data.dao.EventDao eventDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.meshcommand.app.data.dao.PositionHistoryDao positionHistoryDao();
}