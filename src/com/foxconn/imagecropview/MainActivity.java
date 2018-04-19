package com.foxconn.imagecropview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	private CropView cropView;
	
	
	private ImageView resultIV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.cropView = (CropView) findViewById(R.id.crop);
//		this.cropView.setRatio(2);
//		this.cropView.setScalable(true);
		this.resultIV = (ImageView) findViewById(R.id.iv_result);
		
	}
	
	public void onOKClick(View view){
		this.resultIV.setImageBitmap(cropView.getCropBitmap());
	}

}
