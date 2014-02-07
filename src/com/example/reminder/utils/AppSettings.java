package com.example.reminder.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class AppSettings {
	public static AppSettings appSettings = null;

	public static AppSettings getInstance(){
		if(appSettings == null){
			appSettings = new AppSettings();
		}
		return appSettings;
	}


	// Snooze time ---------------------------------------
	public static void setSnoozeTime(long snoozeTime, Context context) {
		SharedPreferences prefs = Prefs.get(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong("snooze_time", snoozeTime);
		editor.commit();
	}

	public static long getAuthToken(Context context) {
		SharedPreferences prefs = Prefs.get(context);
		return prefs.getLong("snooze_time", 60000);
	}
	
	// Minimum Radius ---------------------------------------
	public static void setMinimumRadius(float minimumRadius, Context context) {
		SharedPreferences prefs = Prefs.get(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat("minimum_radius", minimumRadius);
		editor.commit();
	}

	public static float getMinimumRadius(Context context) {
		SharedPreferences prefs = Prefs.get(context);
		return prefs.getFloat("minimum_radius", AppConstants.DEFAULT_MINIMUM_RADIUS);
	}
	
	// Maximum Radius ---------------------------------------
		public static void setMaximumRadius(float minimumRadius, Context context) {
			SharedPreferences prefs = Prefs.get(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putFloat("maximum_radius", minimumRadius);
			editor.commit();
		}

		public static float getMaximumRadius(Context context) {
			SharedPreferences prefs = Prefs.get(context);
			return prefs.getFloat("maximum_radius", AppConstants.DEFAULT_MAXIMUM_RADIUS);
		}
}
