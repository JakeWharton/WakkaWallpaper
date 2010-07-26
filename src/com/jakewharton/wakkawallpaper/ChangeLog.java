package com.jakewharton.wakkawallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ChangeLog extends Activity {
	private static final String FILENAME = "changelog.html";
	private static final char NEWLINE = '\n';
	private static final String ERROR = "Failed to load change log text from assets.";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create layout
        this.setContentView(R.layout.changelog);
        
        StringBuffer content = new StringBuffer();
        
        try {
        	//Load entire about plain text from asset
			BufferedReader about = new BufferedReader(new InputStreamReader(this.getAssets().open(ChangeLog.FILENAME)));
			String data;
			while ((data = about.readLine()) != null) {
				content.append(data);
				content.append(ChangeLog.NEWLINE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			content.append(ChangeLog.ERROR);
		}
		
		//Put text into layout
		((WebView)this.findViewById(R.id.content)).loadData(content.toString(), "text/html", "utf8");
    }
}
