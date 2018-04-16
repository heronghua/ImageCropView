package com.foxconn.imagecropview;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private CropView cropView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.cropView = (CropView) findViewById(R.id.crop);
		
		
		this.cropView.setResource(R.drawable.android);
	
	}
	
	

}
