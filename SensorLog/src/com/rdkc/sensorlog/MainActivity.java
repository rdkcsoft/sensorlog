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
import android.content.Intent;
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
 * error handling if service does not start because no sensors present
 */
public class MainActivity extends Activity {

	private Button button;

	private boolean recording = false;
	
	// Intent used for starting the service
	private Intent loggingServiceIntent;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        
        loggingServiceIntent = new Intent(getApplicationContext(),
    			LoggingService.class);
        
        // Set an OnClickListener on this Button
        // Called each time the user clicks the Button
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				recording = !recording;

				if(recording) {
					startRecording();
				}
				else {
					stopRecording();
				}
			}
		}); 

	}
	
	protected void startRecording() {
		
		try {
			startService(loggingServiceIntent);
			button.setText(getString(R.string.stop_recording_string));
		
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	protected void stopRecording() {
		stopService(loggingServiceIntent);
		button.setText(getString(R.string.start_recording_string));
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// see if service running, set flag and button state accordingly
		recording = LoggingService.isRunning();
		button.setText(LoggingService.isRunning() ? 
				getString(R.string.stop_recording_string) : 
				getString(R.string.start_recording_string));
	}

	@Override
	protected void onPause() {
		super.onPause();

		//recording = false;
		//stopRecording();
	}
}