package com.example.reminder.controllers;

import android.os.Bundle;

import com.example.reminder.database.DBAdapter;

public class HomeActivity extends DashboardActivity {

	private DBAdapter mDbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();
		//this.setAlarm();
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/*public void setAlarm() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 10);
		//Create a new PendingIntent and add it to the AlarmManager
		//Intent alarmIntent = new Intent(this, AlarmReceiverActivity.class);
		//PendingIntent pendingIntent = PendingIntent.getActivity(this, 12345, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		Intent alarmIntent = new Intent(this, SnoozeReceiver.class);
		int _id = (int)System.currentTimeMillis();
		alarmIntent.putExtra("REQUEST CODE", _id);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
		//am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10*1000, pendingIntent);
	}*/
}
