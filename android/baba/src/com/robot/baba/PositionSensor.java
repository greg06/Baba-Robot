package com.robot.baba;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import com.robot.baba.R;
import com.robot.baba.PositionMarker;

/**
 * Permet de conna�tre � tout moment plusieurs positions :
 * - Acc�l�rom�tre : x,y,z
 * - Bousole : angle
 * - GPS : longitude, latitude, altitude, vitesse
 */
public final class PositionSensor implements 	LocationListener, 
												SensorEventListener
												{
	
	/** Contient la derni�re position relev�e **/
	public static PositionMarker CURRENT_POSITION = null;
	
	/**
	 * Fr�quence de rafraichissement du sensor en ms.
	 */
	private final static int SENSOR_REFRESH_MS = 100;
	
	private float x,y,z;
	private long lastAccelerometerUpdate = System.currentTimeMillis();
	
	private float angle;
	private long lastOrientationUpdate = System.currentTimeMillis();	
	
	private double longitude, latitude, altitude;
	private float speed;
	private long lastGpsUpdate = System.currentTimeMillis();

	private Activity activity = null;
	
	private long lastRefreshLayoutUpdate = System.currentTimeMillis();
	
	/**
	 * Constructeur mettant en place :
	 * - la sonde acc�l�rom�tre;
	 * - la sonde orientation;
	 * - la sonde GPS.
	 */
	public PositionSensor(Activity activity) {
		this.activity = activity;

        // Met en place la sonde multipositions
        {
        	SensorManager m = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        	m.registerListener(
        						this, 
        						m.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        						SensorManager.SENSOR_DELAY_NORMAL
        						);
        	
        	m.registerListener(
        						this, 
								m.getDefaultSensor(Sensor.TYPE_ORIENTATION),
								SensorManager.SENSOR_DELAY_NORMAL
								);
        	
        	// Sonde GPS
        	{
        		LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        		locationManager.requestLocationUpdates(
        				LocationManager.GPS_PROVIDER, 
        				0, 
        				0, 
        				this);   	
        	} // end-block
        } // end-block
	}
	
	/**
	 * Met � jour les donn�es longitude,latitude,... dans la vue principale.
	 */
 	public void refreshLayout() {
 		
 		// Contr�le la fr�quence de mise � jour du layout
 		{
 			long currTime = System.currentTimeMillis();
 			if (currTime - lastRefreshLayoutUpdate < SENSOR_REFRESH_MS)
 				return;
 			lastRefreshLayoutUpdate = System.currentTimeMillis();
 		} // end-block

 		// R�cup�re la derni�re position enregistr�e
 		CURRENT_POSITION = null;
 		{
 			CURRENT_POSITION = new PositionMarker(	0, // techKey
												Calendar.getInstance(), // creationDate
												angle, 
												x, 
												y, 
												z, 
												longitude,
												latitude,
												altitude,
												speed
											);
 		} // end-block
 		
 		// Met � jour le layout
 		{
	 		TextView altitude = (TextView) activity.findViewById(R.id.txtAltitude);
	 		TextView longitude = (TextView) activity.findViewById(R.id.txtLongitude);
	 		TextView latitude = (TextView) activity.findViewById(R.id.txtLatitude);
	 		TextView degree = (TextView) activity.findViewById(R.id.txtDegree);
	 		TextView X = (TextView) activity.findViewById(R.id.txtX);
	 		TextView Y = (TextView) activity.findViewById(R.id.txtY);
	 		TextView Z = (TextView) activity.findViewById(R.id.txtZ);
	 		
	 		altitude.setText("" + CURRENT_POSITION.getAltitude());
	 		longitude.setText("" + CURRENT_POSITION.getLongitude());
	 		latitude.setText("" + CURRENT_POSITION.getLatitude());
	 		degree.setText("" + CURRENT_POSITION.getAngle());
	 		X.setText("" + CURRENT_POSITION.getX());
	 		Y.setText("" + CURRENT_POSITION.getY());
	 		Z.setText("" + CURRENT_POSITION.getZ());
 		} // end-block
	}
	
 	/**
 	 * Ex�cut� par la sonde GPS pour mettre � jour des attributs contenant les coordon�es GPS.
 	 */
	@Override
	public void onLocationChanged(Location location) {
		{
			long currTime = System.currentTimeMillis();
			if (currTime - lastGpsUpdate < SENSOR_REFRESH_MS)
				return;
			lastGpsUpdate = System.currentTimeMillis();
		} // end-block
		altitude = location.getAltitude();
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		speed = location.getSpeed();
		refreshLayout();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Ex�cut� par les sondes acc�l�rom�tre et boussole pour mettre � jour les attributs x,y,z et angle.
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		long currTime = System.currentTimeMillis();
		
		float values[] = event.values; 
		
		switch (event.sensor.getType()) {
		
			case Sensor.TYPE_ACCELEROMETER :
				if (currTime - lastAccelerometerUpdate < SENSOR_REFRESH_MS)
					break;
				lastAccelerometerUpdate = System.currentTimeMillis();
				
				x = values[0];
				y = values[1];
				z = values[2];
				
				break;
				
			case Sensor.TYPE_ORIENTATION :
				
				if (currTime - lastOrientationUpdate < SENSOR_REFRESH_MS)
					break;
				lastOrientationUpdate = System.currentTimeMillis();
				
				angle = values[0];
				break;
			
		} // end-switch
		
		refreshLayout();
		
	}

	public float pos_x()
	{
		return x;
	}
	
	public float pos_y()
	{
		return y;
	}
	
	public float pos_z()
	{
		return z;
	}
	
	public float pos_angle()
	{
		return angle;
	}
}
