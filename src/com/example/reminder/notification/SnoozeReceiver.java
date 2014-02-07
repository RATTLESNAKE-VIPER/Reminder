package com.example.reminder.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.reminder.controllers.AlarmReceiverActivity;

public class SnoozeReceiver extends BroadcastReceiver {
	public static final String ALARM_ALERT_ACTION = "com.example.reminder.ALARM_ALERT";
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

	@Override
	public void onReceive(final Context context, Intent intent) {
        // do the thing in here
        // including figuring out the next time you want to run 
        // and scheduling another PendingIntent with the AlarmManager
		/*DBAdapter mDbAdapter = new DBAdapter(context);
    	mDbAdapter.open();
		Cursor c = mDbAdapter.fetchAllTaskForSnoozeType(AppConstants.SNOOZE_ON);
        c.moveToFirst();
        if (c.getCount() > 0) {
            Intent in = new Intent(Intent.ACTION_MAIN);
            in.setClass(context, AlarmReceiverActivity.class);            
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("REQUEST CODE", intent.getIntExtra("REQUEST CODE", 0));
            in.putExtra(TaskTableSchema._ID, c.getLong(c.getColumnIndex(TaskTableSchema._ID)));
    		in.putExtra(TaskTableSchema.KEY_TASK, c.getString(c.getColumnIndex(TaskTableSchema.KEY_TASK)));
    		in.putExtra(TaskTableSchema.KEY_NOTES, c.getString(c.getColumnIndex(TaskTableSchema.KEY_NOTES)));
            context.startActivity(in);
		}
        c.close();*/
		
		Intent in = new Intent(Intent.ACTION_MAIN);
        in.setClass(context, AlarmReceiverActivity.class);            
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.putExtra("REQUEST CODE", intent.getIntExtra("REQUEST CODE", 0));
        in.putExtras(intent.getExtras());
        context.startActivity(in);
    }
}
