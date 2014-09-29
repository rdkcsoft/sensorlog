package com.rdkc.sensorlog;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/*
 * TODO
 * gyroscope data
 * gps data
 * "start recording/stop recording" button
 * write to csv
 */
public class MainActivity extends Activity implements
		SensorEventListener {

	// References to SensorManager and accelerometer

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private Button button;

	// Filtering constant

	private final float mAlpha = 0.8f;

	// Arrays for storing filtered values
	private float[] mGravity = new float[3];
	private float[] mAccel = new float[3];

	private boolean recording = false;

	private long lastUpdateAccel;
	private FileOutputStream fosAccel;
	private PrintWriter pwAccel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        final DateFormat sd = new SimpleDateFormat("yyMMdd_HHmmss");
        
        // Set an OnClickListener on this Button
        // Called each time the user clicks the Button
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				recording = !recording;

				if(recording) {
					startRecording("accel_" +sd.format(new Date()));
				}
				else {
					stopRecording();
				}
			}
		}); 


		// Get reference to SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Get reference to Accelerometer
		if (null == (mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)))
			finish();

	}
	
	protected void startRecording(String fileNameAccel) {
		
		try {
			fosAccel = openFileOutput(fileNameAccel, MODE_PRIVATE);
			pwAccel = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(fosAccel)));
			button.setText(getString(R.string.stop_recording_string));
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_UI);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			finish();
		}
	}
	
	protected void stopRecording() {
		pwAccel.close();
		button.setText(getString(R.string.start_recording_string));
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		recording = false;
		button.setText(getString(R.string.start_recording_string));
	}

	@Override
	protected void onPause() {
		super.onPause();

		recording = false;
		stopRecording();
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