package com.example.reminder.utils;

public final class AppConstants {
	
	public AppConstants() {
        // don't allow the class to be instantiated
    }

	// Request Code ----------------------------
	public static final int TASK_DETAILS_REQUEST_CODE = 0;
	public static final int MAP_REQUEST_CODE = 1;

	// Context ----------------------------
	public static final int CONTEXT_MAP_FROM_DETAILS = 0;
	public static final int CONTEXT_MAP_FROM_MAPS = 1;

	// Bundle Extra ----------------------------
	public static final String BUNDLE_EXTRA_KEY_TASK_MODEL = "taskModel";
	public static final String BUNDLE_EXTRA_KEY_TASK_ARRAY = "taskList";
	public static final String BUNDLE_EXTRA_KEY_CONTEXT = "context";
	public static final String BUNDLE_EXTRA_KEY_IS_NEW_TASK = "isNewTask";

	// Snooze Type ----------------------------
	public static final int DEACTIVATED = 0;
	public static final int SNOOZE_ON = 1;
	public static final int DISMISS = 2;
	
	
	// Common Constants ----------------------------
	public static final int DEFAULT_MINIMUM_RADIUS = 500; //0.5Km == 500meters
	public static final int DEFAULT_MAXIMUM_RADIUS = 2000; // 2Km == 2000meters
	
	// Milliseconds in the snooze duration, which translates to 20 seconds.
    public static final int SNOOZE_DURATION = 20000;
    public static final int DEFAULT_TIMER_DURATION = 10000;
    
    public static final String ACTION_SNOOZE = "com.example.reminder.ACTION_SNOOZE";
    public static final String ACTION_DISMISS = "com.example.reminder.ACTION_DISMISS";
    public static final String ACTION_PING = "com.example.reminder.ACTION_PING";
    
    public static final String EXTRA_MESSAGE= "com.example.reminder.EXTRA_MESSAGE";
    public static final String EXTRA_TIMER = "com.example.reminder.EXTRA_TIMER";
    
    public static final int NOTIFICATION_ID = 001;
    public static final String DEBUG_TAG = "Reminder";
}
