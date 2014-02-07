package com.example.reminder.controllers;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.models.TaskModel;
import com.example.reminder.utils.AppConstants;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;

public class RemindMeActivity extends DashboardActivity {

	private ToggleButton tbLocationOnOff;
	private TextView tvLocationTitle;
	private TextView tvLocation;
	private CheckedTextView ctvWhenLeave;
	private CheckedTextView ctvWhenArrive;

	private DBAdapter mDbAdapter;
	private Long mRowId;
	private TaskModel taskModel;
	

	

	//---------------------------------------------------
	// LIFE CYCLE METHODS
	//---------------------------------------------------

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView (R.layout.activity_remind_me);		
		setTitleFromActivityLabel (R.id.title_text);


		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();


		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(TaskTableSchema._ID);
		if (mRowId == null || mRowId == 0) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(TaskTableSchema._ID) : null;	
		}


		tbLocationOnOff = (ToggleButton) findViewById(R.id.tbLocationOnOff);
		tbLocationOnOff.setOnCheckedChangeListener(onCheckedChangeListener);
		
		tvLocationTitle = (TextView) findViewById(R.id.locationTitle);
		tvLocation = (TextView) findViewById(R.id.locationDesc);
		ctvWhenLeave = (CheckedTextView) findViewById(R.id.whenLeave);
		ctvWhenArrive = (CheckedTextView) findViewById(R.id.whenArrive);

		populateFields();

		
		tvLocation.setOnClickListener(onClickListener);
		ctvWhenArrive.setOnClickListener(onClickListener);
		ctvWhenLeave.setOnClickListener(onClickListener);
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == AppConstants.MAP_REQUEST_CODE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// if context is from details - CONTEXT_MAP_FROM_DETAILS
				taskModel = (TaskModel) data.getSerializableExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL);
				RemindMeActivity.this.setLocation();
			}
		}
	}


	//---------------------------------------------------
	// HELPER METHODS
	//---------------------------------------------------

	private void saveState() {
		this.setLocation();
		taskModel.setLocationOn(tbLocationOnOff.isChecked());
		taskModel.setOnArrive(ctvWhenArrive.isChecked());
		taskModel.setOnLeave(ctvWhenLeave.isChecked());
	}

	private void populateFields() {
		if (mRowId != null && mRowId != 0) {
			Cursor c = mDbAdapter.fetchTask(mRowId);
			startManagingCursor(c);

			tbLocationOnOff.setChecked(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LOCATION_ON)) == 1 ? true : false);
			ctvWhenLeave.setChecked(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_ON_LEAVE)) == 1 ? true : false);
			ctvWhenArrive.setChecked(c.getInt(c.getColumnIndex(TaskTableSchema.KEY_ON_ARRIVE)) == 1 ? true : false);
		}
	}
	
	public void setLocation() {
		if (taskModel == null) {
			taskModel = new TaskModel();
		}
		
		if (taskModel.getLatitudeE6() == null || taskModel.getLatitudeE6() == 0) {
			if (mRowId != null && mRowId != 0) {
				Cursor c = mDbAdapter.fetchTask(mRowId);
				startManagingCursor(c);
				
				int latE6 = c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LATITUDE));
				int lonE6 = c.getInt(c.getColumnIndex(TaskTableSchema.KEY_LONGITUDE));
				if (latE6 != 0 && lonE6 != 0) {
					tvLocationTitle.setText(this.getResources().getString(R.string.location));
					taskModel.setLatitudeE6(latE6);
					taskModel.setLongitudeE6(lonE6);
				} else {
					tvLocationTitle.setText(this.getResources().getString(R.string.currentLocation));
					LocationInfo latestInfo = new LocationInfo(getBaseContext());
					taskModel.setLatitudeE6((int)latestInfo.lastLat);
					taskModel.setLongitudeE6((int)latestInfo.lastLong);
				}
			}			
		}
		tvLocation.setText("Lat: " + taskModel.getLatitudeE6() + ", Lon:" + taskModel.getLongitudeE6());
	}

		

	public void showMap() {
		Intent intent = new Intent(this, MapsActivity.class);
		if (taskModel != null && taskModel.getTaskID() != null) {
			intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
			intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT, AppConstants.CONTEXT_MAP_FROM_DETAILS);
		} else {
			if (mRowId != null && mRowId != 0) {
				Cursor c = mDbAdapter.fetchTask(mRowId);
				startManagingCursor(c);

				TaskModel model = TaskModel.populateModelFromCursor(c);
				intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, model);
				intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT, AppConstants.CONTEXT_MAP_FROM_DETAILS);
			}
		}		
		startActivityForResult(intent, AppConstants.MAP_REQUEST_CODE);
	}
	
	
	public void showAllLocationFields() {
		tvLocationTitle.setVisibility(View.VISIBLE);
		tvLocation.setVisibility(View.VISIBLE);
//		ctvWhenLeave.setVisibility(View.VISIBLE);
//		ctvWhenArrive.setVisibility(View.VISIBLE);
	}
	
	public void hideAllLocationFields() {
		tvLocationTitle.setVisibility(View.GONE);
		tvLocation.setVisibility(View.GONE);
//		ctvWhenLeave.setVisibility(View.GONE);
//		ctvWhenArrive.setVisibility(View.GONE);
	}

	// listeners---------------------
	public void onClickDone (View v) {
		this.saveState();
		Intent intent = getIntent();
		intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
		setResult(RESULT_OK, intent);
		this.finish();		
	}

	public OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				RemindMeActivity.this.setLocation();
				showAllLocationFields();				
			}else{
				hideAllLocationFields();
			}
		}
	};
	
	public OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == ctvWhenArrive) {
				ctvWhenArrive.setChecked(!ctvWhenArrive.isChecked());
			} else if (v == ctvWhenLeave) {
				ctvWhenLeave.setChecked(!ctvWhenLeave.isChecked());
			} else if (v == tvLocation) {
				showMap();
			}
		}
	};
}
