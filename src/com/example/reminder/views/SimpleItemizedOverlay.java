package com.example.reminder.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.example.reminder.controllers.R;
import com.example.reminder.extensions.ExtendedOverlayItem;
import com.example.reminder.utils.AppConstants;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class SimpleItemizedOverlay extends BalloonItemizedOverlay<ExtendedOverlayItem> implements OnGestureListener {

	private ArrayList<ExtendedOverlayItem> m_overlays = new ArrayList<ExtendedOverlayItem>();
	private Context c;

	private GestureDetector gestureDetector;
	private OnGestureListener onGestureListener;
	public Handler mHandler;


	public interface SimpleItemizedOverlayDelegate {
		public void onBalloonTap(int index, ExtendedOverlayItem item);
		public long getIDForNewPinDropped(int latitudeE6, int longitudeE6);
		public void onItemRadiusChanged(ExtendedOverlayItem item, float radius);
	}

	/**
	 * Amount of room to shift dragging images for finger height.
	 */
	private static final int FINGER_Y_OFFSET = -45;

	private final Paint fillPaint;
	private final Paint strokePaint;
	private final Paint dottedStrokePaint;
	private final Paint textPaint;

	private float miniCircleRadius = 3;
	private float currCircleRadius = 50;
	private final int strokeWidth = 2;


	private Drawable dragDrawable = null;
	private boolean currentlyDraggingCircle = false;
	private Boolean isMultiplePinsOnLongTouchAllowed = false;

	private SimpleItemizedOverlayDelegate delegate;
	
	
	
	
	private Location lastKnownLocation;
	private GeoPoint lastKnownPoint;
	private boolean myLocationEnabled;
	
	private Drawable centerDrawable;
	private Paint accuracyPaint;
	private Point center;
	private Point left;
	private Drawable drawable;
	private int width;
	private int height;
	
	


	public SimpleItemizedOverlay(Drawable defaultMarker, MapView mapView, SimpleItemizedOverlayDelegate delegate) {
		super(boundCenter(defaultMarker), mapView);
		this.centerDrawable = defaultMarker;
		c = mapView.getContext();
		this.delegate = delegate;
		gestureDetector = new GestureDetector(this);

		this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//this.fillPaint.setARGB(64, 255, 119, 107);
		this.fillPaint.setColor(Color.BLUE);
		this.fillPaint.setAlpha(25);

		this.strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//this.strokePaint.setARGB(255, 255, 119, 107);
		this.strokePaint.setColor(Color.BLUE);
		this.strokePaint.setAlpha(30);
		this.strokePaint.setStyle(Style.STROKE);
		this.strokePaint.setStrokeWidth(1);

		this.dottedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//this.dottedStrokePaint.setARGB(255, 148, 154, 170);
		this.dottedStrokePaint.setColor(Color.BLUE);
		this.dottedStrokePaint.setAlpha(50);
		this.dottedStrokePaint.setStyle(Style.STROKE);
		this.dottedStrokePaint.setStrokeWidth(strokeWidth);
		this.dottedStrokePaint.setPathEffect(new DashPathEffect(new float[] {20.0f, 7.5f}, 0.0f));
		
		
		this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.textPaint.setColor(Color.RED);
		this.textPaint.setStyle(Style.STROKE);
//		this.textPaint.setStrokeWidth(1);
		
		// My Location
		accuracyPaint = new Paint();
	    accuracyPaint.setAntiAlias(true);
	    accuracyPaint.setStrokeWidth(2.0f);

	    drawable = centerDrawable;
	    width = drawable.getIntrinsicWidth();
	    height = drawable.getIntrinsicHeight();
	    center = new Point();
	    left = new Point();
	}

	public void addOverlay(ExtendedOverlayItem overlay) {
		m_overlays.add(overlay);
		populate();
	}	

	@Override
	protected ExtendedOverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}
	
	private void replaceMyOverlay(ExtendedOverlayItem overlay) {
		int index = this.getIndexOfItem(-1);
	    if (index != -1) {
	    	m_overlays.remove(index);
	    	addOverlay(overlay);
		}
	}
	
	private int getIndexOfItem(long id) {
		int index = -1;
		
	    for (ExtendedOverlayItem item : m_overlays) {
			if (item.getId() == id) {
				index = m_overlays.indexOf(item);
				break;
			}
		}	    
	    
	    return index;
	}

	@Override
	protected boolean onBalloonTap(int index, ExtendedOverlayItem item) {
		//Toast.makeText(c, "onBalloonTap for overlay index " + index, Toast.LENGTH_LONG).show();

		if (this.delegate != null) {
			this.delegate.onBalloonTap(index, item);
		}
		
		return true;
	}


	public void setMultiplePinsOnLongTouch(Boolean isMultiplePinsOnLongTouch) {
		this.isMultiplePinsOnLongTouchAllowed = isMultiplePinsOnLongTouch;
	}


	private class GeocoderHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			String result;
			switch (message.what) {
			case ReverseGeocodingTask.UPDATE_ADDRESS:
				//Bundle bundle = message.getData();
				//result = bundle.getString("address");
				if (message.obj instanceof Address) {
					Address address = (Address) message.obj;
				}
				Log.d(SimpleItemizedOverlay.class.getCanonicalName(), "--------address: " + message.obj);
				break;
			default:
				result = null;
			}

		}   
	}



	// AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
	// we do not want to invoke it from the UI thread.
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
		private static final int UPDATE_ADDRESS = 0;
		Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

			Location loc = params[0];
			List<Address> addresses = null;	        
			try {
				// Call the synchronous getFromLocation() method by passing in the lat/long values.
				addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				// Update UI field with the exception.
				Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and country name.
				String addressText = String.format("%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
								address.getLocality(),
								address.getCountryName());
				// Update the UI via a message handler.
				Message.obtain(mHandler, UPDATE_ADDRESS, address).sendToTarget();
			}
			return null;
		}
	}



	/**
	 * Getting Latitude and Longitude on Touch event
	 * **/
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) 
	{   
		/*if (gestureDetector != null && gestureDetector.onTouchEvent(event)) {
			return true;
		}
		return false;*/


		boolean handled = false;
		ExtendedOverlayItem focusItem = getFocus();
		// Test for radius dragging before alarm dragging.  Otherwise the user can
		// get stuck with a tiny circle.
		if (focusItem != null
				&& event.getAction() == MotionEvent.ACTION_DOWN
				&& distance(mapView, event, focusItem) < currCircleRadius + strokeWidth * 2
				&& distance(mapView, event, focusItem) > currCircleRadius - strokeWidth * 2)
		{
			currentlyDraggingCircle = true;
			handled = true;
		}
		// Need to do actual drawable hit detection here, but hitTest doesn't
		// work as I'd expect, so do a distance test.
		else if (focusItem != null
				&& event.getAction() == MotionEvent.ACTION_DOWN
				&& distance(mapView, event, focusItem) < 20)
		{
			this.dragDrawable = boundCenterBottom(this.c.getResources().getDrawable(R.drawable.pin_grey));
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE && this.dragDrawable != null)
		{
			Rect bounds = this.dragDrawable.copyBounds();
			bounds.offsetTo(((int)event.getX()) - this.dragDrawable.getIntrinsicWidth() / 2, ((int)event.getY()) - this.dragDrawable.getIntrinsicHeight());
			// Leave room for a finger.
			bounds.offset(0, FINGER_Y_OFFSET);
			this.dragDrawable.setBounds(bounds);
			mapView.postInvalidate();
			handled = true;
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE && currentlyDraggingCircle)
		{
			currCircleRadius = distance(mapView, event, focusItem);
			handled = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			if (this.dragDrawable != null)
			{
				/*GeoPoint newGeoPoint = mapView.getProjection().fromPixels((int)event.getX(), (int)event.getY() + FINGER_Y_OFFSET);
            	moveItemTo(focusItem, newGeoPoint);*/
				this.dragDrawable = null;
				// Keep our item from losing focus.
				handled = true;
			}
			if (this.currentlyDraggingCircle)
			{
				setItemRadius(focusItem, currCircleRadius);
				this.currentlyDraggingCircle = false;
				handled = true;
			}
		}


		if (!this.currentlyDraggingCircle) {
			gestureDetector.onTouchEvent(event);
		}

		return handled;
	} 

	@Override
	public boolean onDown(MotionEvent e) {
		if (onGestureListener != null) {
			return onGestureListener.onDown(e);
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (onGestureListener != null) {
			return onGestureListener.onFling(e1, e2, velocityX, velocityY);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onLongPress(e);
		}


		this.dropPin(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (onGestureListener != null) {
			onGestureListener.onScroll(e1, e2, distanceX, distanceY);
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onShowPress(e);
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (onGestureListener != null) {
			onGestureListener.onSingleTapUp(e);
		}
		return false;
	}

	public boolean isLongpressEnabled() {
		return gestureDetector.isLongpressEnabled();
	}

	public void setIsLongpressEnabled(boolean isLongpressEnabled) {
		gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
	}

	public OnGestureListener getOnGestureListener() {
		return onGestureListener;
	}

	public void setOnGestureListener(OnGestureListener onGestureListener) {
		this.onGestureListener = onGestureListener;
	}



	private void dropPin(MotionEvent e) {
//		if (!this.isMultiplePinsOnLongTouchAllowed) {
//			this.m_overlays.clear();
//		}
		GeoPoint geopoint = this.getMapView().getProjection().fromPixels(
				(int) e.getX(),
				(int) e.getY());
		double latit = (geopoint.getLatitudeE6() / 1E6);
		double longi = (geopoint.getLongitudeE6() / 1E6);
		Log.d("-------", "Lat: " + latit + ", Lon: "+longi);
		
		int lat = (int) geopoint.getLatitudeE6();
		int lon = (int) geopoint.getLongitudeE6();
		long itemID = -1;
		
		if (this.delegate != null) {
			itemID = this.delegate.getIDForNewPinDropped(lat, lon);
		}

		GeoPoint point = new GeoPoint(lat,lon);
		
		if (!this.isMultiplePinsOnLongTouchAllowed) {
			ExtendedOverlayItem ExtendedOverlayItem = new ExtendedOverlayItem(point, this.m_overlays.get(0).getTitle(), this.m_overlays.get(0).getSnippet(), itemID, AppConstants.DEFAULT_MINIMUM_RADIUS);
			this.m_overlays.clear();
			this.addOverlay(ExtendedOverlayItem);
		} else {
			ExtendedOverlayItem ExtendedOverlayItem = new ExtendedOverlayItem(point, "Add New Task", "", itemID, AppConstants.DEFAULT_MINIMUM_RADIUS);
			this.addOverlay(ExtendedOverlayItem);
		}
		
		
		
		

		/*mHandler = new GeocoderHandler();
		// Bypass reverse-geocoding if the Geocoder service is not available on the
		// device. The isPresent() convenient method is only available on Gingerbread or above.
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
		// Since the geocoding API is synchronous and may take a while.  You don't want to lock
		// up the UI thread.  Invoking reverse geocoding in an AsyncTask.
		Location location = new Location("point A");
		location.setLatitude(lat / 1E6);
		location.setLongitude(lon / 1E6);
		Log.d(SimpleItemizedOverlay.class.getCanonicalName(), "----location: " + location);
		(new ReverseGeocodingTask(SimpleItemizedOverlay.this.c)).execute(new Location[] {location});
		//}
*/	}


	private float distance(MapView mapView, MotionEvent event, ExtendedOverlayItem focusItem)
	{
		return distance(event.getX(), event.getY(), mapView.getProjection().toPixels(focusItem.getPoint(), null));
	}

	private float distance(float x, float y, Point point)
	{
		return (float)Math.sqrt(square(x - point.x) + square(y - point.y));
	}

	private float square(float x)
	{
		return x * x;
	}


	private void setItemRadius(ExtendedOverlayItem item, float radius)
	{
		if (currCircleRadius <= AppConstants.DEFAULT_MAXIMUM_RADIUS) {
			item.setRadius(radius);
		} else {
			item.setRadius(AppConstants.DEFAULT_MAXIMUM_RADIUS);
		}
		
		item.setSnippet(item.getRadiusMeters() + " meters");
		
		if (this.delegate != null) {
			this.delegate.onItemRadiusChanged(item, radius);
		}
	}


	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		ExtendedOverlayItem item = getFocus();
		if (item != null)
		{
			Projection projection = mapView.getProjection();
			GeoPoint geoPoint = item.getPoint();
			//Point pt = projection.toPixels(geoPoint, null);
			Point pt = new Point();
	        projection.toPixels(geoPoint, pt);
			
			if (!this.currentlyDraggingCircle)
			{
				//Uncommenting the following two lines and commenting the last will make it so 
				//the size of the circle stays fixed relative the earth when zooming in and out.
				//The only problem is that it gets recorded in screen pixels right now, so ends up as not
				//many meters.  Need to calculate how many meters a radius is in meters and store that, and
				//the projection classes offer no easy conversion back.
				//float pixels = projection.metersToEquatorPixels(item.getRadiusMeters());
				//currCircleRadius = pixels;
				
				/*currCircleRadius = item.getRadiusMeters();	
				canvas.drawCircle((float)pt.x, (float)pt.y, currCircleRadius, this.fillPaint);
		        canvas.drawCircle((float)pt.x, (float)pt.y, currCircleRadius, this.strokePaint);
		        canvas.drawCircle((float)pt.x, (float)pt.y, miniCircleRadius, this.strokePaint);*/
				
				currCircleRadius = projection.metersToEquatorPixels(item.getRadiusMeters()) * (1 / FloatMath.cos((float) Math.toRadians(geoPoint.getLatitudeE6()/1E6)));	

				canvas.drawText(item.getRadiusMeters() + " meters", 10, 20, textPaint);

				drawCircle(canvas, this.fillPaint, pt, currCircleRadius);
				drawCircle(canvas, this.strokePaint, pt, currCircleRadius);
				drawCircle(canvas, this.strokePaint, pt, miniCircleRadius);
			}
			else
			{
		        //canvas.drawCircle((float)pt.x, (float)pt.y, currCircleRadius, this.dottedStrokePaint);
		        drawCircle(canvas, this.dottedStrokePaint, pt, currCircleRadius);
		        canvas.drawText(currCircleRadius + " meters", 10, 20, textPaint);
			}			
		}
		
		if (this.dragDrawable != null)
		{
			this.dragDrawable.draw(canvas);
		}

		if (this.myLocationEnabled) {
			drawMyLocation(canvas, mapView, lastKnownLocation, lastKnownPoint, 0);
		}
		
		super.draw(canvas, mapView, shadow);
	}

	private void drawCircle(Canvas canvas, Paint paint, Point center, float radius)
	{
		RectF circleRect = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
		canvas.drawOval(circleRect, paint);
	}
	
	
	// hack to get more accurate radius, because the accuracy is changing as the location
    // getting further away from the equator
    public int metersToRadius(MapView mapView, double latitude, float meters) {
        return (int) (mapView.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
    }
	
	
	
	
	
	
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc, long when) {
		if (lastFix != null && myLoc != null) {
		    Projection projection = mapView.getProjection();

		    double latitude = lastFix.getLatitude();
		    double longitude = lastFix.getLongitude();
		    float accuracy = lastFix.getAccuracy();

		    float[] result = new float[1];

		    Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
		    float longitudeLineDistance = result[0];

		    GeoPoint leftGeo = new GeoPoint((int) (latitude * 1e6), (int) ((longitude - accuracy
		            / longitudeLineDistance) * 1e6));
		    projection.toPixels(leftGeo, left);
		    projection.toPixels(myLoc, center);
		    int radius = center.x - left.x;

		    accuracyPaint.setColor(0xff6666ff);
		    accuracyPaint.setStyle(Style.STROKE);
		    canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

		    accuracyPaint.setColor(0x186666ff);
		    accuracyPaint.setStyle(Style.FILL);
		    canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

		    drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2,
		            center.y + height / 2);
		    drawable.draw(canvas);
		}	    
	}
	
	
	public void enableMyLocation() {
		this.myLocationEnabled = true;	
	}
	
	public void disableMyLocation() {
		this.myLocationEnabled = false;
		this.lastKnownLocation = null;
	    this.lastKnownPoint = null;
	}
	
	public void onLocationChanged(Location location) {
	    this.lastKnownLocation = location;
	    this.lastKnownPoint = createGeoPoint(location);
	    ExtendedOverlayItem item = new ExtendedOverlayItem(new GeoPoint((int)(this.lastKnownLocation.getLatitude()*1E6), (int)(this.lastKnownLocation.getLongitude()*1E6)), "CurrentLocation", "", -1, 0);
		
	    int index = this.getIndexOfItem(-1);
	    if (index == -1) {
	    	addOverlay(item);
		} else {
		    this.replaceMyOverlay(item);
		}
	    
	    this.getMapView().getController().animateTo(this.lastKnownPoint);
	}
	
	private GeoPoint createGeoPoint(Location loc) {
	    int lat = (int) (loc.getLatitude() * 1E6);
	    int lng = (int) (loc.getLongitude() * 1E6);
	    return new GeoPoint(lat, lng);
	}
	
	public Location getLastKnownLocation() {
	    return lastKnownLocation;
	}

	public GeoPoint getLastKnownPoint() {
	    return lastKnownPoint;
	}
}
