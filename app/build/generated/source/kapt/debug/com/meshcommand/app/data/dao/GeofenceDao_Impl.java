package com.meshcommand.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.meshcommand.app.data.entity.GeofenceEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class GeofenceDao_Impl implements GeofenceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GeofenceEntity> __insertionAdapterOfGeofenceEntity;

  private final EntityDeletionOrUpdateAdapter<GeofenceEntity> __deletionAdapterOfGeofenceEntity;

  private final EntityDeletionOrUpdateAdapter<GeofenceEntity> __updateAdapterOfGeofenceEntity;

  public GeofenceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGeofenceEntity = new EntityInsertionAdapter<GeofenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `geofences` (`id`,`name`,`zone_type`,`points_json`,`is_active`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GeofenceEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getZoneType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getZoneType());
        }
        if (entity.getPointsJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPointsJson());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfGeofenceEntity = new EntityDeletionOrUpdateAdapter<GeofenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `geofences` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GeofenceEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfGeofenceEntity = new EntityDeletionOrUpdateAdapter<GeofenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `geofences` SET `id` = ?,`name` = ?,`zone_type` = ?,`points_json` = ?,`is_active` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GeofenceEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getZoneType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getZoneType());
        }
        if (entity.getPointsJson() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPointsJson());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final GeofenceEntity geofence,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfGeofenceEntity.insertAndReturnId(geofence);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final GeofenceEntity geofence,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfGeofenceEntity.handle(geofence);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final GeofenceEntity geofence,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfGeofenceEntity.handle(geofence);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<GeofenceEntity>> getAll() {
    final String _sql = "SELECT * FROM geofences ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"geofences"}, new Callable<List<GeofenceEntity>>() {
      @Override
      @NonNull
      public List<GeofenceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zone_type");
          final int _cursorIndexOfPointsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "points_json");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<GeofenceEntity> _result = new ArrayList<GeofenceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GeofenceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpZoneType;
            if (_cursor.isNull(_cursorIndexOfZoneType)) {
              _tmpZoneType = null;
            } else {
              _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            }
            final String _tmpPointsJson;
            if (_cursor.isNull(_cursorIndexOfPointsJson)) {
              _tmpPointsJson = null;
            } else {
              _tmpPointsJson = _cursor.getString(_cursorIndexOfPointsJson);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new GeofenceEntity(_tmpId,_tmpName,_tmpZoneType,_tmpPointsJson,_tmpIsActive,_tmpCreatedAt);
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
  public Flow<List<GeofenceEntity>> getActiveGeofences() {
    final String _sql = "SELECT * FROM geofences WHERE is_active = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"geofences"}, new Callable<List<GeofenceEntity>>() {
      @Override
      @NonNull
      public List<GeofenceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zone_type");
          final int _cursorIndexOfPointsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "points_json");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<GeofenceEntity> _result = new ArrayList<GeofenceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GeofenceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpZoneType;
            if (_cursor.isNull(_cursorIndexOfZoneType)) {
              _tmpZoneType = null;
            } else {
              _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            }
            final String _tmpPointsJson;
            if (_cursor.isNull(_cursorIndexOfPointsJson)) {
              _tmpPointsJson = null;
            } else {
              _tmpPointsJson = _cursor.getString(_cursorIndexOfPointsJson);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new GeofenceEntity(_tmpId,_tmpName,_tmpZoneType,_tmpPointsJson,_tmpIsActive,_tmpCreatedAt);
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
  public Object getById(final long geofenceId,
      final Continuation<? super GeofenceEntity> $completion) {
    final String _sql = "SELECT * FROM geofences WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, geofenceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GeofenceEntity>() {
      @Override
      @Nullable
      public GeofenceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfZoneType = CursorUtil.getColumnIndexOrThrow(_cursor, "zone_type");
          final int _cursorIndexOfPointsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "points_json");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final GeofenceEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpZoneType;
            if (_cursor.isNull(_cursorIndexOfZoneType)) {
              _tmpZoneType = null;
            } else {
              _tmpZoneType = _cursor.getString(_cursorIndexOfZoneType);
            }
            final String _tmpPointsJson;
            if (_cursor.isNull(_cursorIndexOfPointsJson)) {
              _tmpPointsJson = null;
            } else {
              _tmpPointsJson = _cursor.getString(_cursorIndexOfPointsJson);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new GeofenceEntity(_tmpId,_tmpName,_tmpZoneType,_tmpPointsJson,_tmpIsActive,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
