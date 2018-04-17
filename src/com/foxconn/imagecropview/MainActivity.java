package com.foxconn.imagecropview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	
	private CropView cropView;
	
	private RelativeLayout resultRL;
	
	private ImageView resultIV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.cropView = (CropView) findViewById(R.id.crop);
		this.resultRL = (RelativeLayout) findViewById(R.id.rl_result);
		this.cropView.setResource(R.drawable.android);
		this.resultIV = (ImageView) findViewById(R.id.iv_result);
	
	}
	
	public void onOKClick(View view){
		this.resultRL.setVisibility(View.VISIBLE);
		this.resultIV.setImageBitmap(cropView.getCropBitmap());
	}

}
