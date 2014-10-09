package com.rdkc.sensorlog;



import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;


public class MainActivity extends Activity {

	private Button btnControlRecording;
	private Button btnUpload;

	private boolean recording = false;
	
	// Intent used for starting the service
	private Intent loggingServiceIntent;
	
	private static final String PARSE_COM_APPLICATION_ID = "NHEp03LQenYAsrgbzd6pgVocx8t8LLiAgYw9xIGK";
	private static final String PARSE_COM_CLIENT_KEY = "OgpXUGJq0bgk76y4cBVewHmkYjyYhk0ocFh7Uh7r";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setupParse();
		
		setContentView(R.layout.activity_main);
        btnControlRecording = (Button) findViewById(R.id.btnControlRecording);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        
        btnUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				uploadAllFiles();
			}
		});
        
        loggingServiceIntent = new Intent(getApplicationContext(),
    			LoggingService.class);
        
        // Set an OnClickListener on this Button
        // Called each time the user clicks the Button
        btnControlRecording.setOnClickListener(new OnClickListener() {
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
	
	private void setupParse() {
		Parse.initialize(this, PARSE_COM_APPLICATION_ID, PARSE_COM_CLIENT_KEY);
		
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("foo", "bar");
		testObject.saveInBackground();
	}

	private void uploadAllFiles() {
		File homeDir = new File(".");
		
		File[] allFiles = homeDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith("accel_");
			}
		});
		
		for(File f : allFiles) {
			// Only upload offline only files
			
//			ParseFile file = new ParseFile("resume.txt", data);
			
		}
	}

	
	protected void startRecording() {
		
		try {
			startService(loggingServiceIntent);
			btnControlRecording.setText(getString(R.string.stop_recording_string));
		
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	protected void stopRecording() {
		stopService(loggingServiceIntent);
		btnControlRecording.setText(getString(R.string.start_recording_string));
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// see if service running, set flag and button state accordingly
		recording = LoggingService.isRunning();
		btnControlRecording.setText(LoggingService.isRunning() ? 
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