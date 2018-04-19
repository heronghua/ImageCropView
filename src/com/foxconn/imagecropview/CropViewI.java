package com.foxconn.imagecropview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

//TODO bug 待修復
public class CropViewI extends View {
	
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
	
	//width/height
	private float mRatio = 2f;
	private static final int FINGER_RADIUS = 20;
	private int currentState = NONE;
	private boolean scalable;

	private Rect cropShelterRect,shelterDrawableBitmapRect;
	
	private Bitmap shelterDrawableBitmap , backgroundBitmap , resultBitmap;

	private Paint paint;
	
	private int resutlWidth,resultHeight;
	
	public CropViewI(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropView);
		shelterDrawableBitmap= ((BitmapDrawable)a.getDrawable(R.styleable.CropView_crop_shelter)).getBitmap();
		backgroundBitmap = ((BitmapDrawable)getBackground()).getBitmap();
		a.recycle();
		
		
		cropShelterRect = new Rect((backgroundBitmap.getWidth()-shelterDrawableBitmap.getWidth())/2,
				(backgroundBitmap.getHeight()-shelterDrawableBitmap.getHeight())/2, 
				(backgroundBitmap.getWidth()+shelterDrawableBitmap.getWidth())/2,
				(backgroundBitmap.getHeight()+shelterDrawableBitmap.getHeight())/2);
		Log.d("Crop", cropShelterRect+"");
		paint = new Paint();
		mRatio = shelterDrawableBitmap.getWidth()/shelterDrawableBitmap.getHeight();
		shelterDrawableBitmapRect = new Rect(0,0,shelterDrawableBitmap.getWidth(),shelterDrawableBitmap.getHeight());
		
	}
	
		
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawShelter(canvas);
	}
	

	private void drawShelter(Canvas canvas) {
		canvas.drawBitmap(shelterDrawableBitmap, shelterDrawableBitmapRect, 
				new Rect(cropShelterRect.left,cropShelterRect.top,cropShelterRect.right,cropShelterRect.bottom),  paint);
	}

	public Bitmap getCropBitmap() {
		
		if (resultBitmap!=null) {
			if(!resultBitmap.isRecycled()){
				resultBitmap.recycle();
			}
		}
		
		resultBitmap = Bitmap.createBitmap(backgroundBitmap,cropShelterRect.left,cropShelterRect.top, 
        		cropShelterRect.right-cropShelterRect.left,
        		cropShelterRect.bottom-cropShelterRect.top);
		
		
        return resultBitmap;
    }  
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		if (backgroundBitmap!=null) {
			if(!backgroundBitmap.isRecycled()){
				backgroundBitmap.recycle();
			}
		}
		
		if (shelterDrawableBitmap!=null) {
			if(!shelterDrawableBitmap.isRecycled()){
				shelterDrawableBitmap.recycle();
			}
		}
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Bitmap bmp=((BitmapDrawable)getBackground()).getBitmap();
		setMeasuredDimension(bmp.getWidth(), bmp.getHeight());
		
	}
	
	private int downX,downY,preMoveX,preMoveY;
	private boolean actionMoved;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Rect outerRect = new Rect(cropShelterRect.left - FINGER_RADIUS, cropShelterRect.top - FINGER_RADIUS,
				cropShelterRect.right + FINGER_RADIUS, cropShelterRect.bottom + FINGER_RADIUS);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			actionMoved = false;
			downX = (int) event.getX();
			downY = (int) event.getY();
			
			this.currentState = NONE;
			if (outerRect.contains(downX, downY)) {
				if (Math.abs(downY - cropShelterRect.top) <= FINGER_RADIUS) {
					this.currentState += SCALE_TOP;
				}

				if (Math.abs(downY - cropShelterRect.bottom) <= FINGER_RADIUS) {
					this.currentState += SCALE_BOTTOM;
				}

				if (Math.abs(downX - cropShelterRect.left) <= FINGER_RADIUS) {
					this.currentState += SCALE_LEFT;
				}

				if (Math.abs(downX - cropShelterRect.right) <= FINGER_RADIUS) {
					this.currentState += SCALE_RIGHT;
				}
				
				Rect innerRect = new Rect(cropShelterRect.left + FINGER_RADIUS, cropShelterRect.top + FINGER_RADIUS,
						cropShelterRect.right - FINGER_RADIUS, cropShelterRect.bottom - FINGER_RADIUS);
				if (innerRect.contains(downX, downY)){
					this.currentState += CENTER_MOVE;
				}

			}
			
