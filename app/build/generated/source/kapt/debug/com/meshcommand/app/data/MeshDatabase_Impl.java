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
import com.meshcommand.app.data.dao.GeofenceDao;
import com.meshcommand.app.data.dao.GeofenceDao_Impl;
import com.meshcommand.app.data.dao.PositionHistoryDao;
import com.meshcommand.app.data.dao.PositionHistoryDao_Impl;
import com.meshcommand.app.data.dao.SoldierDao;
import com.meshcommand.app.data.dao.SoldierDao_Impl;
import com.meshcommand.app.data.dao.TeamDao;
import com.meshcommand.app.data.dao.TeamDao_Impl;
import com.meshcommand.app.data.dao.WaypointDao;
import com.meshcommand.app.data.dao.WaypointDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MeshDatabase_Impl extends MeshDatabase {
  private volatile SoldierDao _soldierDao;

  private volatile EventDao _eventDao;

  private volatile PositionHistoryDao _positionHistoryDao;

  private volatile TeamDao _teamDao;

  private volatile GeofenceDao _geofenceDao;

  private volatile WaypointDao _waypointDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `soldiers` (`node_id` INTEGER NOT NULL, `team_name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `heading` REAL NOT NULL, `heart_rate` INTEGER NOT NULL, `spo2` INTEGER NOT NULL, `temperature` REAL NOT NULL, `humidity` REAL NOT NULL, `pressure` REAL NOT NULL, `battery_volts` REAL NOT NULL, `status_flags` INTEGER NOT NULL, `alert_level` INTEGER NOT NULL, `is_online` INTEGER NOT NULL, `last_seen_ms` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`node_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp_ms` INTEGER NOT NULL, `event_type` TEXT NOT NULL, `severity` INTEGER NOT NULL, `node_id` INTEGER, `message` TEXT NOT NULL, `acknowledged` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `position_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `node_id` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `heading` REAL NOT NULL, `timestamp_ms` INTEGER NOT NULL, FOREIGN KEY(`node_id`) REFERENCES `soldiers`(`node_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_position_history_node_id` ON `position_history` (`node_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_position_history_timestamp_ms` ON `position_history` (`timestamp_ms`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `teams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `geofences` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `zone_type` TEXT NOT NULL, `points_json` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `waypoints` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `icon_type` TEXT NOT NULL, `assigned_node_id` INTEGER, `description` TEXT NOT NULL, `created_at` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c77d060cc338ac9c18c8603b981f67a5')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `soldiers`");
        db.execSQL("DROP TABLE IF EXISTS `events`");
        db.execSQL("DROP TABLE IF EXISTS `position_history`");
        db.execSQL("DROP TABLE IF EXISTS `teams`");
        db.execSQL("DROP TABLE IF EXISTS `geofences`");
        db.execSQL("DROP TABLE IF EXISTS `waypoints`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsPositionHistory = new HashMap<String, TableInfo.Column>(6);
        _columnsPositionHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPositionHistory.put("node_id", new TableInfo.Column("node_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPositionHistory.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPositionHistory.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPositionHistory.put("heading", new TableInfo.Column("heading", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPositionHistory.put("timestamp_ms", new TableInfo.Column("timestamp_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPositionHistory = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPositionHistory.add(new TableInfo.ForeignKey("soldiers", "CASCADE", "NO ACTION", Arrays.asList("node_id"), Arrays.asList("node_id")));
        final HashSet<TableInfo.Index> _indicesPositionHistory = new HashSet<TableInfo.Index>(2);
        _indicesPositionHistory.add(new TableInfo.Index("index_position_history_node_id", false, Arrays.asList("node_id"), Arrays.asList("ASC")));
        _indicesPositionHistory.add(new TableInfo.Index("index_position_history_timestamp_ms", false, Arrays.asList("timestamp_ms"), Arrays.asList("ASC")));
        final TableInfo _infoPositionHistory = new TableInfo("position_history", _columnsPositionHistory, _foreignKeysPositionHistory, _indicesPositionHistory);
        final TableInfo _existingPositionHistory = TableInfo.read(db, "position_history");
        if (!_infoPositionHistory.equals(_existingPositionHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "position_history(com.meshcommand.app.data.entity.PositionHistoryEntity).\n"
                  + " Expected:\n" + _infoPositionHistory + "\n"
                  + " Found:\n" + _existingPositionHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsTeams = new HashMap<String, TableInfo.Column>(4);
        _columnsTeams.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeams.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeams.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeams.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTeams = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTeams = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTeams = new TableInfo("teams", _columnsTeams, _foreignKeysTeams, _indicesTeams);
        final TableInfo _existingTeams = TableInfo.read(db, "teams");
        if (!_infoTeams.equals(_existingTeams)) {
          return new RoomOpenHelper.ValidationResult(false, "teams(com.meshcommand.app.data.entity.TeamEntity).\n"
                  + " Expected:\n" + _infoTeams + "\n"
                  + " Found:\n" + _existingTeams);
        }
        final HashMap<String, TableInfo.Column> _columnsGeofences = new HashMap<String, TableInfo.Column>(6);
        _columnsGeofences.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofences.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofences.put("zone_type", new TableInfo.Column("zone_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofences.put("points_json", new TableInfo.Column("points_json", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofences.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGeofences.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGeofences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGeofences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGeofences = new TableInfo("geofences", _columnsGeofences, _foreignKeysGeofences, _indicesGeofences);
        final TableInfo _existingGeofences = TableInfo.read(db, "geofences");
        if (!_infoGeofences.equals(_existingGeofences)) {
          return new RoomOpenHelper.ValidationResult(false, "geofences(com.meshcommand.app.data.entity.GeofenceEntity).\n"
                  + " Expected:\n" + _infoGeofences + "\n"
                  + " Found:\n" + _existingGeofences);
        }
        final HashMap<String, TableInfo.Column> _columnsWaypoints = new HashMap<String, TableInfo.Column>(8);
        _columnsWaypoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("icon_type", new TableInfo.Column("icon_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("assigned_node_id", new TableInfo.Column("assigned_node_id", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWaypoints.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWaypoints = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWaypoints = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWaypoints = new TableInfo("waypoints", _columnsWaypoints, _foreignKeysWaypoints, _indicesWaypoints);
        final TableInfo _existingWaypoints = TableInfo.read(db, "waypoints");
        if (!_infoWaypoints.equals(_existingWaypoints)) {
          return new RoomOpenHelper.ValidationResult(false, "waypoints(com.meshcommand.app.data.entity.WaypointEntity).\n"
                  + " Expected:\n" + _infoWaypoints + "\n"
                  + " Found:\n" + _existingWaypoints);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c77d060cc338ac9c18c8603b981f67a5", "6f12054183ef0a71f63a87a63c1d9f6f");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "soldiers","events","position_history","teams","geofences","waypoints");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `soldiers`");
      _db.execSQL("DELETE FROM `events`");
      _db.execSQL("DELETE FROM `position_history`");
      _db.execSQL("DELETE FROM `teams`");
      _db.execSQL("DELETE FROM `geofences`");
      _db.execSQL("DELETE FROM `waypoints`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(PositionHistoryDao.class, PositionHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TeamDao.class, TeamDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GeofenceDao.class, GeofenceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WaypointDao.class, WaypointDao_Impl.getRequiredConverters());
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

  @Override
  public PositionHistoryDao positionHistoryDao() {
    if (_positionHistoryDao != null) {
      return _positionHistoryDao;
    } else {
      synchronized(this) {
        if(_positionHistoryDao == null) {
          _positionHistoryDao = new PositionHistoryDao_Impl(this);
        }
        return _positionHistoryDao;
      }
    }
  }

  @Override
  public TeamDao teamDao() {
    if (_teamDao != null) {
      return _teamDao;
    } else {
      synchronized(this) {
        if(_teamDao == null) {
          _teamDao = new TeamDao_Impl(this);
        }
        return _teamDao;
      }
    }
  }

  @Override
  public GeofenceDao geofenceDao() {
    if (_geofenceDao != null) {
      return _geofenceDao;
    } else {
      synchronized(this) {
        if(_geofenceDao == null) {
          _geofenceDao = new GeofenceDao_Impl(this);
        }
        return _geofenceDao;
      }
    }
  }

  @Override
  public WaypointDao waypointDao() {
    if (_waypointDao != null) {
      return _waypointDao;
    } else {
      synchronized(this) {
        if(_waypointDao == null) {
          _waypointDao = new WaypointDao_Impl(this);
        }
        return _waypointDao;
      }
    }
  }
}
