package com.example.reminder.controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.notification.SnoozeReceiver;
import com.example.reminder.utils.AppConstants;

public class AlarmReceiverActivity extends Activity {
	private MediaPlayer mMediaPlayer; 
	private DBAdapter mDbAdapter;
	private Long taskID;
	private String taskName;
	private String description;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();

		Bundle extras = getIntent().getExtras();
		taskID = extras != null ? extras.getLong(TaskTableSchema._ID) : null;
		taskName = extras != null ? extras.getString(TaskTableSchema.KEY_TASK) : null;
		description = extras != null ? extras.getString(TaskTableSchema.KEY_NOTES) : null;

		if (taskID != null && taskID > 0) {
			this.displayAlert();

		} else {
			setContentView(R.layout.alarm);

			Button stopAlarm = (Button) findViewById(R.id.stopAlarm);
			stopAlarm.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					mMediaPlayer.stop();
					finish();
					return false;
				}
			});
		}


		playSound(this, getAlarmUri());
	}

	private void displayAlert()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(taskName);
		builder.setMessage(description);
		builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void onClick(DialogInterface dialog, int id) {
				HashMap taskMap = new HashMap();
				taskMap.put(TaskTableSchema.KEY_SNOOZE_ON, AppConstants.DISMISS);
				mDbAdapter.updateTask(taskID, taskMap);

				mMediaPlayer.stop();
				dialog.cancel();
				finish();
			}
		});
		builder.setNegativeButton("Snooze",	new DialogInterface.OnClickListener() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void onClick(DialogInterface dialog, int id) {
				HashMap taskMap = new HashMap();
				taskMap.put(TaskTableSchema.KEY_SNOOZE_ON, AppConstants.SNOOZE_ON);
				mDbAdapter.updateTask(taskID, taskMap);
				AlarmReceiverActivity.this.snooze(getIntent().getExtras());

				mMediaPlayer.stop();
				dialog.cancel();
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void snooze(Bundle extras) 
	{
		//Set Calendar Value for Snooze Alarm
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 10); //SNOOZE_MIN = 1;
		long snoozeTime = calendar.getTimeInMillis();
		//Build Intent and Pending Intent to Set Snooze Alarm     
		Intent alarmIntent = new Intent(AlarmReceiverActivity.this, SnoozeReceiver.class);
		AlarmManager almMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		//int _id = (int)System.currentTimeMillis();
		alarmIntent.putExtra("REQUEST CODE", (int)extras.getLong(TaskTableSchema._ID));
		alarmIntent.putExtras(extras);
		PendingIntent sender = PendingIntent.getBroadcast(AlarmReceiverActivity.this, (int)extras.getLong(TaskTableSchema._ID), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);   
		almMgr.set(AlarmManager.RTC_WAKEUP, snoozeTime, sender);
	}

	private void playSound(Context context, Uri alert) {
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(context, alert);
			final AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (IOException e) {
			System.out.println("OOPS");
		}
	}

	//Get an alarm sound. Try for an alarm. If none set, try notification, 
	//Otherwise, ringtone.
	private Uri getAlarmUri() {
		Uri alert = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		return alert;
	}
}