package com.example.reminder.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.reminder.database.DBAdapter;
import com.example.reminder.database.TaskTableSchema;
import com.example.reminder.extensions.ExtendedOverlayItem;
import com.example.reminder.models.TaskModel;
import com.example.reminder.utils.AppConstants;
import com.example.reminder.views.SimpleItemizedOverlay;
import com.example.reminder.views.SimpleItemizedOverlay.SimpleItemizedOverlayDelegate;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.readystatesoftware.maps.OnSingleTapListener;
import com.readystatesoftware.maps.TapControlledMapView;

public class MapsActivity extends MapActivity implements SimpleItemizedOverlayDelegate {
	TapControlledMapView mapView; // use the custom TapControlledMapView
	//MyLocationOverlay mMyLocationOverlay;
	SimpleItemizedOverlay mMyLocationOverlay;
	List<Overlay> mapOverlays;
	Drawable drawable;
	SimpleItemizedOverlay itemizedOverlay;

	ArrayList<TaskModel> taskList;
	TaskModel taskModel;
	int screenContext;
	private DBAdapter mDbAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		taskList = new ArrayList<TaskModel>();
		mDbAdapter = new DBAdapter(this);
		mDbAdapter.open();

		mapView = (TapControlledMapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		// dismiss balloon upon single tap of MapView (iOS behavior) 
		mapView.setOnSingleTapListener(new OnSingleTapListener() {		
			@Override
			public boolean onSingleTap(MotionEvent e) {
				itemizedOverlay.hideAllBalloons();
				mapView.postInvalidate();
				return true;
			}
		});
		
		final MapController mc = mapView.getController();
		mc.setZoom(16);


		
		// My location overlay
		drawable = getResources().getDrawable(R.drawable.marker2);		
		mMyLocationOverlay = new SimpleItemizedOverlay(drawable, mapView, null);


		// first overlay
		drawable = getResources().getDrawable(R.drawable.mark_red);
		itemizedOverlay = new SimpleItemizedOverlay(drawable, mapView, this);
		// set iOS behavior attributes for overlay
		itemizedOverlay.setShowClose(false);
		itemizedOverlay.setSnapToCenter(true);


		mapOverlays = mapView.getOverlays();
		mapOverlays.add(mMyLocationOverlay);
		mapOverlays.add(itemizedOverlay);


		if (savedInstanceState != null) {			
			// example restoring focused state of overlays
			int focused;
			focused = savedInstanceState.getInt("focused_1", -1);
			if (focused >= 0) {
				itemizedOverlay.setFocus(itemizedOverlay.getItem(focused));
			}

			taskModel = (TaskModel) savedInstanceState.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL);
			screenContext = (Integer) savedInstanceState.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT);
		}		

		if (taskModel == null) {
			Bundle extras = getIntent().getExtras();
			taskModel = (TaskModel) (extras != null ? extras.getSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL) : null);
			screenContext = extras != null ? extras.getInt(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT) : null;
		}



		if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS) {
			itemizedOverlay.setShowDisclosure(true);
			itemizedOverlay.setMultiplePinsOnLongTouch(true);

			Cursor c = mDbAdapter.fetchAllTaskForLocation();
			startManagingCursor(c);
			c.moveToFirst();			

			while (c.isAfterLast() == false) {
				TaskModel model = TaskModel.populateModelFromCursor(c);
				taskList.add(model);

				GeoPoint point = new GeoPoint(model.getLatitudeE6(), model.getLongitudeE6());
				ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(point, model.getTask(), model.getRadius() + " meters", model.getTaskID(), model.getRadius());
				itemizedOverlay.addOverlay(overlayItem);

				c.moveToNext();
			}

		} else if (screenContext == AppConstants.CONTEXT_MAP_FROM_DETAILS) {
			GeoPoint point = new GeoPoint(taskModel.getLatitudeE6(), taskModel.getLongitudeE6());
			ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(point, taskModel.getTask(), taskModel.getRadius() + " meters", taskModel.getTaskID(), taskModel.getRadius());
			itemizedOverlay.addOverlay(overlayItem);
			itemizedOverlay.setShowDisclosure(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mMyLocationOverlay.enableMyLocation();	

		// cancel any notification we may have received from ReminderBroadcastReceiver
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1234);

		refreshDisplay();

		// This demonstrates how to dynamically create a receiver to listen to the location updates.
		// You could also register a receiver in your manifest.
		final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
		registerReceiver(lftBroadcastReceiver, lftIntentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		mMyLocationOverlay.disableMyLocation();
		unregisterReceiver(lftBroadcastReceiver);
	}

	private void refreshDisplay() {
		refreshDisplay(new LocationInfo(this));
	}

	private void refreshDisplay(final LocationInfo locationInfo) {
		if (locationInfo.anyLocationDataReceived()) {
			Location loc = new Location(LocationManager.GPS_PROVIDER);
		    loc.setLatitude(locationInfo.lastLat);
		    loc.setLongitude(locationInfo.lastLong);
			mMyLocationOverlay.onLocationChanged(loc);
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





	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// example saving focused state of overlays
		if (itemizedOverlay.getFocus() != null) outState.putInt("focused_1", itemizedOverlay.getLastFocusedIndex());
		outState.putSerializable(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
		outState.putSerializable(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT, screenContext);
		super.onSaveInstanceState(outState);
	}



	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_S) {
			mapView.setSatellite(!mapView.isSatellite());
			return(true);
		}
		else if (keyCode == KeyEvent.KEYCODE_Z) {
			mapView.displayZoomControls(true);
			return(true);
		}

		return(super.onKeyDown(keyCode, event));
	}



	public void setTitleFromActivityLabel (int textViewId)
	{
		TextView tv = (TextView) findViewById (textViewId);
		if (tv != null) tv.setText (getTitle ());
	}

	public void onClickHome (View v)
	{
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity (intent);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onClickDone (View view) {
		if (screenContext == AppConstants.CONTEXT_MAP_FROM_DETAILS) {
			Intent intent = getIntent();

			if (itemizedOverlay.size() > 0) {
				ExtendedOverlayItem overlayItem = itemizedOverlay.getItem(0);
				taskModel.setLatitudeE6(overlayItem.getPoint().getLatitudeE6());
				taskModel.setLongitudeE6(overlayItem.getPoint().getLongitudeE6());
				taskModel.setRadius(overlayItem.getRadiusMeters());
			}			

			intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, taskModel);
			setResult(RESULT_OK, intent);

		} else if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS) {
			for (int i = 0; i < taskList.size(); i++) {
				TaskModel model = taskList.get(i);

				if (model.getTaskID() != null) {
					if (itemizedOverlay.size() > 0) {
						ExtendedOverlayItem overlayItem = itemizedOverlay.getItem(i);
						if (overlayItem.getId() == model.getTaskID()) {
							model.setLatitudeE6(overlayItem.getPoint().getLatitudeE6());
							model.setLongitudeE6(overlayItem.getPoint().getLongitudeE6());
							model.setRadius(overlayItem.getRadiusMeters());							
							model.setTimerOn(false);
							model.setOnArrive(true);
							model.setLocationOn(true);
							model.setReminderOn(true);

							HashMap taskMap = new HashMap();
							taskMap.put(TaskTableSchema.KEY_LATITUDE, model.getLatitudeE6());
							taskMap.put(TaskTableSchema.KEY_LONGITUDE, model.getLongitudeE6());
							taskMap.put(TaskTableSchema.KEY_RADIUS, model.getRadius());							
							taskMap.put(TaskTableSchema.KEY_TIMER_ON, model.getTimerOn());	
							taskMap.put(TaskTableSchema.KEY_ON_ARRIVE, model.getOnArrive());	
							taskMap.put(TaskTableSchema.KEY_LOCATION_ON, model.getLocationOn());			
							taskMap.put(TaskTableSchema.KEY_REMINDER_ON, model.getReminderOn());


							mDbAdapter.updateTask(model.getTaskID(), taskMap);
						}						
					}
				}			
			}
		}

		this.finish();
	}



	public void onBalloonTap(int index, ExtendedOverlayItem item) {
		if (screenContext == AppConstants.CONTEXT_MAP_FROM_MAPS && item.getId() > 0) {
			Intent intent = new Intent(this, TaskDetailsActivity.class);			
			intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_CONTEXT, AppConstants.CONTEXT_MAP_FROM_MAPS);

			boolean isNew = true;

			for (int i = 0; i < taskList.size(); i++) {
				TaskModel model = taskList.get(i);
				if (model.getTaskID() == item.getId()) {
					intent.putExtra(TaskTableSchema._ID, item.getId());
					intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, model);
					isNew = false;
					break;
				}
			}	

			if (isNew) {
				TaskModel model = new TaskModel();
				model.setTaskID(item.getId());
				model.setLatitudeE6(item.getPoint().getLatitudeE6());
				model.setLongitudeE6(item.getPoint().getLongitudeE6());
				model.setRadius(item.getRadiusMeters());
				//taskList.add(model);

				intent.putExtra(TaskTableSchema._ID, model.getTaskID());
				intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_IS_NEW_TASK, true);
				intent.putExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL, model);
			}

			this.startActivityForResult(intent, AppConstants.TASK_DETAILS_REQUEST_CODE);
		}	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == AppConstants.TASK_DETAILS_REQUEST_CODE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// if context is from details - CONTEXT_MAP_FROM_DETAILS
				if (data.getSerializableExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL) != null) {
					TaskModel model = (TaskModel) data.getSerializableExtra(AppConstants.BUNDLE_EXTRA_KEY_TASK_MODEL);
					if (model != null) {
						taskList.add(model);
						
						if (model.getTaskID() != null) {
							if (itemizedOverlay.size() > 0) {
								ExtendedOverlayItem overlayItem = itemizedOverlay.getItem(taskList.indexOf(model));
								if (overlayItem.getId() == model.getTaskID()) {
									overlayItem.setTitle(model.getTask());									
									overlayItem.setRadius(model.getRadius());
									overlayItem.setSnippet(model.getRadius() + " meters");	
									
									itemizedOverlay.hideBalloon();
								}
							}
						}
					}
				}				
			}
		}
	}

	public long getIDForNewPinDropped(int latitudeE6, int longitudeE6) {
		ExtendedOverlayItem overlayItem = itemizedOverlay.size() > 0 ? itemizedOverlay.getItem(itemizedOverlay.size()-1) : null;
		long id = overlayItem != null ? overlayItem.getId() + 1 : -1;
		return id;		
	}

	public void onItemRadiusChanged(ExtendedOverlayItem item, float radius) {

	}
}