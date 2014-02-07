package com.example.reminder.controllers;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.reminder.controls.quickactions.ActionItem;
import com.example.reminder.controls.quickactions.QuickAction;
import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.IndeterminateTaskSchema;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.notification.SnoozeReceiver;
import com.example.reminder.utils.AppConstants;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

/**
 * This is the activity for feature 3 in the dashboard application.
 * It displays some text and provides a way to get back to the home activity.
 *
 */

public class NotificationViewController extends DashboardActivity 
{
	private int REL_SWIPE_MIN_DISTANCE; 
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;

	private DBAdapter mDbAdapter;
	private ListView taskListView;	
	private QuickAction mQuickAction;
	private PopupWindow pw;

	private long selectedID;
	private int selectedItemPriority;
	private int selectedItemCompleted;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView (R.layout.controller_notifications);
		setTitleFromActivityLabel (R.id.title_text);

		this.setupQuickAction();
		this.taskListView = (ListView) this.findViewById(R.id.current_task_list);

		// Use density-aware measurements. 
		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5); 
		REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);

		final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event); 
			}};
			this.taskListView.setOnTouchListener(gestureListener);

			this.taskListView.setOnItemLongClickListener(this.onItemLongClickListener);




			mDbAdapter = new DBAdapter(this);
			mDbAdapter.open();

			this.fillData();
	}

	@Override
	public void onResume() {
		super.onResume();

		// cancel any notification we may have received from ReminderBroadcastReceiver
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1234);

		refreshDisplay();

		// This demonstrates how to dynamically create a receiver to listen to the location updates.
		// You could also register a receiver in your manifest.
		final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
		registerReceiver(lftBroadcastReceiver, lftIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(lftBroadcastReceiver);
	}



	//---------------------------------------------------
	// HELPER METHODS
	//---------------------------------------------------

	// setup views---------------------	
	public void fillData()
	{	
		Cursor c = mDbAdapter.fetchAllIndeterminateTask();
		startManagingCursor(c);
		TaskCursorAdapter taskAdapter = new TaskCursorAdapter(this, c);
		this.taskListView.setAdapter(taskAdapter);
	}

	private void setupQuickAction() {
		ActionItem priorityAction = new ActionItem();		
		priorityAction.setTitle("Priority");
		priorityAction.setIcon(getResources().getDrawable(R.drawable.ic_add));

		ActionItem completeAction = new ActionItem();		
		completeAction.setTitle("Complete");
		completeAction.setIcon(getResources().getDrawable(R.drawable.ic_accept));

		ActionItem snoozeOffAction = new ActionItem();		
		snoozeOffAction.setTitle("Snooze Off");
		snoozeOffAction.setIcon(getResources().getDrawable(R.drawable.ic_up));

		mQuickAction = new QuickAction(this);		
		mQuickAction.addActionItem(priorityAction);
		mQuickAction.addActionItem(completeAction);
		mQuickAction.addActionItem(snoozeOffAction);

		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void onItemClick(int pos) {
				if (pos == 0) {
					HashMap data = new HashMap();
					data.put(TaskTableSchema.KEY_PRIORITY, selectedItemPriority == 1 ? 0 : 1);
					mDbAdapter.updateTask(selectedID, data);
					TaskCursorAdapter cursorAdapter = (TaskCursorAdapter) taskListView.getAdapter();
					cursorAdapter.getCursor().requery();

				} else if (pos == 1) {
					HashMap data = new HashMap();
					data.put(TaskTableSchema.KEY_TASK_COMPLETED, selectedItemCompleted == 1 ? 0 : 1);
					mDbAdapter.updateTask(selectedID, data);
					TaskCursorAdapter cursorAdapter = (TaskCursorAdapter) taskListView.getAdapter();
					cursorAdapter.getCursor().requery();

				} else if (pos == 2) {
					Intent alarmIntent = new Intent(NotificationViewController.this, SnoozeReceiver.class);
					alarmIntent.putExtra("REQUEST CODE",selectedID);
					AlarmManager almMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
					PendingIntent operation = PendingIntent.getBroadcast(NotificationViewController.this, (int) selectedID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);   
					almMgr.cancel(operation);

					HashMap data = new HashMap();
					data.put(TaskTableSchema.KEY_SNOOZE_ON, AppConstants.DISMISS);
					mDbAdapter.updateTask(selectedID, data);
				}	
			}
		});
	}

	private void refreshDisplay() {
		refreshDisplay(new LocationInfo(this));
	}

	private void refreshDisplay(final LocationInfo locationInfo) {
		if (locationInfo.anyLocationDataReceived()) {
			this.fillData();
		}
	}

	private final BroadcastReceiver lftBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// extract the location info in the broadcast
			final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
			// refresh the display with it
			refreshDisplay(locationInfo);
		}
	};
	




	private void showPopup(long ID) {
		//We need to get the instance of the LayoutInflater, use the context of this activity
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//Inflate the view from a predefined XML layout
		View layout = inflater.inflate(R.layout.detail_popup,
				(ViewGroup) findViewById(R.id.popup_element));
		

		Cursor c = mDbAdapter.fetchTask(ID);
		c.moveToFirst();
		if (c.getCount() > 0) {
			TextView tvMainHeader = (TextView) layout.findViewById(R.id.main_header);
			tvMainHeader.setText("Details");
			
			TextView tvContentHeader = (TextView) layout.findViewById(R.id.content_header);
			tvContentHeader.setText(c.getString(c.getColumnIndex(TaskTableSchema.KEY_TASK)));
			
			
			TextView tvDetails = (TextView) layout.findViewById(R.id.details);
			tvDetails.setText(c.getString(c.getColumnIndex(TaskTableSchema.KEY_NOTES)));
		}
		c.close();

		Button cancelButton = (Button) layout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(cancel_button_click_listener);


        DisplayMetrics dm = getResources().getDisplayMetrics();
		// create a 300px width and 470px height PopupWindow
		pw = new PopupWindow(layout, dm.widthPixels, dm.heightPixels, true);
		// display the popup in the center
		pw.showAtLocation(layout, Gravity.CENTER, 0, 20);
	}
	
	private OnClickListener cancel_button_click_listener = new OnClickListener() {
	    public void onClick(View v) {
	        pw.dismiss();
	    }
	};

	// Do not use LitView.setOnItemClickListener(). Instead, I override 
	// SimpleOnGestureListener.onSingleTapUp() method, and it will call to this method when
	// it detects a tap-up event.
	private void myOnItemClick(int position) {   
		Cursor theCursor = (Cursor) taskListView.getItemAtPosition(position);
		selectedID = theCursor.getInt(theCursor.getColumnIndex(IndeterminateTaskSchema.KEY_TASK_ID));
		this.showPopup(selectedID);
	}

	private void onLTRFling(int pos) {
		long id = taskListView.getItemIdAtPosition(pos);        
	}

	private void onRTLFling(int pos) {
		long id = taskListView.getItemIdAtPosition(pos);        
	}

	class MyGestureDetector extends SimpleOnGestureListener{ 

		// Detect a single-click and call my own handler.
		@Override 
		public boolean onSingleTapUp(MotionEvent e) {
			int pos = taskListView.pointToPosition((int)e.getX(), (int)e.getY());
			myOnItemClick(pos);
			return false;
		}

		@Override 
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { 
			if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) 
				return false; 
			if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && 
					Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
				int pos = taskListView.pointToPosition((int)e1.getX(), (int)e1.getY());            	
				onRTLFling(pos); 
			}  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && 
					Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
				int pos = taskListView.pointToPosition((int)e1.getX(), (int)e1.getY());            	
				onLTRFling(pos); 
			} 
			return false; 
		} 
	} 




	private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> viewgroup, View v, int index, long id) {
			Cursor theCursor = (Cursor) taskListView.getItemAtPosition(index);
			selectedID = theCursor.getInt(theCursor.getColumnIndex(IndeterminateTaskSchema.KEY_TASK_ID));

			//Cursor c = mDbAdapter.fetchIndeterminateTask(id);
			Cursor c = mDbAdapter.fetchTask(selectedID);
			c.moveToFirst();	
			if (c.getCount() > 0) {
				selectedItemPriority = c.getInt(c.getColumnIndex(TaskTableSchema.KEY_PRIORITY));
				selectedItemCompleted = c.getInt(c.getColumnIndex(TaskTableSchema.KEY_TASK_COMPLETED));

				if (c.getInt(c.getColumnIndex(TaskTableSchema.KEY_SNOOZE_ON)) != AppConstants.SNOOZE_ON) {
					ViewGroup trackViewGroup = mQuickAction.getmTrack();
					View snoozeView = trackViewGroup.getChildAt(3);
					snoozeView.setVisibility(View.GONE);
				}				
			}	
			c.close();

			mQuickAction.show(v);
			mQuickAction.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
			return false;
		}
	};

} // end class
