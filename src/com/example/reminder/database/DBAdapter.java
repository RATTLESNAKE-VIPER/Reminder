package com.example.reminder.database;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.reminder.utils.AppConstants;

public class DBAdapter
{
	SQLiteDatabase mDb;
	Context mCtx;
	DataBaseHelper mDbHelper;


	public DBAdapter(Context context)
	{
		this.mCtx = context;
	}

	public DBAdapter open() throws SQLException
	{
		mDbHelper = new DataBaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{
		mDbHelper.close();
	}

	@SuppressWarnings("rawtypes")
	public long createTask(HashMap taskMap)
	{
		ContentValues initialValues = new ContentValues();

		int totalColumns = TaskTableSchema.ALL_COLUMNS.length;
		for (int i = 0; i < totalColumns; i++) {
			String columnName = TaskTableSchema.ALL_COLUMNS[i];

			if (!taskMap.containsKey(columnName) || columnName.equalsIgnoreCase(TaskTableSchema._ID)) {
				continue;
			}


			if (taskMap.get(columnName) instanceof String) {
				initialValues.put(columnName, taskMap.get(columnName).toString());

			} else if (taskMap.get(columnName) instanceof Integer) {
				initialValues.put(columnName, Integer.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Float) {
				initialValues.put(columnName, Float.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Long) {
				initialValues.put(columnName, Long.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Boolean) {
				initialValues.put(columnName, Boolean.valueOf(taskMap.get(columnName).toString()));

			} else {
				Log.d("", "columnName: " + columnName + ", value: " + taskMap.get(columnName));
			}
		}

		return mDb.insert(TaskTableSchema.TABLE_NAME, null, initialValues);
	}

	public boolean deleteTask(long id)
	{
		return mDb.delete(TaskTableSchema.TABLE_NAME, TaskTableSchema._ID + " = " + id, null) > 0;
	}


	@SuppressWarnings("rawtypes")
	public boolean updateTask(long id, HashMap taskMap)
	{
		ContentValues initialValues = new ContentValues();

		int totalColumns = TaskTableSchema.ALL_COLUMNS.length;
		for (int i = 0; i < totalColumns; i++) {
			String columnName = TaskTableSchema.ALL_COLUMNS[i];

			if (!taskMap.containsKey(columnName) || columnName.equalsIgnoreCase(TaskTableSchema._ID)) {
				continue;
			}

			if (taskMap.get(columnName) instanceof String) {
				initialValues.put(columnName, taskMap.get(columnName).toString());

			} else if (taskMap.get(columnName) instanceof Integer) {
				initialValues.put(columnName, Integer.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Float) {
				initialValues.put(columnName, Float.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Long) {
				initialValues.put(columnName, Long.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Boolean) {
				initialValues.put(columnName, Boolean.valueOf(taskMap.get(columnName).toString()));

			} else {
				Log.d("", "columnName: " + columnName + ", value: " + taskMap.get(columnName));
			}
		}
		Log.d("-----------", "id: " + id + ", taskMap: " + taskMap);
		return mDb.update(TaskTableSchema.TABLE_NAME, initialValues, TaskTableSchema._ID + " = " + id, null) > 0;
	}

	public Cursor fetchAllTasks()
	{
		return mDb.query(TaskTableSchema.TABLE_NAME, TaskTableSchema.ALL_COLUMNS, null, null, null, null, TaskTableSchema._ID + " DESC");
	}

	public Cursor fetchTask(long id)
	{
		Cursor c = mDb.query(TaskTableSchema.TABLE_NAME, TaskTableSchema.ALL_COLUMNS, TaskTableSchema._ID + " = " + id, null, null, null, null);
		if(c != null)
		{
			c.moveToFirst();
		}
		return c;
	}

	public Cursor fetchAllTaskForLocation() {
		String locationOnAndPendingClause = TaskTableSchema.KEY_LOCATION_ON + " = 1 and " + TaskTableSchema.KEY_TASK_COMPLETED + " = 0";
		String whereClause = locationOnAndPendingClause;
		return mDb.query(TaskTableSchema.TABLE_NAME, TaskTableSchema.ALL_COLUMNS, whereClause, null, null, null, null);
	}

	//Lists all of the dots that are near to the GeoPoint based on manhattan distance. Ordering is made by the DB Sqlite
	public Cursor fetchAllTaskForLocation(int currentLat, int currentLon, int maxRadius, int resultlimit) {
		//String locationOnAndPendingClause = TaskTableSchema.KEY_LOCATION_ON + " = 1 and " + TaskTableSchema.KEY_TASK_COMPLETED + " = 0";

		//=====================================================================================
		/*String rangeClause = TaskTableSchema.KEY_LATITUDE + " > (" + (currentLat + maxRadius) + ") AND "
				+ TaskTableSchema.KEY_LATITUDE + " < (" + (currentLat - maxRadius) +") AND "
				+ TaskTableSchema.KEY_LONGITUDE + " > ("+ (currentLon + maxRadius) +") AND "
				+ TaskTableSchema.KEY_LONGITUDE +" < ("+ (currentLon - maxRadius) +")";*/


		//=====================================================================================
		/*int lat_min = currentLat - maxRadius;
		int lat_max = currentLat + maxRadius;
		int lon_min = currentLon - maxRadius;
		int lon_max = currentLon + maxRadius;

		String rangeClause = TaskTableSchema.KEY_LATITUDE + " > " + lat_min + " AND " + TaskTableSchema.KEY_LATITUDE + " < " + lat_max
				+ " AND " + TaskTableSchema.KEY_LONGITUDE + " > " + lon_min + " AND " + TaskTableSchema.KEY_LONGITUDE + " < " + lon_max;

		String whereClause = locationOnAndPendingClause + " and " + rangeClause;
		Log.d("where: ", whereClause);
		return mDb.query(TaskTableSchema.TABLE_NAME, TaskTableSchema.ALL_COLUMNS, whereClause, null, null, null, null);*/


		//=====================================================================================

		//SELECT * FROM Locations WHERE lat BETWEEN %f AND %f AND lon BETWEEN %f AND %f ORDER BY distance(lat, lon, %f, %f) LIMIT 25 

		//=====================================================================================


		return mDb.rawQuery(
				"SELECT * FROM " + TaskTableSchema.TABLE_NAME 
				+ " WHERE " + TaskTableSchema.KEY_LOCATION_ON + " = 1 and " + TaskTableSchema.KEY_TASK_COMPLETED + " = 0 and " + TaskTableSchema.KEY_SNOOZE_ON + " != " + AppConstants.SNOOZE_ON
				+ " ORDER BY abs(" + TaskTableSchema.KEY_LATITUDE + " - (?)) + abs( " + TaskTableSchema.KEY_LONGITUDE + " - (?))"
				+ " LIMIT ?", new String[] { String.valueOf(currentLat), String.valueOf(currentLon), String.valueOf(resultlimit) });		
	}



	public Cursor fetchAllTaskForSnoozeType(Integer snoozeType) {
		String whereClause = TaskTableSchema.KEY_SNOOZE_ON + " = " + snoozeType;
		return mDb.query(TaskTableSchema.TABLE_NAME, TaskTableSchema.ALL_COLUMNS, whereClause, null, null, null, null);
	}

	public Cursor fetchTaskForIDS(ArrayList<String> list) {
		String[] ids = new String[list.size()];
		ids = list.toArray(ids);
		String query = "SELECT * FROM " + TaskTableSchema.TABLE_NAME + " WHERE " + TaskTableSchema._ID + " IN (" + makePlaceholders(ids.length) + ")";
		return mDb.rawQuery(query, ids);
	}

	private String makePlaceholders(int length) {
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			strBuilder.append("?, ");
			if (i == length-1) {
				strBuilder.append("?");
			}
		}
		return strBuilder.toString();
	}










	@SuppressWarnings("rawtypes")
	public long createIndeterminateTask(HashMap taskMap)
	{
		ContentValues initialValues = new ContentValues();

		int totalColumns = IndeterminateTaskSchema.ALL_COLUMNS.length;
		for (int i = 0; i < totalColumns; i++) {
			String columnName = IndeterminateTaskSchema.ALL_COLUMNS[i];

			if (!taskMap.containsKey(columnName) || columnName.equalsIgnoreCase(IndeterminateTaskSchema._ID)) {
				continue;
			}

			if (taskMap.get(columnName) instanceof String) {
				initialValues.put(columnName, taskMap.get(columnName).toString());

			} else if (taskMap.get(columnName) instanceof Integer) {
				initialValues.put(columnName, Integer.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Float) {
				initialValues.put(columnName, Float.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Long) {
				initialValues.put(columnName, Long.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Boolean) {
				initialValues.put(columnName, Boolean.valueOf(taskMap.get(columnName).toString()));

			} else {
				Log.d("", "columnName: " + columnName + ", value: " + taskMap.get(columnName));
			}
		}

		//return mDb.insert(IndeterminateTaskSchema.TABLE_NAME, null, initialValues);
		return mDb.insertWithOnConflict(IndeterminateTaskSchema.TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
	}

	public boolean deleteIndeterminateTask(long id)
	{
		return mDb.delete(IndeterminateTaskSchema.TABLE_NAME, IndeterminateTaskSchema._ID + " = " + id, null) > 0;
	}
	
	public boolean deleteAllIndeterminateTask() {
		return mDb.delete(IndeterminateTaskSchema.TABLE_NAME, null, null) > 0;
	}

	@SuppressWarnings("rawtypes")
	public boolean updateIndeterminateTask(long id, HashMap taskMap)
	{
		ContentValues initialValues = new ContentValues();

		int totalColumns = IndeterminateTaskSchema.ALL_COLUMNS.length;
		for (int i = 0; i < totalColumns; i++) {
			String columnName = IndeterminateTaskSchema.ALL_COLUMNS[i];

			if (!taskMap.containsKey(columnName) || columnName.equalsIgnoreCase(IndeterminateTaskSchema._ID)) {
				continue;
			}

			if (taskMap.get(columnName) instanceof String) {
				initialValues.put(columnName, taskMap.get(columnName).toString());

			} else if (taskMap.get(columnName) instanceof Integer) {
				initialValues.put(columnName, Integer.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Float) {
				initialValues.put(columnName, Float.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Long) {
				initialValues.put(columnName, Long.valueOf(taskMap.get(columnName).toString()));

			} else if (taskMap.get(columnName) instanceof Boolean) {
				initialValues.put(columnName, Boolean.valueOf(taskMap.get(columnName).toString()));

			} else {
				Log.d("", "columnName: " + columnName + ", value: " + taskMap.get(columnName));
			}
		}

		return mDb.update(IndeterminateTaskSchema.TABLE_NAME, initialValues, IndeterminateTaskSchema._ID + " = " + id, null) > 0;
	}

	public Cursor fetchAllIndeterminateTask()
	{
		String query = "SELECT * FROM " + TaskTableSchema.TABLE_NAME + " t1 JOIN " + IndeterminateTaskSchema.TABLE_NAME + " t2 ON t1." + TaskTableSchema._ID + " = t2." + IndeterminateTaskSchema.KEY_TASK_ID;
		return mDb.rawQuery(query, null);
	}

	public Cursor fetchIndeterminateTask(long id)
	{
		String query = "SELECT * FROM " + TaskTableSchema.TABLE_NAME + " t1 JOIN " + IndeterminateTaskSchema.TABLE_NAME + " t2 ON t1." + TaskTableSchema._ID + " = t2." + IndeterminateTaskSchema.KEY_TASK_ID
				+ " WHERE t2." + IndeterminateTaskSchema._ID + " = " + id;
		return mDb.rawQuery(query, null);
	}
}

