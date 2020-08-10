package presto.privaid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static presto.privaid.Utils.TAG;

public class DatabaseController implements Closeable {
  private final SQLiteOpenHelperWrapper mSqLiteOpenHelperWrapper;
  private final String DB_NAME = "presto_events.db";
  private final Context mAppContext;
  private static DatabaseController instance;

  private DatabaseController(Context context) {
    this.mSqLiteOpenHelperWrapper = new SQLiteOpenHelperWrapper(context, DB_NAME);
    this.mAppContext = context;
  }

  public static DatabaseController getInstance(Context context) {
    if (instance == null) {
      synchronized (DatabaseController.class) {
        if (instance == null) {
          instance = new DatabaseController(context);
        }
      }
    }
    return instance;
  }

  public static DatabaseController getInstance() {
    if (instance == null) {
      synchronized (DatabaseController.class) {
        if (instance == null) {
          throw new RuntimeException("DatabaseController not initialized.");
        }
      }
    }
    return instance;
  }

  @Override
  public void close() throws IOException {
    try {
      this.mSqLiteOpenHelperWrapper.close();
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Sql error closing database: " + sQLiteException);
    } catch (IllegalStateException illegalStateException) {
      Log.e(TAG, "Error closing database: " + illegalStateException);
    }
  }

  private SQLiteDatabase getWritableDatabase() {
    try {
      return this.mSqLiteOpenHelperWrapper.getWritableDatabase();
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Error opening database: " + sQLiteException);
      throw sQLiteException;
    }
  }

  public final void beginTransaction() {
    this.getWritableDatabase().beginTransaction();
  }

  public final void setTransactionSuccessful() {
    this.getWritableDatabase().setTransactionSuccessful();
  }

  public final void endTransaction() {
    this.getWritableDatabase().endTransaction();
  }

  private long executeRawQuery(String query, String[] args) {
    Cursor cursor = null;
    try {
      cursor = this.getWritableDatabase().rawQuery(query, args);
      if (cursor.moveToFirst()) return cursor.getLong(0);
      return 0;
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Database error " + query + " " + sQLiteException);
      throw sQLiteException;
    }/* finally {
      if (cursor != null && !cursor.isClosed()) cursor.close();
    }*/
  }

  long numberOfStoredEvents() {
    Work.checkIfInWorkThread();
    return this.executeRawQuery("SELECT COUNT(*) FROM events", null);
  }

  long numberOfStoredRandomizedEvents() {
    Work.checkIfInWorkThread();
    return this.executeRawQuery("SELECT COUNT(*) FROM random_events", null);
  }

  private long storeEvent(String name, Bundle bundle, String tbl) {
    Work.checkIfInWorkThread();
    ContentValues contentValues = new ContentValues();
    String params = Utils.bundleToJsonString(bundle);
    contentValues.put("name", name);
    contentValues.put("params", params);
    try {
      long id = this.getWritableDatabase().insert(tbl, null, contentValues);
      if (id == -1L) {
        Log.e(TAG, "Failed to insert a event (got -1) to " + tbl);
        return -1;
      }
      Log.v(TAG, "Event saved to " + tbl + ". db-id=" + id + ", name=" + name + ", params=" + params);
      return id;
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Error storing a event to " + tbl + " " + sQLiteException);
      throw sQLiteException;
    }
  }

  final long storeEvent(String name, Bundle bundle) {
    return storeEvent(name, bundle, "events");
  }

  final long storeRandomizedEvent(String name, Bundle bundle) {
    return storeEvent(name, bundle, "random_events");
  }

  final long storeProcessedEvent(String name, Bundle bundle) {
    return storeEvent(name, bundle, "processed_events");
  }

