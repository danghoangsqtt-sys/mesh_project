package com.meshcommand.app.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\u0007\u001a\u00020\u00062\b\b\u0001\u0010\b\u001a\u00020\tH\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0012\u0010\u000e\u001a\u00020\u000f2\b\b\u0001\u0010\b\u001a\u00020\tH\u0007\u00a8\u0006\u0010"}, d2 = {"Lcom/meshcommand/app/di/AppModule;", "", "()V", "provideEventDao", "Lcom/meshcommand/app/data/dao/EventDao;", "database", "Lcom/meshcommand/app/data/MeshDatabase;", "provideMeshDatabase", "context", "Landroid/content/Context;", "providePositionHistoryDao", "Lcom/meshcommand/app/data/dao/PositionHistoryDao;", "provideSoldierDao", "Lcom/meshcommand/app/data/dao/SoldierDao;", "provideUsbSerialManager", "Lcom/meshcommand/app/comm/UsbSerialManager;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.meshcommand.app.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.comm.UsbSerialManager provideUsbSerialManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.data.MeshDatabase provideMeshDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.data.dao.SoldierDao provideSoldierDao(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.MeshDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.data.dao.EventDao provideEventDao(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.MeshDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.meshcommand.app.data.dao.PositionHistoryDao providePositionHistoryDao(@org.jetbrains.annotations.NotNull()
    com.meshcommand.app.data.MeshDatabase database) {
        return null;
    }
}