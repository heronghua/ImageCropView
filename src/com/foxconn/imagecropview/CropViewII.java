package com.foxconn.imagecropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CropViewII extends View{
	
	private static final String TAG = CropViewII.class.getSimpleName();

	private Bitmap backgroundBitmap = null;

	private float bgRatio;


	public CropViewII(Context context) {
		this(context,null);
	}
	
	public CropViewII(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void initial(Bitmap bmp) {
		setBackground(new BitmapDrawable(getResources(), backgroundBitmap=bmp));
		bgRatio = ((float)backgroundBitmap.getWidth())/backgroundBitmap.getHeight();
		Log.d(TAG+".INITAL", bgRatio+"");
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (backgroundBitmap != null) {
			int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			
			int width = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
			int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
			
			if (widthMode == MeasureSpec.EXACTLY&&heightMode !=MeasureSpec.EXACTLY) {
				height = (int) (width / bgRatio + 0.5f);
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			}
			
			if (widthMode != MeasureSpec.EXACTLY&&heightMode ==MeasureSpec.EXACTLY) {
				width = (int) (height * bgRatio+0.5f);
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			}
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//TODO 調用了兩次
		Log.d(TAG, "width:"+getMeasuredWidth()+" height:"+getMeasuredHeight());
		
		//TODO 思考 當父控件是ScrollView 的時候不起作用
	}

}
