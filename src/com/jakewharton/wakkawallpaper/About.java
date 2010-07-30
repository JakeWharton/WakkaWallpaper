package com.jakewharton.wakkawallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class About extends Activity {
	public static final String EXTRA_FILENAME = "filename";
	public static final String EXTRA_TITLE = "title";
	private static final char NEWLINE = '\n';
	private static final String ERROR = "Failed to load the file from assets.";
	private static final String MIME_TYPE = "text/html";
	private static final String ENCODING = "utf8";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        StringBuffer content = new StringBuffer();
        
        try {
        	//Load entire about plain text from asset
			BufferedReader about = new BufferedReader(new InputStreamReader(this.getAssets().open(this.getIntent().getStringExtra(About.EXTRA_FILENAME))));
			String data;
			while ((data = about.readLine()) != null) {
				content.append(data);
				content.append(About.NEWLINE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			content.append(About.ERROR);
		}
		
		this.setTitle(this.getIntent().getStringExtra(About.EXTRA_TITLE));
		
		//Put text into layout
        final WebView view = new WebView(this);
		view.loadData(content.toString(), About.MIME_TYPE, About.ENCODING);
		
		this.setContentView(view);
    }
}