//			Toast.makeText(getContext(), currentState+"", Toast.LENGTH_SHORT).show();
		}
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			actionMoved = true;
			
			//點幾點在周圍，拉伸裁剪框
			if (scalable&&currentState != CENTER_MOVE) {
				//TODO 
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
				
				cropShelterRect.left += deltX;
				cropShelterRect.top += deltY;
				cropShelterRect.right += deltX;
				cropShelterRect.bottom += deltY;
				
				handleOutBunds();
				
				preMoveX=(int) event.getX();
				preMoveY=(int) event.getY();
			}
			invalidate();
			
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
	
	private void handleOutBunds() {
		//限制裁剪框只能在圖片裡面移動
		if (cropShelterRect.left<0) {
			cropShelterRect.left=0;
			cropShelterRect.right=cropShelterRect.left + resutlWidth;
			
			if (cropShelterRect.right>backgroundBitmap.getWidth()) {
				cropShelterRect.right=backgroundBitmap.getWidth();
			}
		}
		
		if (cropShelterRect.right>backgroundBitmap.getWidth()) {
			cropShelterRect.right=backgroundBitmap.getWidth();
			cropShelterRect.left=cropShelterRect.right-resutlWidth;
			
			if (cropShelterRect.left<0) {
				cropShelterRect.left=0;
			}
		}
		
		
		if (cropShelterRect.top<0) {
			cropShelterRect.top=0;
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;
			
			if (cropShelterRect.bottom>backgroundBitmap.getHeight()) {
				cropShelterRect.bottom=backgroundBitmap.getHeight();
			}
		}
		
		if (cropShelterRect.bottom>backgroundBitmap.getHeight()) {
			cropShelterRect.bottom=backgroundBitmap.getHeight();
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;
			
			if (cropShelterRect.top<0) {
				cropShelterRect.top=0;
			}
		}
	}


	private void scale(MotionEvent event) {
		if (currentState==SCALE_LEFT_TOP) {
			cropShelterRect.left = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;
		}
		
		if (currentState==SCALE_RIGHT_TOP) {
			cropShelterRect.right = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;
		}
		
		if (currentState==SCALE_LEFT_BOTTOM) {
			cropShelterRect.left = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;
		}
		
		if (currentState==SCALE_RIGHT_BOTTOM) {
			cropShelterRect.right = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;
		}
		
		if (currentState==SCALE_LEFT) {
			int heigtBefore = cropShelterRect.bottom - cropShelterRect.top;
			
			cropShelterRect.left = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			
			int deltY= (int) ((resultHeight-heigtBefore)/2f+0.5);
			cropShelterRect.top = cropShelterRect.top - deltY;
			cropShelterRect.bottom = cropShelterRect.bottom + deltY;
		}
		
		if (currentState==SCALE_RIGHT) {
			int heigtBefore = cropShelterRect.bottom - cropShelterRect.top;
			
			cropShelterRect.right = (int)event.getX();
			resutlWidth = (int) (cropShelterRect.right-cropShelterRect.left+0.5);
			resultHeight = (int) (resutlWidth / mRatio+0.5);
			
			int deltY= (int) ((resultHeight-heigtBefore)/2f+0.5);
			cropShelterRect.top = cropShelterRect.top - deltY;
			cropShelterRect.bottom = cropShelterRect.bottom + deltY;
		}
		
		if (currentState==SCALE_TOP) {
			int widthBefore = cropShelterRect.right - cropShelterRect.left;
			
			cropShelterRect.top = (int)event.getY();
			resultHeight = (int) (cropShelterRect.bottom-cropShelterRect.top+0.5);
			resutlWidth = (int) (resultHeight * mRatio+0.5);
			
			int deltY= (int) ((resutlWidth-widthBefore)/2f+0.5);
			cropShelterRect.left=cropShelterRect.left-deltY;
			cropShelterRect.right=cropShelterRect.right+deltY;
		}
		
		if (currentState==SCALE_BOTTOM) {
			int widthBefore = cropShelterRect.right - cropShelterRect.left;
			
			cropShelterRect.bottom = (int)event.getY();
			resultHeight = (int) (cropShelterRect.bottom-cropShelterRect.top+0.5);
			resutlWidth = (int) (resultHeight * mRatio+0.5);
			
			int deltY= (int) ((resutlWidth-widthBefore)/2f+0.5);
			cropShelterRect.left=cropShelterRect.left-deltY;
			cropShelterRect.right=cropShelterRect.right+deltY;
		}
	}
	
	public void setScalable(boolean scalable) {
		this.scalable = scalable;
	}
	
	public void setRatio(float mRatio) {
		this.mRatio = mRatio;
		invalidate();
	}

}
