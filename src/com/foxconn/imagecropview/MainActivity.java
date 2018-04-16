package com.foxconn.imagecropview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
	
	private CropView cropView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.cropView = (CropView) findViewById(R.id.crop);
		
		
		this.cropView.setResource(R.drawable.android);
	
	}
	
	

}
