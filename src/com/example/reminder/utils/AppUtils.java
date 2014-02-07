package com.example.reminder.utils;



public class AppUtils {

	public static final double KPiDouble = 3.141592654;
	public static final double KDegreesToRadiansDouble = KPiDouble / 180.0;

	/**
	A constant to convert radians to metres for the Mercator and other projections.
	It is the semi-major axis (equatorial radius) used by the WGS 84 datum (see http://en.wikipedia.org/wiki/WGS84).
	 */
	public static final int KEquatorialRadiusInMetres = 6378137;


	/** Find the great-circle distance in metres, assuming a spherical earth, between two lat-long points in degrees. */
	public static double greatCircleDistanceInMeters(double aLong1,double aLat1,double aLong2,double aLat2) {
		aLong1 *= KDegreesToRadiansDouble;
		aLat1 *= KDegreesToRadiansDouble;
		aLong2 *= KDegreesToRadiansDouble;
		aLat2 *= KDegreesToRadiansDouble;

		double angle = Math.acos(Math.sin(aLat1) * Math.sin(aLat2) + Math.cos(aLat1) * Math.cos(aLat2) * Math.cos(aLong2 - aLong1));

		return angle * KEquatorialRadiusInMetres;		
	}


	public static boolean isPointInside(double centerX, double centerY, double x, double y, double radius) {		
		double D = Math.pow(centerX - x, 2) + Math.pow((centerY - y), 2);
		return D <= radius*radius;
	}
}
