package com.example.reminder.database;

import android.provider.BaseColumns;

public interface IndeterminateTaskSchema extends BaseColumns {
	public static final String TABLE_NAME = "indeterminateTask";

	public static final String KEY_TASK_ID   = "schema_id";

	public static final String[] ALL_COLUMNS = new String[] {_ID, KEY_TASK_ID};
}
