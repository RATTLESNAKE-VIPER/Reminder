package com.example.reminder.controllers;

import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.utils.AppConstants;

/**
 * This is the activity for feature 1 in the dashboard application.
 * It displays some text and provides a way to get back to the home activity.
 *
 */

public class ReminderViewController extends DashboardActivity 
{
	private int REL_SWIPE_MIN_DISTANCE; 
    private int REL_SWIPE_MAX_OFF_PATH;
    private int REL_SWIPE_THRESHOLD_VELOCITY;
    
	private DBAdapter mDbAdapter;
	private ListView taskListView;	
	private Long mSelectedItemID;

	//---------------------------------------------------
	// LIFE CYCLE METHODS
	//---------------------------------------------------

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView (R.layout.controller_reminders);
		setTitleFromActivityLabel (R.id.title_text);
		

		this.taskListView = (ListView) this.findViewById(R.id.task_list);
		//this.taskListView.setOnItemClickListener(this.listViewOnItemClickListener);
		
		
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


		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();

		mSelectedItemID = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(TaskTableSchema._ID);
		if (mSelectedItemID == null) {
			Bundle extras = getIntent().getExtras();
			mSelectedItemID = extras != null ? extras.getLong(TaskTableSchema._ID) : null;
		}

		this.fillData();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(TaskTableSchema._ID, mSelectedItemID);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == AppConstants.TASK_DETAILS_REQUEST_CODE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				ReminderViewController.this.fillData();
			}
		}
	}


	//---------------------------------------------------
	// HELPER METHODS
	//---------------------------------------------------

	// setup views---------------------	
	public void fillData()
	{		
		Cursor c = mDbAdapter.fetchAllTasks();
		startManagingCursor(c);
		TaskCursorAdapter taskAdapter = new TaskCursorAdapter(this, c);
		this.taskListView.setAdapter(taskAdapter);
	}
	
	public void removeTask(long id) {
		mDbAdapter.deleteTask(id);
        TaskCursorAdapter cursorAdapter = (TaskCursorAdapter) taskListView.getAdapter();
        cursorAdapter.getCursor().requery();
	}

	// listeners---------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onClickAdd (View v)
	{
		EditText newTaskEditText = (EditText) this.findViewById(R.id.new_task);		

		if (newTaskEditText.getText().length() > 0) {			
			HashMap taskMap = new HashMap();
			taskMap.put(TaskTableSchema.KEY_TASK, newTaskEditText.getText().toString().trim());
			mDbAdapter.createTask(taskMap);
			ReminderViewController.this.fillData();

			newTaskEditText.setText("");
			newTaskEditText.clearFocus();
		}		
	}

	/*private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			F1Activity.this.mSelectedItemID = id;
			Intent intent = new Intent(F1Activity.this, TaskDetailsActivity.class);
			intent.putExtra(TaskTableSchema._ID, F1Activity.this.mSelectedItemID);
			startActivityForResult(intent, AppConstants.TASK_DETAILS_REQUEST_CODE);
		}
	};*/
	
	
	
	// Do not use LitView.setOnItemClickListener(). Instead, I override 
    // SimpleOnGestureListener.onSingleTapUp() method, and it will call to this method when
    // it detects a tap-up event.
    private void myOnItemClick(int position) {        
        long id = taskListView.getItemIdAtPosition(position);

        ReminderViewController.this.mSelectedItemID = id;
		Intent intent = new Intent(ReminderViewController.this, TaskDetailsActivity.class);
		intent.putExtra(TaskTableSchema._ID, ReminderViewController.this.mSelectedItemID);
		startActivityForResult(intent, AppConstants.TASK_DETAILS_REQUEST_CODE);
    }
    
    private void onLTRFling(int pos) {
        long id = taskListView.getItemIdAtPosition(pos);
        this.removeTask(id);
    }

    private void onRTLFling(int pos) {
    	long id = taskListView.getItemIdAtPosition(pos);
        this.removeTask(id);
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

} // end class
