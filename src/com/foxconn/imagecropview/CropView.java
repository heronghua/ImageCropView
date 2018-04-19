package com.foxconn.imagecropview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CropView extends FrameLayout {

	private View cropShelter;

	private static final int NONE = 0;
	private static final int SCALE_TOP = 1;
	private static final int SCALE_BOTTOM = 6;
	private static final int SCALE_LEFT = 2;
	private static final int SCALE_RIGHT = 4;

	private static final int SCALE_LEFT_TOP = 3;
	private static final int SCALE_RIGHT_TOP = 5;
	private static final int SCALE_LEFT_BOTTOM = 8;
	private static final int SCALE_RIGHT_BOTTOM = 10;
	private static final int CENTER_MOVE = 26;
	
	private float mRatio = 0.5f;

	private static final int FINGER_RADIUS = 40;

	private int currentState = NONE;
	private boolean scalable;
	
	private Drawable cropShelterBg;

	public CropView(Context context) {
		super(context);
		init(context, null);
	}

	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropView);
		cropShelterBg=a.getDrawable(R.styleable.CropView_crop_shelter);
		a.recycle();
		
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.gravity = Gravity.CENTER;

		this.cropShelter = new View(context)
		{

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

		}
		;

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		if (getBackground()==null) {
			throw new IllegalArgumentException("CropView's backgroud could not be empty!");
		}
		
		this.cropShelter.setBackgroundDrawable(cropShelterBg);
		Bitmap bitmap = ((BitmapDrawable)cropShelter.getBackground()).getBitmap();
		setRatio(bitmap.getWidth()/bitmap.getHeight());
		
		LayoutParams lp = new LayoutParams(bitmap.getWidth(), LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		this.addView(cropShelter, lp);
	}
	
	private int downX,downY,preMoveX,preMoveY;
	private boolean actionMoved;
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Rect outerRect = new Rect(cropShelter.getLeft() - FINGER_RADIUS, cropShelter.getTop() - FINGER_RADIUS,
				cropShelter.getRight() + FINGER_RADIUS, cropShelter.getBottom() + FINGER_RADIUS);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			actionMoved = false;
			downX = (int) event.getX();
			downY = (int) event.getY();
			
			this.currentState = NONE;
			if (outerRect.contains(downX, downY)) {
				if (Math.abs(downY - cropShelter.getTop()) <= FINGER_RADIUS) {
					this.currentState += SCALE_TOP;
				}

				if (Math.abs(downY - cropShelter.getBottom()) <= FINGER_RADIUS) {
					this.currentState += SCALE_BOTTOM;
				}

				if (Math.abs(downX - cropShelter.getLeft()) <= FINGER_RADIUS) {
					this.currentState += SCALE_LEFT;
				}

				if (Math.abs(downX - cropShelter.getRight()) <= FINGER_RADIUS) {
					this.currentState += SCALE_RIGHT;
				}
				
				Rect innerRect = new Rect(cropShelter.getLeft() + FINGER_RADIUS, cropShelter.getTop() + FINGER_RADIUS,
						cropShelter.getRight() - FINGER_RADIUS, cropShelter.getBottom() - FINGER_RADIUS);
				if (innerRect.contains(downX, downY)){
					this.currentState += CENTER_MOVE;
				}

			}

		}
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			actionMoved = true;
			
			//點幾點在周圍，拉伸裁剪框
			if (scalable) {
				scale(event);
			}
			
			//點幾點在中間 移動裁剪框
			if(currentState == CENTER_MOVE){
				if (preMoveX==0||preMoveY==0) {
					
					preMoveX=downX;
					preMoveY = downY;
				}
				
				float deltX=event.getX()-preMoveX;
				float deltY=event.getY()-preMoveY;
				
				cropShelter.setLeft((int) (cropShelter.getLeft()+deltX));
				cropShelter.setTop((int) (cropShelter.getTop()+deltY));
				
				
				
				cropShelter.setRight((int) (cropShelter.getRight()+deltX));
				cropShelter.setBottom((int) (cropShelter.getBottom()+deltY));
				
				
				//限制裁剪框只能在圖片裡面移動
				if (cropShelter.getLeft()<0) {
					cropShelter.setLeft(0);
					cropShelter.setRight(cropShelter.getLeft()+cropShelter.getMeasuredWidth());
				}
				
				if (cropShelter.getRight()>getMeasuredWidth()) {
					cropShelter.setRight(getMeasuredWidth());
					cropShelter.setLeft(cropShelter.getRight()-cropShelter.getMeasuredWidth());
				}
				
				
				if (cropShelter.getTop()<0) {
					cropShelter.setTop(0);
					cropShelter.setBottom(cropShelter.getTop()+cropShelter.getMeasuredHeight());
				}
				Log.d("sdfasdfa", getMeasuredHeight()+":"+getMeasuredHeight());
				if (cropShelter.getBottom()>getMeasuredHeight()) {
					cropShelter.setBottom(getMeasuredHeight());
					cropShelter.setTop(cropShelter.getBottom()-cropShelter.getMeasuredHeight());
				}
				
				if (cropShelter.getBottom()>getMeasuredHeight()) {
					cropShelter.setBottom(getMeasuredHeight());
					cropShelter.setTop(cropShelter.getBottom()-cropShelter.getMeasuredHeight());
				}
				
				
				preMoveX=(int) event.getX();
				preMoveY=(int) event.getY();
			}
			
		}
		
		if (MotionEvent.ACTION_UP==event.getAction()) {
			if (!actionMoved&&Math.abs(downX-event.getX())<10) {
				performClick();
			}
			preMoveX = 0 ;
			preMoveY = 0 ;
		}

		return true;
	}
	
	public void setScalable(boolean scalable) {
		this.scalable = scalable;
	}
	
	public void setRatio(float mRatio) {
		this.mRatio = mRatio;
		invalidate();
	}

	private void scale(MotionEvent event) {
		if (currentState==SCALE_LEFT_TOP) {
			cropShelter.setLeft((int)event.getX());
			cropShelter.setTop((int) (cropShelter.getBottom()-((cropShelter.getRight()-cropShelter.getLeft())/mRatio)+0.5f));
		}
		
		if (currentState==SCALE_RIGHT_TOP) {
			cropShelter.setRight((int)event.getX());
			cropShelter.setTop((int) (cropShelter.getBottom()-((cropShelter.getRight()-cropShelter.getLeft())/mRatio)+0.5f));
		}
		
		if (currentState==SCALE_LEFT_BOTTOM) {
			cropShelter.setLeft((int)event.getX());
			cropShelter.setBottom((int) (cropShelter.getTop()+((cropShelter.getRight()-cropShelter.getLeft())/mRatio)+0.5f));
		}
		
		if (currentState==SCALE_RIGHT_BOTTOM) {
			cropShelter.setRight((int)event.getX());
			cropShelter.setBottom((int) (cropShelter.getTop()+((cropShelter.getRight()-cropShelter.getLeft())/mRatio)+0.5f));
		}
		
		if (currentState==SCALE_LEFT) {
			int heightAfter=(int) ((cropShelter.getRight()-event.getX())/mRatio+0.5);
			int heigtBefore = cropShelter.getBottom() - cropShelter.getTop();
			int deltY= (int) ((heightAfter-heigtBefore)/2f+0.5);
			cropShelter.setTop(cropShelter.getTop()-deltY);
			cropShelter.setBottom((int) (cropShelter.getBottom()+deltY));
			cropShelter.setLeft((int)event.getX());
		}
		
		if (currentState==SCALE_RIGHT) {
			int heightAfter=(int) ((event.getX()-cropShelter.getLeft())/mRatio+0.5);
			int heigtBefore = cropShelter.getBottom() - cropShelter.getTop();
			int deltY= (int) ((heightAfter-heigtBefore)/2f+0.5);
			cropShelter.setTop(cropShelter.getTop()-deltY);
			cropShelter.setBottom((int) (cropShelter.getBottom()+deltY));
			cropShelter.setRight((int)event.getX());
		}
		
		if (currentState==SCALE_TOP) {
			int widthAfter=(int) ((cropShelter.getBottom()-event.getY())*mRatio+0.5);
			int widthBefore = cropShelter.getRight() - cropShelter.getLeft();
			int deltY= (int) ((widthAfter-widthBefore)/2f+0.5);
			cropShelter.setLeft(cropShelter.getLeft()-deltY);
			cropShelter.setRight((int) (cropShelter.getRight()+deltY));
			cropShelter.setTop((int)event.getY());
		}
		
		if (currentState==SCALE_BOTTOM) {
			int widthAfter=(int) ((event.getY()-cropShelter.getTop())*mRatio+0.5);
			int widthBefore = cropShelter.getRight() - cropShelter.getLeft();
			int deltY= (int) ((widthAfter-widthBefore)/2f+0.5);
			cropShelter.setLeft(cropShelter.getLeft()-deltY);
			cropShelter.setRight((int) (cropShelter.getRight()+deltY));
			cropShelter.setBottom((int)event.getY());
		}
	}
	
	public Bitmap getCropBitmap() {
		Bitmap bitmap = ((BitmapDrawable)getBackground()).getBitmap();
        return Bitmap.createBitmap(bitmap,cropShelter.getLeft(),cropShelter.getTop(), 
        		cropShelter.getMeasuredWidth(),
        		cropShelter.getMeasuredHeight());
    }  
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Bitmap bmp=((BitmapDrawable)getBackground()).getBitmap();
		setMeasuredDimension(bmp.getWidth(), bmp.getHeight());
	}

}
