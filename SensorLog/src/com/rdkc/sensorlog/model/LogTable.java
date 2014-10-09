package com.rdkc.sensorlog.model;

import com.parse.ParseFile;
import com.parse.ParseObject;

public class LogTable extends ParseObject {
	public void setFile(String name, ParseFile f) {
		put("name", name);
		put("content", f);
	}
	
}
