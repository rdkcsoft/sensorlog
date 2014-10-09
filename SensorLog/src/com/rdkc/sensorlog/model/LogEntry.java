package com.rdkc.sensorlog.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Log")
public class LogEntry extends ParseObject {
	public void setFile(String name, long size, ParseFile f) {
		put("name", name);
		put("content", f);
		put("size", size);
	}
	
}
