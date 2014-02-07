package com.example.reminder.controllers;

import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.models.TaskModel;
import com.example.reminder.utils.AppConstants;

public class TaskDetailsActivity extends DashboardActivity {

	private EditText etTaskName;
	private EditText etNotes;
	private CheckedTextView ctvPriority;

	private DBAdapter mDbAdapter;
	private Long mRowId;
	private Integer screenContext;

	private Boolean isNewTask;
	private TaskModel taskModel;





	//---------------------------------------------------
	// LIFE CYCLE METHODS
	//---------------------------------------------------

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView (R.layout.task_details_layout);		
		setTitleFromActivityLabel (R.id.title_text);

		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();


		etTaskName = (EditText) this.findViewById(R.id.edit_task_name);
		etNotes = (EditText) this.findViewById(R.id.notes);
		ctvPriority = (CheckedTextView) this.findViewById(R.id.priority);
		ctvPriority.setOnClickListener(onClickListener);

		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(TaskTableSchema._ID);
		// if context is from maps - CONTEXT_MAP_FROM_MAPS
		screenContext = (Integer) ((savedInstanceState == null) ? null : savedInstanceState.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT));		
		taskModel = (TaskModel) ((savedInstanceState == null) ? null : savedInstanceState.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL));
		isNewTask = ((savedInstanceState == null) ? null : savedInstanceState.getBoolean(AppConstants.BUNDLE_EXTRA_KEY_IS_NEW_TASK));

		if (mRowId == null || mRowId == 0) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(TaskTableSchema._ID) : null;			
			// if context is from maps - CONTEXT_MAP_FROM_MAPS
			screenContext = extras != null ? extras.getInt(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT) : null;			
			taskModel = (TaskModel) (extras != null ? extras.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL) : null);
			isNewTask = (extras != null ? extras.getBoolean(AppConstants.BUNDLE_EXTRA_KEY_IS_NEW_TASK) : null);
		}
		this.populateFields();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(TaskTableSchema._ID, mRowId);
		outState.putSerializable(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT, screenContext);
		outState.putSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
		outState.putSerializable(AppConstants.BUNDLE_EXTRA_KEY_IS_NEW_TASK, isNewTask);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void saveState() {
		if (this.taskModel == null) {
			this.taskModel = new TaskModel();
		}
		taskModel.setPriority(ctvPriority.isChecked() == true ? 1 : 0);
		taskModel.setTask(etTaskName.getText().toString());
		taskModel.setNotes(etNotes.getText().toString());
		taskModel.setLatitudeE6(taskModel.getLatitudeE6() == null ? 0 : taskModel.getLatitudeE6());
		taskModel.setLongitudeE6(taskModel.getLongitudeE6() == null ? 0: taskModel.getLongitudeE6());
		taskModel.setOnArrive(taskModel.getOnArrive() == null ? false : taskModel.getOnArrive());
		taskModel.setOnLeave(taskModel.getOnLeave() == null ? false : taskModel.getOnLeave());
		taskModel.setRadius(taskModel.getRadius() == null ? AppConstants.DEFAULT_MINIMUM_RADIUS : taskModel.getRadius());
		taskModel.setTimerOn(taskModel.getTimerOn() == null ? false : taskModel.getTimerOn());
		taskModel.setLocationOn((taskModel.getLocationOn() == null ? false : taskModel.getLocationOn()));
		taskModel.setReminderOn(taskModel.getTimerOn() ? true : taskModel.getLocationOn());
		taskModel.setSnoozeOn(taskModel.getSnoozeOn() == null ? AppConstants.DEACTIVATED : taskModel.getSnoozeOn());
		taskModel.setCompleted(taskModel.getCompleted() == null ? false : taskModel.getCompleted());
		
		if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS) {
			if (isNewTask) {
				taskModel.setOnArrive(true);
				taskModel.setLocationOn(true);
				taskModel.setReminderOn(true);
			}
		}

		HashMap taskMap = new HashMap();
		taskMap.put(TaskTableSchema.KEY_TASK, taskModel.getTask());
		taskMap.put(TaskTableSchema.KEY_NOTES, taskModel.getNotes());
		taskMap.put(TaskTableSchema.KEY_TIMER_ON, taskModel.getTimerOn());
		taskMap.put(TaskTableSchema.KEY_LATITUDE, taskModel.getLatitudeE6());
		taskMap.put(TaskTableSchema.KEY_LONGITUDE, taskModel.getLongitudeE6());
		taskMap.put(TaskTableSchema.KEY_ON_ARRIVE, taskModel.getOnArrive());
		taskMap.put(TaskTableSchema.KEY_ON_LEAVE, taskModel.getOnLeave());
		taskMap.put(TaskTableSchema.KEY_RADIUS, taskModel.getRadius());
		taskMap.put(TaskTableSchema.KEY_LOCATION_ON, taskModel.getLocationOn());			
		taskMap.put(TaskTableSchema.KEY_REMINDER_ON, taskModel.getReminderOn());
		taskMap.put(TaskTableSchema.KEY_SNOOZE_ON, taskModel.getSnoozeOn());
		taskMap.put(TaskTableSchema.KEY_TASK_COMPLETED, taskModel.getCompleted());
		taskMap.put(TaskTableSchema.KEY_PRIORITY, taskModel.getPriority());

		if ((mRowId == null || mRowId == 0) || isNewTask == true) {
			long id = mDbAdapter.createTask(taskMap);
			if (id > 0) {
				mRowId = id;
				//isNewTask = false;
			}
		} else {
			mDbAdapter.updateTask(mRowId, taskMap);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == AppConstants.MAP_REQUEST_CODE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// if context is from details - CONTEXT_MAP_FROM_DETAILS
				taskModel = (TaskModel) data.getSerializableExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL);
			}
		}
	}

	//---------------------------------------------------
	// HELPER METHODS
	//---------------------------------------------------

	private void populateFields() {
		if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS) {
			Button btnRemindMe = (Button) this.findViewById(R.id.remind_me);
			btnRemindMe.setVisibility(Button.GONE);
		}
		if (mRowId != null && mRowId != 0) {	
			if (isNewTask != null && isNewTask == false) {
				Cursor c = mDbAdapter.fetchTask(mRowId);
				startManagingCursor(c);
				etTaskName.setText(c.getString(c.getColumnIndex(TaskTableSchema.KEY_TASK)));
				etNotes.setText(c.getString(c.getColumnIndex(TaskTableSchema.KEY_NOTES)));
				ctvPriority.setChecked(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_PRIORITY)) == 1 ? true : false);
			}			
		}
	}

	// listeners---------------------
	public void onClickDone (View v) {
		this.saveState();


		if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS) {
			Intent intent = getIntent();
			if (isNewTask) {
				isNewTask = false;
				intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
			}			
			setResult(RESULT_OK, intent);

		} else {
			setResult(RESULT_OK);
		}


		this.finish();		
	}

	public void onClickRemindMe (View v) {
		Intent intent = new Intent(this, RemindMeActivity.class);
		intent.putExtra(TaskTableSchema._ID, mRowId);
		startActivityForResult(intent, AppConstants.MAP_REQUEST_CODE);
	}

	/*public void onClickPriority (View v) {

	}*/

	public OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == ctvPriority) {
				ctvPriority.setChecked(!ctvPriority.isChecked());
			}
		}
	};
}
