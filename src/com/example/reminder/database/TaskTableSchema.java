package com.example.reminder.database;

import android.net.Uri;
import android.provider.BaseColumns;

public interface TaskTableSchema extends BaseColumns {
	public static final String TABLE_NAME = "task";

	public static final String AUTHORITY = "com.example.reminder";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

	public static final String KEY_TASK   = "task_name";
	public static final String KEY_DESCRIPTION  = "description";
	public static final String KEY_TIMER_ON  = "timer_on";
	public static final String KEY_TIME  = "time";
	public static final String KEY_LOCATION_ON  = "location_on";
	public static final String KEY_LATITUDE  = "latitudeE6";
	public static final String KEY_LONGITUDE  = "longitudeE6";
	public static final String KEY_ON_ARRIVE  = "on_arrive";
	public static final String KEY_ON_LEAVE  = "on_leave";
	public static final String KEY_RADIUS  = "radius";
	public static final String KEY_PRIORITY  = "priority";
	public static final String KEY_NOTES  = "notes";
	public static final String KEY_TASK_COMPLETED  = "task_completed";
	public static final String KEY_SNOOZE_ON  = "snooze_on";
	public static final String KEY_REMINDER_ON  = "reminder_on";
	
	public static final String[] ALL_COLUMNS = new String[] {_ID, KEY_TASK, KEY_DESCRIPTION, KEY_TIMER_ON, KEY_TIME, KEY_LOCATION_ON, KEY_LATITUDE, KEY_LONGITUDE, KEY_ON_ARRIVE, KEY_ON_LEAVE, KEY_RADIUS, KEY_PRIORITY, KEY_NOTES, KEY_TASK_COMPLETED, KEY_SNOOZE_ON, KEY_REMINDER_ON};
}