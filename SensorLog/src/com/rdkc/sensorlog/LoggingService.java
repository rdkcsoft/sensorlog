package com.rdkc.sensorlog;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

public class LoggingService extends Service implements SensorEventListener {

	@SuppressWarnings("unused")
	private final String TAG = "LoggingService";

	private static final int NOTIFICATION_ID = 1;
	
	// References to SensorManager and accelerometer
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private long lastUpdateAccel;
	private FileOutputStream fosAccel;
	private PrintWriter pwAccel;
	
	private final DateFormat sd = new SimpleDateFormat("yyMMdd_HHmmss");

	// Filtering constant
	private final float mAlpha = 0.8f;

	// Arrays for storing filtered values
	private float[] mGravity = new float[3];
	private float[] mAccel = new float[3];

	private static boolean running = false;
	
	// https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
	public static boolean isRunning() {
		return running;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Get reference to SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get reference to Accelerometer
		if (null == (mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))) {
			throw new RuntimeException("no accelerometer found");
		}

		// Create a notification area notification so the user 
		// can get back to the client UI
		
		final Intent notificationIntent = new Intent(getApplicationContext(),
				MainActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		final Notification notification = new Notification.Builder(
				getApplicationContext())
				.setSmallIcon(android.R.drawable.ic_menu_info_details)
				.setOngoing(true).setContentTitle("Sensor Logging")
				.setContentText("Go to SensorLog")
				.setContentIntent(pendingIntent)
				.build();

		// Put this Service in a foreground state, so it won't 
		// readily be killed by the system  
		startForeground(NOTIFICATION_ID, notification);
		Log.d("====== service created", "debug");

	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {

		startRecording("accel_" +sd.format(new Date()));
		Log.d("====== service started", "debug");

		// Do not automatically restart this service if it is killed
		return this.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {

		stopRecording();
		Log.d("====== service stopped", "debug");
	}

	// Can't bind to this Service
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	protected void startRecording(String fileNameAccel) {
		
		try {
			fosAccel = openFileOutput(fileNameAccel, MODE_PRIVATE);
			pwAccel = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(fosAccel)));
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_UI);
			running = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			stopSelf();
		}
	}
	
	protected void stopRecording() {
		pwAccel.close();
		mSensorManager.unregisterListener(this);
		running = false;
	}

	// Process new reading
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			processAccelEvent(event);
		}
	}

	private void processAccelEvent(SensorEvent event) {
		long actualTime = System.currentTimeMillis();

		if (actualTime - lastUpdateAccel > 500) {

			lastUpdateAccel = actualTime;

			float rawX = event.values[0];
			float rawY = event.values[1];
			float rawZ = event.values[2];

			// Apply low-pass filter
			mGravity[0] = lowPass(rawX, mGravity[0]);
			mGravity[1] = lowPass(rawY, mGravity[1]);
			mGravity[2] = lowPass(rawZ, mGravity[2]);

			// Apply high-pass filter
			mAccel[0] = highPass(rawX, mGravity[0]);
			mAccel[1] = highPass(rawY, mGravity[1]);
			mAccel[2] = highPass(rawZ, mGravity[2]);

			pwAccel.println(actualTime + "," +
					String.valueOf(mAccel[0]) + "," +
					String.valueOf(mAccel[1]) + "," +
					String.valueOf(mAccel[2]));
		}
	}

	// Deemphasize transient forces
	private float lowPass(float current, float gravity) {
		return gravity * mAlpha + current * (1 - mAlpha);
	}

	// Deemphasize constant forces
	private float highPass(float current, float gravity) {
		return current - gravity;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// NA
	}
} 