package com.peter.breadcrumbs;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    public static final int DATABASE_VERSION = 1;
 
    // Database Name
    public static final String DATABASE_NAME = "breadcrumbsManager";
 
    // Contacts table name
    public static final String TABLE_BREADCRUMBS = "breadcrumbs";
 
    // Contacts Table Columns names
    public static final String _id = "_id";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LABEL = "label";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BREADCRUMBS_TABLE = "CREATE TABLE " + TABLE_BREADCRUMBS + "("
                + _id + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT," + KEY_LABEL + " TEXT" + ")";
        db.execSQL(CREATE_BREADCRUMBS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BREADCRUMBS);
 
        // Create tables again
        onCreate(db);
    }
    
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    
    // Column numbers:
    //
    // 0 = ID
    // 1 = Latitude
    // 2 = Longitude
    // 3 = Label 
    //
    
    	// Adding new location/breadcrumb
    public void addBreadcrumb(Breadcrumb breadcrumb) {
    		SQLiteDatabase db = this.getWritableDatabase();
 
    		ContentValues values = new ContentValues();
    		values.put(KEY_LATITUDE, breadcrumb.getBreadcrumbLatitude()); // Latitude
    		values.put(KEY_LONGITUDE, breadcrumb.getBreadcrumbLongitude()); // Longitude
    		values.put(KEY_LABEL, breadcrumb.getLabel()); // label

 
    		// Inserting Row
    		db.insert(TABLE_BREADCRUMBS, null, values);
    		db.close(); // Closing database connection
    	}
   

   public void deleteAllBreadcrumbs()   {
	   
	 SQLiteDatabase db = this.getWritableDatabase();
       db.delete(TABLE_BREADCRUMBS, null, null);
       db.close();
   }
    
    public void relabelBreadcrumb(Integer id, String string) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
		ContentValues values = new ContentValues();
		values.put(KEY_LABEL, string); // put second arg into LABEL column
		db.update(TABLE_BREADCRUMBS, values, _id + "=" + id, null); // update specific row by id
		db.close();
    }

    	// Getting single breadcrumb
    public Breadcrumb getBreadcrumb(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();
 
    	Cursor cursor = db.query(TABLE_BREADCRUMBS, new String[] { _id,
    			KEY_LATITUDE, KEY_LONGITUDE, KEY_LABEL }, _id + "=?",
    			new String[] { String.valueOf(id) }, null, null, null, null);
    	if (cursor != null)
    	cursor.moveToLast();
 
    		Breadcrumb breadcrumb = new Breadcrumb(Integer.parseInt(cursor.getString(0)),
    		cursor.getInt(1), cursor.getInt(2), cursor.getString(3));
    	// return breadcrumb
    	cursor.close();
    	return breadcrumb;
    }
    
    
    
    	// Getting All locations
    public List<Breadcrumb> getAllBreadcrumbs() {
	 List<Breadcrumb> breadcrumbList = new ArrayList<Breadcrumb>();
    	// Select All Query
    	String selectQuery = "SELECT  * FROM " + TABLE_BREADCRUMBS;
 
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);
 
    	// looping through all rows and adding to list
    	if (cursor.moveToFirst()) {
        	do {
            	Breadcrumb breadcrumb = new Breadcrumb();
            	breadcrumb.setId(Integer.parseInt(cursor.getString(0)));
            	breadcrumb.setBreadcrumbLatitude(cursor.getInt(1));
            	breadcrumb.setBreadcrumbLongitude(cursor.getInt(2));
            	breadcrumb.setLabel(cursor.getString(3));
            	// Adding location to list
            	breadcrumbList.add(breadcrumb);
        	} while (cursor.moveToNext());
        	db.close();
    	}
 
    	// return locations list
    	cursor.close();
    	return breadcrumbList;
    }
    
    // Getting breadcrumbs/locations Count
    	public int getBreadcrumbsCount() {
    		int count;
        	String countQuery = "SELECT  * FROM " + TABLE_BREADCRUMBS;
        	SQLiteDatabase db = this.getReadableDatabase();
        	Cursor cursor = db.rawQuery(countQuery, null);
        	if ( cursor.getCount() > 0 ){
        		count = cursor.getCount();}
        	else
        	{count = 0;}
        	cursor.close();
 
        	// return count
        	return count;
    	}
    	
    	
    	  // Updating single location
    	public int updateBreadcrumbs(Breadcrumb breadcrumb) {
    	    SQLiteDatabase db = this.getWritableDatabase();
    	 
    	    ContentValues values = new ContentValues();
    	    values.put(KEY_LATITUDE, breadcrumb.getBreadcrumbLatitude());
    	    values.put(KEY_LONGITUDE, breadcrumb.getBreadcrumbLongitude());
    	    values.put(KEY_LABEL, breadcrumb.getLabel());

    	    // updating row
    	    return db.update(TABLE_BREADCRUMBS, values, _id + " = ?",
    	            new String[] { String.valueOf(breadcrumb.getId()) });
    	}
    	
    	// Deleting single location
    	  public void deleteBreadcrumb(Breadcrumb breadcrumb) {
      		SQLiteDatabase db = this.getWritableDatabase();
      		int id = breadcrumb.getId();
    		System.out.println("Comment deleted with id: " + id);
    		db.delete(TABLE_BREADCRUMBS, _id
    		        + " = " + id, null);
    		db.close();
    		  }
    	  
    	  //get all labels as strings
    	  
    	  public Cursor getAllLabels() {
        	 SQLiteDatabase db = this.getReadableDatabase();
    		  return db.rawQuery("SELECT " + _id + " ," + KEY_LABEL + " FROM " + TABLE_BREADCRUMBS, null);
    	  }
    	
}