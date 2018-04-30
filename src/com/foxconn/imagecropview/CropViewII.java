package com.foxconn.imagecropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CropViewII extends View{
	
	private static final String TAG = CropViewII.class.getSimpleName();

	private Bitmap backgroundBitmap;


	public CropViewII(Context context) {
		this(context,null);
	}
	
	public CropViewII(Context context, AttributeSet attrs) {
		super(context, attrs);
		backgroundBitmap = ((BitmapDrawable)getBackground()).getBitmap();
		if (backgroundBitmap==null) {
			Log.e(TAG+".constructor","back ground could not be null !");
			throw new IllegalArgumentException("back ground could not be null !");
		}
		//TODO decodeBitmap 是耗時操作怎樣和onMesure 通信
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int width = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		
		
		if (widthMode == MeasureSpec.EXACTLY&&heightMode !=MeasureSpec.EXACTLY) {
			
		}
		
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
