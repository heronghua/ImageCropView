package com.foxconn.imagecropview;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

public class TestCropViewIIActivity extends Activity {
	
	private static final String TAG = TestCropViewIIActivity.class.getSimpleName();	
	private CropViewII cii;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_crop);
		this.cii = (CropViewII) findViewById(R.id.cii);
		
		this.cii.initial(BitmapFactory.decodeResource(getResources(), R.drawable.android));
		
	}
	
	
	
}
