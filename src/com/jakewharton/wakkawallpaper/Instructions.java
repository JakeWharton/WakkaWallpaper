package com.jakewharton.wakkawallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Instructions extends Activity {
	private static final String FILENAME = "instructions.txt";
	private static final char NEWLINE = '\n';
	private static final String ERROR = "Failed to load about text from assets.";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create layout
        this.setContentView(R.layout.instructions);
        
        StringBuffer content = new StringBuffer();
        
        try {
        	//Load entire about plain text from asset
			BufferedReader about = new BufferedReader(new InputStreamReader(this.getAssets().open(Instructions.FILENAME)));
			String data;
			while ((data = about.readLine()) != null) {
				content.append(data);
				content.append(Instructions.NEWLINE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			content.append(Instructions.ERROR);
		}
		
		//Put text into layout
		((TextView)this.findViewById(R.id.content)).setText(content.toString());
    }
}
