package com.rdkc.sensorlog.model;

import com.parse.ParseFile;
import com.parse.ParseObject;

public class AccelerometerReading extends ParseObject {
	public void setFile(String name, ParseFile f) {
		put("fileName", name);
		put("fileRef", f);
	}
	
}
