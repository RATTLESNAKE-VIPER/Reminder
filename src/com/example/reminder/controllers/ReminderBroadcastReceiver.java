package com.example.reminder.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.IndeterminateTaskSchema;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.notification.NotifierHelper;
import com.example.reminder.utils.AppConstants;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ReminderBroadcastReceiver", "onReceive: received location update");

		final LocationInfo currentLocationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
		

        if (currentLocationInfo.anyLocationDataReceived()) {        	
        	DBAdapter mDbAdapter = new DBAdapter(context);
        	mDbAdapter.open();
        	Cursor c = mDbAdapter.fetchAllTaskForLocation((int)(currentLocationInfo.lastLat*1E6), (int)(currentLocationInfo.lastLong*1E6), 200, 50);
        	c.moveToFirst();
        	
        	
        	boolean deletedIndeterminate = true;
        	ArrayList<HashMap> locations = new ArrayList<HashMap>();        	
        	while (!c.isAfterLast()) {
        		float[] results = new float[1];
        		Location.distanceBetween(currentLocationInfo.lastLat, currentLocationInfo.lastLong, c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LATITUDE)) / 1E6, c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LONGITUDE)) / 1E6, results);
        		float distanceInMeters = results[0];
        		
    			Log.d("----task", c.getString(c.getColumnIndex(TaskTableSchema.KEY_TASK)));
    			Log.d("----meters: ", distanceInMeters + " radius: " + c.getFloat(c.getColumnIndex(TaskTableSchema.KEY_RADIUS)));

        		if (distanceInMeters <= c.getFloat(c.getColumnIndex(TaskTableSchema.KEY_RADIUS))) {
        			Log.d("----", "----IN");
        			HashMap data = new HashMap();
        			data.put(IndeterminateTaskSchema.KEY_TASK_ID, c.getLong(c.getColumnIndex(TaskTableSchema._ID)));
        			data.put("radius", c.getLong(c.getColumnIndex(TaskTableSchema.KEY_RADIUS)));
        			data.put("snooze", c.getInt(c.getColumnIndex(TaskTableSchema.KEY_SNOOZE_ON)));
        			locations.add(data);
        			
        			if (deletedIndeterminate) {
        				deletedIndeterminate = false;
						mDbAdapter.deleteAllIndeterminateTask();
					}
        			mDbAdapter.createIndeterminateTask(data);
        		}
        		
        		c.moveToNext();
        	}       	
        	c.close();

        	if (locations.size() > 0) {        		
        		HashMap data = null;
        		for (HashMap hashMap : locations) {
        			Log.d("----" + Integer.valueOf(hashMap.get("snooze").toString()), "----hashMap: " + hashMap);
					if (Integer.valueOf(hashMap.get("snooze").toString()) == AppConstants.DEACTIVATED) {
						data = hashMap;
						break;
					}
				}
        		
        		if (data != null) {
        			//HashMap data = locations.get(0);
        			long id = -1;
                	id = Long.valueOf(data.get(IndeterminateTaskSchema.KEY_TASK_ID).toString());

                	if (id != -1) {
                		Cursor cursor = mDbAdapter.fetchTask(id);
                    	Log.d("----IN task:", cursor.getString(cursor.getColumnIndex(TaskTableSchema.KEY_TASK)));
                    	
                    	String taskName = cursor.getString(cursor.getColumnIndex(TaskTableSchema.KEY_TASK));
        				String desc = cursor.getString(cursor.getColumnIndex(TaskTableSchema.KEY_NOTES));
        				cursor.close();
        				
        				
        				Bundle extras = new Bundle();
        				NotifierHelper.sendNotification(context, NotificationViewController.class, taskName, desc, locations.size(), true, false, extras);
        			    

                    	Intent i = new Intent();
                    	i.setClassName("com.example.reminder.controllers", "com.example.reminder.controllers.AlarmReceiverActivity");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                		i.putExtra(TaskTableSchema._ID, id);
                		i.putExtra(TaskTableSchema.KEY_TASK, taskName);
                		i.putExtra(TaskTableSchema.KEY_NOTES, desc);
                        context.startActivity(i);            	
        			} 
				}            	 
			}
        	
        	
        	mDbAdapter.close();

        	if (currentLocationInfo.hasLatestDataBeenBroadcast()) {
        		Log.d("ReminderBroadcastReceiver", "Latest location has been broadcast");

        	} else {
        		Log.d("ReminderBroadcastReceiver", "Location broadcast pending (last " + LocationInfo.formatTimeAndDay(currentLocationInfo.lastLocationUpdateTimestamp, true) + ")");
        	}
        } else {
        	Log.d("ReminderBroadcastReceiver", "No locations recorded yet");
        }
	}
}
