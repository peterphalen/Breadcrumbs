package com.detimil.breadcrumbs1;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.detimil.breadcrumbs1.DatabaseHandler;

public class CrumbContentProvider extends ContentProvider {

  // database
  private DatabaseHandler database;

  // used for the UriMacher
  public static final int BREADCRUMBS = 10;
  public static final int BREADCRUMB_ID = 20;

  private static final String AUTHORITY = "com.detimil.breadcrumbs1.contentprovider";

  private static final String BASE_PATH = "breadcrumbs";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
      + "/" + BASE_PATH);

  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/breadcrumbs";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/breadcrumb";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, BREADCRUMBS);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BREADCRUMB_ID);
  }

  @Override
  public boolean onCreate() {
    database = new DatabaseHandler(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {

    // Uisng SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // check if the caller has requested a column which does not exists
    checkColumns(projection);

    // Set the table
    queryBuilder.setTables(DatabaseHandler.TABLE_BREADCRUMBS);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case BREADCRUMBS:
      break;
    case BREADCRUMB_ID:
      // adding the ID to the original query
      queryBuilder.appendWhere(DatabaseHandler._id + "="
          + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    // make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    long id = 0;
    switch (uriType) {
    case BREADCRUMBS:
      id = sqlDB.insert(DatabaseHandler.TABLE_BREADCRUMBS, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsDeleted = 0;
    switch (uriType) {
    case BREADCRUMBS:
      rowsDeleted = sqlDB.delete(DatabaseHandler.TABLE_BREADCRUMBS, selection,
          selectionArgs);
      break;
    case BREADCRUMB_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsDeleted = sqlDB.delete(DatabaseHandler.TABLE_BREADCRUMBS,
        		DatabaseHandler._id + "=" + id, 
            null);
      } else {
        rowsDeleted = sqlDB.delete(DatabaseHandler.TABLE_BREADCRUMBS,
        		DatabaseHandler._id + "=" + id 
            + " and " + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.getWritableDatabase();
    int rowsUpdated = 0;
    switch (uriType) {
    case BREADCRUMBS:
      rowsUpdated = sqlDB.update(DatabaseHandler.TABLE_BREADCRUMBS, 
          values, 
          selection,
          selectionArgs);
      break;
    case BREADCRUMB_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = sqlDB.update(DatabaseHandler.TABLE_BREADCRUMBS, 
            values,
            DatabaseHandler._id + "=" + id, 
            null);
      } else {
        rowsUpdated = sqlDB.update(DatabaseHandler.TABLE_BREADCRUMBS, 
            values,
            DatabaseHandler._id + "=" + id 
            + " and " 
            + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

  private void checkColumns(String[] projection) {
    String[] available = { DatabaseHandler.KEY_LATITUDE,
    		DatabaseHandler.KEY_LONGITUDE, DatabaseHandler.KEY_LABEL,
    		DatabaseHandler._id };
    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
      // check if all columns which are requested are available
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException("Unknown columns in projection");
      }
    }
  }

} 