  private void deleteEvents(List<Long> idList, String tbl) {
    Work.checkIfInWorkThread();
    if (idList.isEmpty()) {
      return;
    }
    StringBuilder stringBuilder = new StringBuilder("event_id");
    stringBuilder.append(" in (");
    Long l;
    for (int i = 0; i < idList.size(); ++i) {
      l = idList.get(i);
      if (l == null || l == 0L) {
        Log.w(TAG, "Invalid event in " + tbl + " id=" + l);
      }
      if (i > 0) {
        stringBuilder.append(",");
      }
      stringBuilder.append(l);
    }
    stringBuilder.append(")");
    String string = stringBuilder.toString();
    try {
      int n = this.getWritableDatabase().delete(tbl, string, null);
      if (n != idList.size()) {
        Log.v(
            TAG,
            "Deleted fewer events then expected from "
                + tbl
                + " "
                + idList.size()
                + " "
                + n
                + " "
                + string);
      }
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Error deleting events in " + tbl + " " + sQLiteException);
      throw sQLiteException;
    }
  }

  private void deleteEvent(long id, String tbl) {
    Work.checkIfInWorkThread();
    List<Long> arrayList = new ArrayList<>(1);
    arrayList.add(id);
    Log.v(TAG, "Deleting event in " + tbl + ", id=" + id);
    deleteEvents(arrayList, tbl);
  }

  public final void deleteEvent(long id) {
    deleteEvent(id, "events");
  }

  public final void deleteRandomizedEvent(long id) {
    deleteEvent(id, "random_events");
  }

  public final void deleteProcessedEvent(long id) {
    deleteEvent(id, "processed_events");
  }

  private List<EventInfo> readEvents(long limit, String tbl) {
    Work.checkIfInWorkThread();
    Cursor cursor = null;
    try {
      cursor =
          this.getWritableDatabase()
              .query(
                  tbl,
                  new String[] {"event_id", "name", "params"},
                  null,
                  null,
                  null,
                  null,
                  String.format("%s ASC", "event_id"));
      List<EventInfo> events = new ArrayList<>();
      if (cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String name = cursor.getString(1);
          String params = cursor.getString(2);
          Bundle bundle = Utils.jsonStringToBundle(params);
          events.add(new EventInfo(id, name, bundle));
        } while (cursor.moveToNext());
      }
      cursor.close();
      Log.v(TAG, "Events read from " + tbl + ": " + events.size());
      return events;
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Error loading Events from " + tbl + " " + sQLiteException);
      throw sQLiteException;
    } finally {
      if (cursor != null && !cursor.isClosed()) cursor.close();
    }
  }

  public final List<EventInfo> readEvents(long limit) {
    return readEvents(limit, "events");
  }

  public final List<EventInfo> readRandomizedEvents(long limit) {
    return readEvents(limit, "random_events");
  }

  public final List<EventInfo> readProcessedEvents(long limit) {
    return readEvents(limit, "processed_events");
  }

  public final void createContentTable(String name) {
    String CREATE_DICT_TABLE = String.format(
            "CREATE TABLE IF NOT EXISTS %s ( "
                    + "'%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "'%s' TEXT NOT NULL);",
            name, "id", "item");
    executeRawQuery(CREATE_DICT_TABLE, null);
  }

  public long addContent(String name, Bundle bundle) {
    ContentValues contentValues = new ContentValues();
    String params = Utils.bundleToJsonString(bundle);
    contentValues.put("item", params);
    try {
      long id = this.getWritableDatabase().insert(name, null, contentValues);
      if (id == -1L) {
        Log.e(TAG, "Failed to insert a event (got -1) to " + name);
        return -1;
      }
      Log.v(TAG, "Element saved to " + name + ". db-id=" + id + ", item=" + params);
      return id;
    } catch (SQLiteException sQLiteException) {
      Log.e(TAG, "Error storing a event to " + name + " " + sQLiteException);
      throw sQLiteException;
    }
  }

  public long numberOfDictionaryElements(String name) {
    return this.executeRawQuery("SELECT COUNT(*) FROM " + name, null);
  }
}
