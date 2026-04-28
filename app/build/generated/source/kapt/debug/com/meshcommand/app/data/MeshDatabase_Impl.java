package com.meshcommand.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.meshcommand.app.data.dao.EventDao;
import com.meshcommand.app.data.dao.EventDao_Impl;
import com.meshcommand.app.data.dao.SoldierDao;
import com.meshcommand.app.data.dao.SoldierDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MeshDatabase_Impl extends MeshDatabase {
  private volatile SoldierDao _soldierDao;

  private volatile EventDao _eventDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `soldiers` (`node_id` INTEGER NOT NULL, `team_name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `heading` REAL NOT NULL, `heart_rate` INTEGER NOT NULL, `spo2` INTEGER NOT NULL, `temperature` REAL NOT NULL, `humidity` REAL NOT NULL, `pressure` REAL NOT NULL, `battery_volts` REAL NOT NULL, `status_flags` INTEGER NOT NULL, `alert_level` INTEGER NOT NULL, `is_online` INTEGER NOT NULL, `last_seen_ms` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`node_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp_ms` INTEGER NOT NULL, `event_type` TEXT NOT NULL, `severity` INTEGER NOT NULL, `node_id` INTEGER, `message` TEXT NOT NULL, `acknowledged` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd762132e55abaf1d80e81d15682d520c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `soldiers`");
        db.execSQL("DROP TABLE IF EXISTS `events`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSoldiers = new HashMap<String, TableInfo.Column>(17);
        _columnsSoldiers.put("node_id", new TableInfo.Column("node_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("team_name", new TableInfo.Column("team_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("heading", new TableInfo.Column("heading", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("heart_rate", new TableInfo.Column("heart_rate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("spo2", new TableInfo.Column("spo2", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("temperature", new TableInfo.Column("temperature", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("humidity", new TableInfo.Column("humidity", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("pressure", new TableInfo.Column("pressure", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("battery_volts", new TableInfo.Column("battery_volts", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("status_flags", new TableInfo.Column("status_flags", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("alert_level", new TableInfo.Column("alert_level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("is_online", new TableInfo.Column("is_online", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("last_seen_ms", new TableInfo.Column("last_seen_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSoldiers.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSoldiers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSoldiers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSoldiers = new TableInfo("soldiers", _columnsSoldiers, _foreignKeysSoldiers, _indicesSoldiers);
        final TableInfo _existingSoldiers = TableInfo.read(db, "soldiers");
        if (!_infoSoldiers.equals(_existingSoldiers)) {
          return new RoomOpenHelper.ValidationResult(false, "soldiers(com.meshcommand.app.data.entity.SoldierEntity).\n"
                  + " Expected:\n" + _infoSoldiers + "\n"
                  + " Found:\n" + _existingSoldiers);
        }
        final HashMap<String, TableInfo.Column> _columnsEvents = new HashMap<String, TableInfo.Column>(7);
        _columnsEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("timestamp_ms", new TableInfo.Column("timestamp_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("event_type", new TableInfo.Column("event_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("severity", new TableInfo.Column("severity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("node_id", new TableInfo.Column("node_id", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEvents.put("acknowledged", new TableInfo.Column("acknowledged", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEvents = new TableInfo("events", _columnsEvents, _foreignKeysEvents, _indicesEvents);
        final TableInfo _existingEvents = TableInfo.read(db, "events");
        if (!_infoEvents.equals(_existingEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "events(com.meshcommand.app.data.entity.EventEntity).\n"
                  + " Expected:\n" + _infoEvents + "\n"
                  + " Found:\n" + _existingEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d762132e55abaf1d80e81d15682d520c", "8b2bc3fb43d0dbd891b484925e0821c0");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "soldiers","events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `soldiers`");
      _db.execSQL("DELETE FROM `events`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SoldierDao.class, SoldierDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EventDao.class, EventDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SoldierDao soldierDao() {
    if (_soldierDao != null) {
      return _soldierDao;
    } else {
      synchronized(this) {
        if(_soldierDao == null) {
          _soldierDao = new SoldierDao_Impl(this);
        }
        return _soldierDao;
      }
    }
  }

  @Override
  public EventDao eventDao() {
    if (_eventDao != null) {
      return _eventDao;
    } else {
      synchronized(this) {
        if(_eventDao == null) {
          _eventDao = new EventDao_Impl(this);
        }
        return _eventDao;
      }
    }
  }
}
