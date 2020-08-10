/*
 * SQLiteOpenHelperWrapper.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

package presto.privaid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static presto.privaid.Utils.TAG;

public class SQLiteOpenHelperWrapper extends SQLiteOpenHelper {
  protected final Context appContext;
  protected final Timer timer = new Timer();
  protected final String DB_NAME;
  protected final String CREATE_EVENTS_TABLE_SQL =
      String.format(
          "CREATE TABLE IF NOT EXISTS %s ( "
              + "'%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
              + "'%s' TEXT NOT NULL,"
              + "'%s' TEXT NOT NULL);",
          "events", "event_id", "name", "params");

  protected final String CREATE_RANDOM_EVENTS_TABLE_SQL =
      String.format(
          "CREATE TABLE IF NOT EXISTS %s ( "
              + "'%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
              + "'%s' TEXT NOT NULL,"
              + "'%s' TEXT NOT NULL);",
          "random_events", "event_id", "name", "params");

  protected final String CREATE_PROCESSED_EVENTS_TABLE_SQL =
      String.format(
          "CREATE TABLE IF NOT EXISTS %s ( "
              + "'%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
              + "'%s' TEXT NOT NULL,"
              + "'%s' TEXT NOT NULL);",
          "processed_events", "event_id", "name", "params");

  public SQLiteOpenHelperWrapper(Context context, String name) {
    super(context, name, null, 1);
    this.appContext = context;
    this.DB_NAME = name;
  }

  @Override
  public final SQLiteDatabase getWritableDatabase() {
    if (!timer.checkTimeLimit(60000)) { // wait for 1 min
      throw new SQLiteException("Database open failed");
    }
    try {
      return super.getWritableDatabase();
    } catch (SQLiteException e) {
      timer.start();
      Log.w(TAG, "Opening the database failed, dropping the table and recreating it " + e);
      appContext.getDatabasePath(DB_NAME).delete();
      try {
        SQLiteDatabase sQLiteDatabase = super.getWritableDatabase();
        timer.clear();
        return sQLiteDatabase;
      } catch (SQLiteException e2) {
        Log.e(TAG, "Failed to open freshly created database " + e2);
        throw e2;
      }
    }
  }

  private boolean checkIfExistTable(SQLiteDatabase sQLiteDatabase, String tbl) {
    Cursor cursor =
        sQLiteDatabase.query(
            "SQLITE_MASTER", new String[] {"name"}, "name=?", new String[] {tbl}, null, null, null);
    boolean res = cursor.moveToFirst();
    cursor.close();
    return res;
  }

  private static Set<String> getColumnNamesOfTable(SQLiteDatabase sQLiteDatabase, String tbl) {
    String sql = String.format("SELECT * FROM %s LIMIT 0", tbl);
    Cursor cursor = sQLiteDatabase.rawQuery(sql, null);
    String[] names = cursor.getColumnNames();
    cursor.close();
    return new HashSet<>(Arrays.asList(names));
  }

  @Override
  public void onOpen(SQLiteDatabase sQLiteDatabase) {
    if (!this.checkIfExistTable(sQLiteDatabase, "events")) {
      sQLiteDatabase.execSQL(CREATE_EVENTS_TABLE_SQL);
    } else {
      Set<String> columnNames =
          SQLiteOpenHelperWrapper.getColumnNamesOfTable(sQLiteDatabase, "events");
      String[] COLUMN_NAMES = new String[] {"event_id", "name", "params"};
      for (int i = 0; i < 3; ++i) {
        String name = COLUMN_NAMES[i];
        if (columnNames.remove(name)) continue;
        throw new SQLiteException("Database events is missing required column: " + name);
      }
    }

    if (!this.checkIfExistTable(sQLiteDatabase, "random_events")) {
      sQLiteDatabase.execSQL(CREATE_RANDOM_EVENTS_TABLE_SQL);
    } else {
      Set<String> columnNames =
          SQLiteOpenHelperWrapper.getColumnNamesOfTable(sQLiteDatabase, "random_events");
      String[] COLUMN_NAMES = new String[] {"event_id", "name", "params"};
      for (int i = 0; i < 3; ++i) {
        String name = COLUMN_NAMES[i];
        if (columnNames.remove(name)) continue;
        throw new SQLiteException("Database random_events is missing required column: " + name);
      }
    }

    if (!this.checkIfExistTable(sQLiteDatabase, "processed_events")) {
      sQLiteDatabase.execSQL(CREATE_PROCESSED_EVENTS_TABLE_SQL);
    } else {
      Set<String> columnNames =
          SQLiteOpenHelperWrapper.getColumnNamesOfTable(sQLiteDatabase, "processed_events");
      String[] COLUMN_NAMES = new String[] {"event_id", "name", "params"};
      for (int i = 0; i < 3; ++i) {
        String name = COLUMN_NAMES[i];
        if (columnNames.remove(name)) continue;
        throw new SQLiteException("Database processed_events is missing required column: " + name);
      }
    }
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    File file = new File(sqLiteDatabase.getPath());
    file.setReadable(false, false);
    file.setWritable(false, false);
    file.setReadable(true, true);
    file.setWritable(true, true);
    Log.v(TAG, "Opening database at " + file.getAbsolutePath());
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
