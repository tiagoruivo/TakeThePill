package com.android.takethepill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the pills management, and gives the ability to list all pills as well as
 * retrieve or modify a specific pill.
 */
public class PillsDbAdapter {

	//BBDD fields
	public static final String KEY_USER = "user";
	public static final String KEY_PILL = "pill";
	public static final String KEY_DAYS = "days";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_ROWID = "_id"; 
	public static final String KEY_ALARMS = "alarms"; 
	

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	private static final int DATABASE_VERSION = 2;
	private static final String TAG = "PillsDbAdapter";
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "pills";

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table "+DATABASE_TABLE+" ("
			+KEY_ROWID+" integer primary key autoincrement, "
			+KEY_USER +" text not null, "
			+KEY_PILL +" text not null, "
			+KEY_DAYS +" text not null, "
			+KEY_HOUR +" text not null, "
			+KEY_ALARMS +" integer not null);";	

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public PillsDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the pills database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public PillsDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new pill using the user, pill, days and hours provided. If the pill is
	 * successfully created return the new rowId for that pill, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param user the user of the pill
	 * @param pill the name of the pill
	 * @param days the days of the pill
	 * @param hour the hours of the pill
	 * @param alarms the number of alarms
	 * @return rowId or -1 if failed
	 */
	public long createPill(String user, String pill, String days, String hour, int alarms) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_USER, user);
		initialValues.put(KEY_PILL, pill);
		initialValues.put(KEY_DAYS, days);
		initialValues.put(KEY_HOUR, hour);
		initialValues.put(KEY_ALARMS, alarms);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the pill with the given rowId
	 * 
	 * @param rowId id of pill to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deletePill(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all pills in the database
	 * 
	 * @return Cursor over all pills
	 */
	public Cursor fetchAllPills() {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_USER,
				KEY_PILL, KEY_DAYS, KEY_HOUR, KEY_ALARMS}, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the pill that matches the given rowId
	 * 
	 * @param rowId id of pill to retrieve
	 * @return Cursor positioned to matching pill, if found
	 * @throws SQLException if pill could not be found/retrieved
	 */
	public Cursor fetchPill(long rowId) throws SQLException {

		Cursor mCursor = mDb.query(
				true, DATABASE_TABLE, new String[] {
						KEY_ROWID,
						KEY_USER,
						KEY_PILL, 
						KEY_DAYS, 
						KEY_HOUR, 
						KEY_ALARMS
				}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the pill using the details provided. The pill to be updated is
	 * specified using the rowId, and it is altered to use the user, 
	 * pill, hours and days values passed in
	 * 
	 * @param user the user of the pill
	 * @param pill the name of the pill
	 * @param days the days of the pill
	 * @param hour the hours of the pill
	 * @param alarms the number of alarms
	 * @return true if the pill was successfully updated, false otherwise
	 */
	public boolean updatePill(long rowId, String user, String pill, String days, String hour, int alarms) {
		ContentValues args = new ContentValues();
		args.put(KEY_USER, user);
		args.put(KEY_PILL, pill);
		args.put(KEY_DAYS, days);
		args.put(KEY_HOUR, hour);
		args.put(KEY_ALARMS, alarms);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
