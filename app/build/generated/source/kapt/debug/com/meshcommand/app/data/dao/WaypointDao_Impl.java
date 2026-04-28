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
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.meshcommand.app.data.entity.WaypointEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class WaypointDao_Impl implements WaypointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WaypointEntity> __insertionAdapterOfWaypointEntity;

  private final EntityDeletionOrUpdateAdapter<WaypointEntity> __deletionAdapterOfWaypointEntity;

  private final EntityDeletionOrUpdateAdapter<WaypointEntity> __updateAdapterOfWaypointEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public WaypointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWaypointEntity = new EntityInsertionAdapter<WaypointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `waypoints` (`id`,`name`,`latitude`,`longitude`,`icon_type`,`assigned_node_id`,`description`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaypointEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        if (entity.getIconType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getIconType());
        }
        if (entity.getAssignedNodeId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getAssignedNodeId());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfWaypointEntity = new EntityDeletionOrUpdateAdapter<WaypointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `waypoints` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaypointEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfWaypointEntity = new EntityDeletionOrUpdateAdapter<WaypointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `waypoints` SET `id` = ?,`name` = ?,`latitude` = ?,`longitude` = ?,`icon_type` = ?,`assigned_node_id` = ?,`description` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WaypointEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        if (entity.getIconType() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getIconType());
        }
        if (entity.getAssignedNodeId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getAssignedNodeId());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM waypoints WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final WaypointEntity waypoint,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfWaypointEntity.insertAndReturnId(waypoint);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final WaypointEntity waypoint,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWaypointEntity.handle(waypoint);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final WaypointEntity waypoint,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfWaypointEntity.handle(waypoint);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long waypointId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, waypointId);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WaypointEntity>> getAll() {
    final String _sql = "SELECT * FROM waypoints ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"waypoints"}, new Callable<List<WaypointEntity>>() {
      @Override
      @NonNull
      public List<WaypointEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "icon_type");
          final int _cursorIndexOfAssignedNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "assigned_node_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<WaypointEntity> _result = new ArrayList<WaypointEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WaypointEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpIconType;
            if (_cursor.isNull(_cursorIndexOfIconType)) {
              _tmpIconType = null;
            } else {
              _tmpIconType = _cursor.getString(_cursorIndexOfIconType);
            }
            final Integer _tmpAssignedNodeId;
            if (_cursor.isNull(_cursorIndexOfAssignedNodeId)) {
              _tmpAssignedNodeId = null;
            } else {
              _tmpAssignedNodeId = _cursor.getInt(_cursorIndexOfAssignedNodeId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WaypointEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIconType,_tmpAssignedNodeId,_tmpDescription,_tmpCreatedAt);
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
  public Flow<List<WaypointEntity>> getForNode(final int nodeId) {
    final String _sql = "SELECT * FROM waypoints WHERE assigned_node_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, nodeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"waypoints"}, new Callable<List<WaypointEntity>>() {
      @Override
      @NonNull
      public List<WaypointEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "icon_type");
          final int _cursorIndexOfAssignedNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "assigned_node_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<WaypointEntity> _result = new ArrayList<WaypointEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WaypointEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpIconType;
            if (_cursor.isNull(_cursorIndexOfIconType)) {
              _tmpIconType = null;
            } else {
              _tmpIconType = _cursor.getString(_cursorIndexOfIconType);
            }
            final Integer _tmpAssignedNodeId;
            if (_cursor.isNull(_cursorIndexOfAssignedNodeId)) {
              _tmpAssignedNodeId = null;
            } else {
              _tmpAssignedNodeId = _cursor.getInt(_cursorIndexOfAssignedNodeId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WaypointEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIconType,_tmpAssignedNodeId,_tmpDescription,_tmpCreatedAt);
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
  public Object getById(final long waypointId,
      final Continuation<? super WaypointEntity> $completion) {
    final String _sql = "SELECT * FROM waypoints WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, waypointId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WaypointEntity>() {
      @Override
      @Nullable
      public WaypointEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "icon_type");
          final int _cursorIndexOfAssignedNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "assigned_node_id");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final WaypointEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpIconType;
            if (_cursor.isNull(_cursorIndexOfIconType)) {
              _tmpIconType = null;
            } else {
              _tmpIconType = _cursor.getString(_cursorIndexOfIconType);
            }
            final Integer _tmpAssignedNodeId;
            if (_cursor.isNull(_cursorIndexOfAssignedNodeId)) {
              _tmpAssignedNodeId = null;
            } else {
              _tmpAssignedNodeId = _cursor.getInt(_cursorIndexOfAssignedNodeId);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new WaypointEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpIconType,_tmpAssignedNodeId,_tmpDescription,_tmpCreatedAt);
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
