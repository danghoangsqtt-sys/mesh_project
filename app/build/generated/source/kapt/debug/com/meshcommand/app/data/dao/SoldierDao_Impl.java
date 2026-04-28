package com.meshcommand.app.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.meshcommand.app.data.entity.SoldierEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SoldierDao_Impl implements SoldierDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfMarkOffline;

  private final EntityUpsertionAdapter<SoldierEntity> __upsertionAdapterOfSoldierEntity;

  public SoldierDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfMarkOffline = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE soldiers SET is_online = 0, alert_level = 0 WHERE last_seen_ms < ? AND is_online = 1";
        return _query;
      }
    };
    this.__upsertionAdapterOfSoldierEntity = new EntityUpsertionAdapter<SoldierEntity>(new EntityInsertionAdapter<SoldierEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `soldiers` (`node_id`,`team_name`,`latitude`,`longitude`,`heading`,`heart_rate`,`spo2`,`temperature`,`humidity`,`pressure`,`battery_volts`,`status_flags`,`alert_level`,`is_online`,`last_seen_ms`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SoldierEntity entity) {
        statement.bindLong(1, entity.getNodeId());
        if (entity.getTeamName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTeamName());
        }
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindDouble(5, entity.getHeading());
        statement.bindLong(6, entity.getHeartRate());
        statement.bindLong(7, entity.getSpo2());
        statement.bindDouble(8, entity.getTemperature());
        statement.bindDouble(9, entity.getHumidity());
        statement.bindDouble(10, entity.getPressure());
        statement.bindDouble(11, entity.getBatteryVolts());
        statement.bindLong(12, entity.getStatusFlags());
        statement.bindLong(13, entity.getAlertLevel());
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(14, _tmp);
        statement.bindLong(15, entity.getLastSeenMs());
        statement.bindLong(16, entity.getCreatedAt());
        statement.bindLong(17, entity.getUpdatedAt());
      }
    }, new EntityDeletionOrUpdateAdapter<SoldierEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `soldiers` SET `node_id` = ?,`team_name` = ?,`latitude` = ?,`longitude` = ?,`heading` = ?,`heart_rate` = ?,`spo2` = ?,`temperature` = ?,`humidity` = ?,`pressure` = ?,`battery_volts` = ?,`status_flags` = ?,`alert_level` = ?,`is_online` = ?,`last_seen_ms` = ?,`created_at` = ?,`updated_at` = ? WHERE `node_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SoldierEntity entity) {
        statement.bindLong(1, entity.getNodeId());
        if (entity.getTeamName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTeamName());
        }
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindDouble(5, entity.getHeading());
        statement.bindLong(6, entity.getHeartRate());
        statement.bindLong(7, entity.getSpo2());
        statement.bindDouble(8, entity.getTemperature());
        statement.bindDouble(9, entity.getHumidity());
        statement.bindDouble(10, entity.getPressure());
        statement.bindDouble(11, entity.getBatteryVolts());
        statement.bindLong(12, entity.getStatusFlags());
        statement.bindLong(13, entity.getAlertLevel());
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(14, _tmp);
        statement.bindLong(15, entity.getLastSeenMs());
        statement.bindLong(16, entity.getCreatedAt());
        statement.bindLong(17, entity.getUpdatedAt());
        statement.bindLong(18, entity.getNodeId());
      }
    });
  }

  @Override
  public Object markOffline(final long thresholdMs, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkOffline.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, thresholdMs);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkOffline.release(_stmt);
        }
      }
    }, arg1);
  }

  @Override
  public Object upsert(final SoldierEntity soldier, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfSoldierEntity.upsert(soldier);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Flow<List<SoldierEntity>> getAll() {
    final String _sql = "SELECT * FROM soldiers ORDER BY alert_level DESC, node_id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"soldiers"}, new Callable<List<SoldierEntity>>() {
      @Override
      @NonNull
      public List<SoldierEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "node_id");
          final int _cursorIndexOfTeamName = CursorUtil.getColumnIndexOrThrow(_cursor, "team_name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfHeading = CursorUtil.getColumnIndexOrThrow(_cursor, "heading");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heart_rate");
          final int _cursorIndexOfSpo2 = CursorUtil.getColumnIndexOrThrow(_cursor, "spo2");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfBatteryVolts = CursorUtil.getColumnIndexOrThrow(_cursor, "battery_volts");
          final int _cursorIndexOfStatusFlags = CursorUtil.getColumnIndexOrThrow(_cursor, "status_flags");
          final int _cursorIndexOfAlertLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_level");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfLastSeenMs = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen_ms");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<SoldierEntity> _result = new ArrayList<SoldierEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SoldierEntity _item;
            final int _tmpNodeId;
            _tmpNodeId = _cursor.getInt(_cursorIndexOfNodeId);
            final String _tmpTeamName;
            if (_cursor.isNull(_cursorIndexOfTeamName)) {
              _tmpTeamName = null;
            } else {
              _tmpTeamName = _cursor.getString(_cursorIndexOfTeamName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpHeading;
            _tmpHeading = _cursor.getDouble(_cursorIndexOfHeading);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpSpo2;
            _tmpSpo2 = _cursor.getInt(_cursorIndexOfSpo2);
            final double _tmpTemperature;
            _tmpTemperature = _cursor.getDouble(_cursorIndexOfTemperature);
            final double _tmpHumidity;
            _tmpHumidity = _cursor.getDouble(_cursorIndexOfHumidity);
            final double _tmpPressure;
            _tmpPressure = _cursor.getDouble(_cursorIndexOfPressure);
            final double _tmpBatteryVolts;
            _tmpBatteryVolts = _cursor.getDouble(_cursorIndexOfBatteryVolts);
            final int _tmpStatusFlags;
            _tmpStatusFlags = _cursor.getInt(_cursorIndexOfStatusFlags);
            final int _tmpAlertLevel;
            _tmpAlertLevel = _cursor.getInt(_cursorIndexOfAlertLevel);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final long _tmpLastSeenMs;
            _tmpLastSeenMs = _cursor.getLong(_cursorIndexOfLastSeenMs);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new SoldierEntity(_tmpNodeId,_tmpTeamName,_tmpLatitude,_tmpLongitude,_tmpHeading,_tmpHeartRate,_tmpSpo2,_tmpTemperature,_tmpHumidity,_tmpPressure,_tmpBatteryVolts,_tmpStatusFlags,_tmpAlertLevel,_tmpIsOnline,_tmpLastSeenMs,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<SoldierEntity> getByNodeId(final int nodeId) {
    final String _sql = "SELECT * FROM soldiers WHERE node_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, nodeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"soldiers"}, new Callable<SoldierEntity>() {
      @Override
      @Nullable
      public SoldierEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "node_id");
          final int _cursorIndexOfTeamName = CursorUtil.getColumnIndexOrThrow(_cursor, "team_name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfHeading = CursorUtil.getColumnIndexOrThrow(_cursor, "heading");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heart_rate");
          final int _cursorIndexOfSpo2 = CursorUtil.getColumnIndexOrThrow(_cursor, "spo2");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfBatteryVolts = CursorUtil.getColumnIndexOrThrow(_cursor, "battery_volts");
          final int _cursorIndexOfStatusFlags = CursorUtil.getColumnIndexOrThrow(_cursor, "status_flags");
          final int _cursorIndexOfAlertLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_level");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfLastSeenMs = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen_ms");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final SoldierEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpNodeId;
            _tmpNodeId = _cursor.getInt(_cursorIndexOfNodeId);
            final String _tmpTeamName;
            if (_cursor.isNull(_cursorIndexOfTeamName)) {
              _tmpTeamName = null;
            } else {
              _tmpTeamName = _cursor.getString(_cursorIndexOfTeamName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpHeading;
            _tmpHeading = _cursor.getDouble(_cursorIndexOfHeading);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpSpo2;
            _tmpSpo2 = _cursor.getInt(_cursorIndexOfSpo2);
            final double _tmpTemperature;
            _tmpTemperature = _cursor.getDouble(_cursorIndexOfTemperature);
            final double _tmpHumidity;
            _tmpHumidity = _cursor.getDouble(_cursorIndexOfHumidity);
            final double _tmpPressure;
            _tmpPressure = _cursor.getDouble(_cursorIndexOfPressure);
            final double _tmpBatteryVolts;
            _tmpBatteryVolts = _cursor.getDouble(_cursorIndexOfBatteryVolts);
            final int _tmpStatusFlags;
            _tmpStatusFlags = _cursor.getInt(_cursorIndexOfStatusFlags);
            final int _tmpAlertLevel;
            _tmpAlertLevel = _cursor.getInt(_cursorIndexOfAlertLevel);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final long _tmpLastSeenMs;
            _tmpLastSeenMs = _cursor.getLong(_cursorIndexOfLastSeenMs);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new SoldierEntity(_tmpNodeId,_tmpTeamName,_tmpLatitude,_tmpLongitude,_tmpHeading,_tmpHeartRate,_tmpSpo2,_tmpTemperature,_tmpHumidity,_tmpPressure,_tmpBatteryVolts,_tmpStatusFlags,_tmpAlertLevel,_tmpIsOnline,_tmpLastSeenMs,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SoldierEntity>> getOnlineSoldiers() {
    final String _sql = "SELECT * FROM soldiers WHERE is_online = 1 ORDER BY node_id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"soldiers"}, new Callable<List<SoldierEntity>>() {
      @Override
      @NonNull
      public List<SoldierEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "node_id");
          final int _cursorIndexOfTeamName = CursorUtil.getColumnIndexOrThrow(_cursor, "team_name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfHeading = CursorUtil.getColumnIndexOrThrow(_cursor, "heading");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heart_rate");
          final int _cursorIndexOfSpo2 = CursorUtil.getColumnIndexOrThrow(_cursor, "spo2");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfBatteryVolts = CursorUtil.getColumnIndexOrThrow(_cursor, "battery_volts");
          final int _cursorIndexOfStatusFlags = CursorUtil.getColumnIndexOrThrow(_cursor, "status_flags");
          final int _cursorIndexOfAlertLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_level");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfLastSeenMs = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen_ms");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<SoldierEntity> _result = new ArrayList<SoldierEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SoldierEntity _item;
            final int _tmpNodeId;
            _tmpNodeId = _cursor.getInt(_cursorIndexOfNodeId);
            final String _tmpTeamName;
            if (_cursor.isNull(_cursorIndexOfTeamName)) {
              _tmpTeamName = null;
            } else {
              _tmpTeamName = _cursor.getString(_cursorIndexOfTeamName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpHeading;
            _tmpHeading = _cursor.getDouble(_cursorIndexOfHeading);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpSpo2;
            _tmpSpo2 = _cursor.getInt(_cursorIndexOfSpo2);
            final double _tmpTemperature;
            _tmpTemperature = _cursor.getDouble(_cursorIndexOfTemperature);
            final double _tmpHumidity;
            _tmpHumidity = _cursor.getDouble(_cursorIndexOfHumidity);
            final double _tmpPressure;
            _tmpPressure = _cursor.getDouble(_cursorIndexOfPressure);
            final double _tmpBatteryVolts;
            _tmpBatteryVolts = _cursor.getDouble(_cursorIndexOfBatteryVolts);
            final int _tmpStatusFlags;
            _tmpStatusFlags = _cursor.getInt(_cursorIndexOfStatusFlags);
            final int _tmpAlertLevel;
            _tmpAlertLevel = _cursor.getInt(_cursorIndexOfAlertLevel);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final long _tmpLastSeenMs;
            _tmpLastSeenMs = _cursor.getLong(_cursorIndexOfLastSeenMs);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new SoldierEntity(_tmpNodeId,_tmpTeamName,_tmpLatitude,_tmpLongitude,_tmpHeading,_tmpHeartRate,_tmpSpo2,_tmpTemperature,_tmpHumidity,_tmpPressure,_tmpBatteryVolts,_tmpStatusFlags,_tmpAlertLevel,_tmpIsOnline,_tmpLastSeenMs,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getOnlineCount() {
    final String _sql = "SELECT COUNT(*) FROM soldiers WHERE is_online = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"soldiers"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SoldierEntity>> getCriticalSoldiers() {
    final String _sql = "SELECT * FROM soldiers WHERE alert_level >= 2 AND is_online = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"soldiers"}, new Callable<List<SoldierEntity>>() {
      @Override
      @NonNull
      public List<SoldierEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "node_id");
          final int _cursorIndexOfTeamName = CursorUtil.getColumnIndexOrThrow(_cursor, "team_name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfHeading = CursorUtil.getColumnIndexOrThrow(_cursor, "heading");
          final int _cursorIndexOfHeartRate = CursorUtil.getColumnIndexOrThrow(_cursor, "heart_rate");
          final int _cursorIndexOfSpo2 = CursorUtil.getColumnIndexOrThrow(_cursor, "spo2");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "humidity");
          final int _cursorIndexOfPressure = CursorUtil.getColumnIndexOrThrow(_cursor, "pressure");
          final int _cursorIndexOfBatteryVolts = CursorUtil.getColumnIndexOrThrow(_cursor, "battery_volts");
          final int _cursorIndexOfStatusFlags = CursorUtil.getColumnIndexOrThrow(_cursor, "status_flags");
          final int _cursorIndexOfAlertLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "alert_level");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfLastSeenMs = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen_ms");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<SoldierEntity> _result = new ArrayList<SoldierEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SoldierEntity _item;
            final int _tmpNodeId;
            _tmpNodeId = _cursor.getInt(_cursorIndexOfNodeId);
            final String _tmpTeamName;
            if (_cursor.isNull(_cursorIndexOfTeamName)) {
              _tmpTeamName = null;
            } else {
              _tmpTeamName = _cursor.getString(_cursorIndexOfTeamName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpHeading;
            _tmpHeading = _cursor.getDouble(_cursorIndexOfHeading);
            final int _tmpHeartRate;
            _tmpHeartRate = _cursor.getInt(_cursorIndexOfHeartRate);
            final int _tmpSpo2;
            _tmpSpo2 = _cursor.getInt(_cursorIndexOfSpo2);
            final double _tmpTemperature;
            _tmpTemperature = _cursor.getDouble(_cursorIndexOfTemperature);
            final double _tmpHumidity;
            _tmpHumidity = _cursor.getDouble(_cursorIndexOfHumidity);
            final double _tmpPressure;
            _tmpPressure = _cursor.getDouble(_cursorIndexOfPressure);
            final double _tmpBatteryVolts;
            _tmpBatteryVolts = _cursor.getDouble(_cursorIndexOfBatteryVolts);
            final int _tmpStatusFlags;
            _tmpStatusFlags = _cursor.getInt(_cursorIndexOfStatusFlags);
            final int _tmpAlertLevel;
            _tmpAlertLevel = _cursor.getInt(_cursorIndexOfAlertLevel);
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final long _tmpLastSeenMs;
            _tmpLastSeenMs = _cursor.getLong(_cursorIndexOfLastSeenMs);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new SoldierEntity(_tmpNodeId,_tmpTeamName,_tmpLatitude,_tmpLongitude,_tmpHeading,_tmpHeartRate,_tmpSpo2,_tmpTemperature,_tmpHumidity,_tmpPressure,_tmpBatteryVolts,_tmpStatusFlags,_tmpAlertLevel,_tmpIsOnline,_tmpLastSeenMs,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
