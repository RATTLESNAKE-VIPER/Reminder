package com.example.reminder.extensions;

import com.example.reminder.utils.AppConstants;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class ExtendedOverlayItem extends OverlayItem {
	final long id;
	private float radiusMeters;
	private String title, snippet;
	
	public ExtendedOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		this.id = -1;
		this.radiusMeters = AppConstants.DEFAULT_MINIMUM_RADIUS;
		this.title = title;
		this.snippet = snippet;
	}

	public ExtendedOverlayItem(GeoPoint point, String title, String snippet, long id, float radiusMeters) {
		super(point, title, snippet);
		this.id = id;
		this.radiusMeters = radiusMeters;
		this.title = title;
		this.snippet = snippet;
	}
	

	public long getId() {
		return id;
	}
	
	public float getRadiusMeters() {
		return this.radiusMeters;
	}

	public void setRadius(float radius) {
		this.radiusMeters = radius;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
	
	
}
