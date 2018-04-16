package com.foxconn.imagecropview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class CropView extends FrameLayout {
	
	private ImageView cropShelter;
	
	private ImageView bgSource;
	
	private static final String SCALE_LEFT_TOP="scale_left_top";
	private static final String SCALE_RIGHT_TOP="scale_right_top";
	private static final String SCALE_LEFT_BOTTOM="scale_left_bottom";
	private static final String SCALE_RIGHT_BOTTOM="scale_right_bottom";
	
	
	private String currentState = SCALE_LEFT_TOP;
	

	public CropView(Context context) {
		super(context);
		init(context,null);
	}


	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	
	public void setResource(int drawable){
		this.bgSource.setImageResource(drawable);
	}

	private void init(Context context, AttributeSet attrs) {
		this.bgSource = new ImageView(context);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		lp.gravity = Gravity.CENTER;
		this.bgSource.setLayoutParams(lp);
		
		
		this.cropShelter = new ImageView(context){
			private float mRatio=0.5f;

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				// 父容器传过来的宽度方向上的模式
				int widthMode = MeasureSpec.getMode(widthMeasureSpec);
				// 父容器传过来的高度方向上的模式
				int heightMode = MeasureSpec.getMode(heightMeasureSpec);

				// 父容器传过来的宽度的值
				int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
				// 父容器传过来的高度的值
				int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingLeft() - getPaddingRight();

				if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY && mRatio != 0.0f) {
					// 判断条件为，宽度模式为Exactly，也就是填充父窗体或者是指定宽度；
					// 且高度模式不是Exaclty，代表设置的既不是fill_parent也不是具体的值，于是需要具体测量
					// 且图片的宽高比已经赋值完毕，不再是0.0f
					// 表示宽度确定，要测量高度
					height = (int) (width / mRatio + 0.5f);
					heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
				} else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && mRatio != 0.0f) {
					// 判断条件跟上面的相反，宽度方向和高度方向的条件互换
					// 表示高度确定，要测量宽度
					width = (int) (height * mRatio + 0.5f);

					widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
				}

				super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			}
			
		};
		
		
		
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.addView(bgSource);
		this.cropShelter.setImageResource(R.drawable.ic_crop_shelter);
		
		LayoutParams lp=new LayoutParams(400,LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		this.addView(cropShelter,lp);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float fingerRadius = getMeasuredWidth()/4f; 
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			if(Math.sqrt(Math.pow(event.getX()-cropShelter.getLeft(), 2)+Math.pow(event.getY()-cropShelter.getTop(), 2))<=fingerRadius){
				//leftTop
				toast("leftTop");
				this.currentState = SCALE_LEFT_TOP;
				
			}else if(Math.sqrt(Math.pow(event.getX()-cropShelter.getRight(), 2)+Math.pow(event.getY()-cropShelter.getTop(), 2))<=fingerRadius){
				//rightTop
				toast("rightTop");
				this.currentState = SCALE_LEFT_TOP;
			}else if(Math.sqrt(Math.pow(event.getX()-cropShelter.getLeft(), 2)+Math.pow(event.getY()-cropShelter.getBottom(), 2))<=fingerRadius){
				//leftBottom
				toast("leftBottom");
				this.currentState = SCALE_LEFT_TOP;
			}else if(Math.sqrt(Math.pow(event.getX()-cropShelter.getRight(), 2)+Math.pow(event.getY()-cropShelter.getBottom(), 2))<=fingerRadius){
				//rightBottom
				toast("rightBottom");
				this.currentState = SCALE_LEFT_TOP;
			}
//				else if(){
//				
//			}
		}
		
		return super.onTouchEvent(event);
	}
	
	private void toast(String msg){
		Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
	}

}
