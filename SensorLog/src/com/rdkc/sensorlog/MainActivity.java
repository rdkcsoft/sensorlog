package com.rdkc.sensorlog;



import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rdkc.sensorlog.model.LogEntry;


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
		ParseObject.registerSubclass(LogEntry.class);
		Parse.initialize(this, PARSE_COM_APPLICATION_ID, PARSE_COM_CLIENT_KEY);
	}

	private void uploadAllFiles() {
		File homeDir = getFilesDir();
		
		File[] allFiles = homeDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith(LoggingService.ACCEL_PREFIX) || filename.startsWith(LoggingService.GYRO_PREFIX);
			}
		});

		for(final File f : allFiles) {
			final String fileName = f.getName();
			
			// Only upload offline only files
			ParseQuery<LogEntry> query = ParseQuery.getQuery(LogEntry.class);
			query.whereEqualTo("name", fileName);
			query.findInBackground(new FindCallback<LogEntry>() {
			  @Override
			  public void done(List<LogEntry> results, ParseException ex) {
				  if(ex == null) {
					  if(results.isEmpty()) {
						  uploadFile(f);
					  } else {
						  // Don't upload existing files
						  Log.i("info", "Skipping already uploaded file " + fileName);
					  }
				  } else {
					  logError(ex);
				  }
			  }
			});
			
			
			uploadFile(f);
		}
		
		Toast.makeText(this, allFiles.length > 0 ? "Upload initiated for " + allFiles.length + " files" : "Nothing to upload.", Toast.LENGTH_LONG).show();
	}

	public void uploadFile(final File f) {
		final String fileName = f.getName();
		
		try {
			final byte[] content = FileUtils.readFileToByteArray(f);
			final ParseFile file = new ParseFile(fileName, content);
			file.saveInBackground(new SaveCallback() {
				@Override public void done(ParseException ex) {
					if(ex == null) {
						LogEntry log = new LogEntry();
						log.setFile(fileName, content.length, file);
						log.saveInBackground(new SaveCallback() {
							@Override public void done(ParseException ex) {
								if(ex != null) {
									logError(ex);
								}
							}
						});
					} else {
						logError(ex);
					}
				}
			});
		} catch (IOException ex) {
			logError(ex);
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

	public void logError(Throwable ex) {
		Log.e("error", ex.getMessage(), ex);
		Toast.makeText(this, "Upload issues: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
	}
}