package com.jakewharton.wakkawallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class About_Instructions extends Activity {
	private static final String FILENAME = "instructions.html";
	private static final char NEWLINE = '\n';
	private static final String ERROR = "Failed to load instructions from assets.";
	private static final String MIME_TYPE = "text/html";
	private static final String ENCODING = "utf8";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        StringBuffer content = new StringBuffer();
        
        try {
        	//Load entire about plain text from asset
			BufferedReader about = new BufferedReader(new InputStreamReader(this.getAssets().open(About_Instructions.FILENAME)));
			String data;
			while ((data = about.readLine()) != null) {
				content.append(data);
				content.append(About_Instructions.NEWLINE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			content.append(About_Instructions.ERROR);
		}
		
		//Put text into layout
        final WebView view = new WebView(this);
		view.loadData(content.toString(), About_Instructions.MIME_TYPE, About_Instructions.ENCODING);
		
		this.setContentView(view);
    }
}
