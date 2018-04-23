package com.foxconn.imagecropview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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

	private static final int MINIMUS_WIDTH = 200;

	// width/height
	private float bgRatio,shelterRatio;
	private static final int FINGER_RADIUS = 20;
	private int currentState = NONE;
	private boolean scalable;

	private Rect cropShelterRect, shelterDrawableBitmapRect;

	private Bitmap shelterDrawableBitmap, backgroundBitmap, resultBitmap;

	private Paint paint;

	private int resutlWidth, resultHeight;

	private boolean scaleSmall = false;

	public CropViewI(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropView);
		shelterDrawableBitmap = ((BitmapDrawable) a.getDrawable(R.styleable.CropView_crop_shelter)).getBitmap();
		a.recycle();

		shelterRatio = shelterDrawableBitmap.getWidth() / shelterDrawableBitmap.getHeight();
		shelterDrawableBitmapRect = new Rect(0, 0, shelterDrawableBitmap.getWidth(), shelterDrawableBitmap.getHeight());
		resutlWidth = shelterDrawableBitmap.getWidth();
		resultHeight = shelterDrawableBitmap.getHeight();

		paint = new Paint();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public void setBackground(Drawable background) {
		super.setBackground(background);
		backgroundBitmap = ((BitmapDrawable) background).getBitmap();
		bgRatio = backgroundBitmap.getWidth()/backgroundBitmap.getHeight();
		
//		if (bgRatio>1) {
//			resutlWidth = (int) (backgroundBitmap.getHeight()/shelterRatio);
//			cropShelterRect = new Rect(backgroundBitmap.getWidth()-resutlWidth);
//		}
		
		cropShelterRect = new Rect((backgroundBitmap.getWidth() - shelterDrawableBitmap.getWidth()) / 2,
				(backgroundBitmap.getHeight() - shelterDrawableBitmap.getHeight()) / 2,
				(backgroundBitmap.getWidth() + shelterDrawableBitmap.getWidth()) / 2,
				(backgroundBitmap.getHeight() + shelterDrawableBitmap.getHeight()) / 2);
		Log.d("Crop", cropShelterRect + "");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(shelterDrawableBitmap, shelterDrawableBitmapRect, cropShelterRect, paint);
	}

	public Bitmap getCropBitmap() {

		Log.e("Exception",
				cropShelterRect.left + ":" + cropShelterRect.top + ":" + (cropShelterRect.right - cropShelterRect.left)
						+ ":" + (cropShelterRect.bottom - cropShelterRect.top));

		resultBitmap = Bitmap.createBitmap(backgroundBitmap, cropShelterRect.left, cropShelterRect.top,
				cropShelterRect.right - cropShelterRect.left, cropShelterRect.bottom - cropShelterRect.top);

		System.gc();
		return resultBitmap;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		if (backgroundBitmap != null) {
			if (!backgroundBitmap.isRecycled()) {
				backgroundBitmap.recycle();
			}
		}

		if (shelterDrawableBitmap != null) {
			if (!shelterDrawableBitmap.isRecycled()) {
				shelterDrawableBitmap.recycle();
			}
		}

	}

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

		if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY && bgRatio != 0.0f) {
			// 判断条件为，宽度模式为Exactly，也就是填充父窗体或者是指定宽度；
			// 且高度模式不是Exaclty，代表设置的既不是fill_parent也不是具体的值，于是需要具体测量
			// 且图片的宽高比已经赋值完毕，不再是0.0f
			// 表示宽度确定，要测量高度
			height = (int) (width / bgRatio + 0.5f);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		} else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && bgRatio != 0.0f) {
			// 判断条件跟上面的相反，宽度方向和高度方向的条件互换
			// 表示高度确定，要测量宽度
			width = (int) (height * bgRatio + 0.5f);

			widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	private int downX, downY, preMoveX, preMoveY;
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
				if (innerRect.contains(downX, downY)) {
					this.currentState += CENTER_MOVE;
				}

			}
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			actionMoved = true;

			// 點幾點在周圍，拉伸裁剪框
			if (scalable && currentState != CENTER_MOVE) {

				judgeIsScaleSmall(event);

				// 限制縮放大小不能小於MINIMUS_WIDTH
				if (scaleSmall && cropShelterRect.right - cropShelterRect.left <= MINIMUS_WIDTH) {
					// do not scale
				} else {

					scale(event);
				}

			}

			// 點幾點在中間 移動裁剪框
			if (currentState == CENTER_MOVE) {
				if (preMoveX == 0 || preMoveY == 0) {

					preMoveX = downX;
					preMoveY = downY;
				}

				float deltX = event.getX() - preMoveX;
				float deltY = event.getY() - preMoveY;

				cropShelterRect.left += deltX;
				cropShelterRect.top += deltY;
				cropShelterRect.right += deltX;
				cropShelterRect.bottom += deltY;

				handleMoveOutBunds();
				preMoveX = (int) event.getX();
				preMoveY = (int) event.getY();
			}

			invalidate();

		}

		if (MotionEvent.ACTION_UP == event.getAction()) {
			if (!actionMoved && Math.abs(downX - event.getX()) < 10) {
				performClick();
			}
			preMoveX = 0;
			preMoveY = 0;
		}

		return true;
	}

	private void judgeIsScaleSmall(MotionEvent event) {
		if (currentState == SCALE_LEFT_TOP || currentState == SCALE_LEFT || currentState == SCALE_LEFT_BOTTOM) {
			scaleSmall = event.getX() > cropShelterRect.left;
		}

		if (currentState == SCALE_RIGHT_TOP || currentState == SCALE_RIGHT || currentState == SCALE_RIGHT_BOTTOM) {
			scaleSmall = event.getX() < cropShelterRect.right;
		}

		if (currentState == SCALE_TOP) {
			scaleSmall = event.getY() > cropShelterRect.top;
		}

		if (currentState == SCALE_BOTTOM) {
			scaleSmall = event.getY() < cropShelterRect.bottom;
		}

	}

	private void handleMoveOutBunds() {
		// 限制裁剪框只能在圖片裡面移動
		if (cropShelterRect.left < 0) {
			cropShelterRect.left = 0;
			cropShelterRect.right = cropShelterRect.left + resutlWidth;
		}

		if (cropShelterRect.right > backgroundBitmap.getWidth()) {
			cropShelterRect.right = backgroundBitmap.getWidth();
			cropShelterRect.left = cropShelterRect.right - resutlWidth;

		}

		if (cropShelterRect.top < 0) {
			cropShelterRect.top = 0;
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;

		}

		if (cropShelterRect.bottom > backgroundBitmap.getHeight()) {
			cropShelterRect.bottom = backgroundBitmap.getHeight();
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;

		}
	}

	private void test(String state) {
		if (cropShelterRect.left < 0 || cropShelterRect.right > backgroundBitmap.getWidth() || cropShelterRect.top < 0
				|| cropShelterRect.bottom > backgroundBitmap.getHeight()) {
			Log.d("exception", "============" + state + "===============");
			Log.e("Exception",
					cropShelterRect.left + ":" + cropShelterRect.top + ":"
							+ (cropShelterRect.right - cropShelterRect.left) + ":"
							+ (cropShelterRect.bottom - cropShelterRect.top));

			Log.d("exception", "============" + state + "===============");

		}

	}

	private void scale(MotionEvent event) {

		// 手指移除圖片區域外，不再拉伸
		if (event.getX() < 0 || event.getX() > backgroundBitmap.getWidth()
				|| event.getY() > backgroundBitmap.getHeight() || event.getY() < 0) {
			return;
		}

		if (currentState == SCALE_LEFT_TOP) {

			cropShelterRect.left = (int) event.getX();

			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (resutlWidth / shelterRatio);
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;

			test("SCALE_LEFT_TOP");
		}

		if (currentState == SCALE_RIGHT_TOP) {
			cropShelterRect.right = (int) event.getX();
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (resutlWidth / shelterRatio);
			cropShelterRect.top = cropShelterRect.bottom - resultHeight;

			test("SCALE_RIGHT_TOP");
		}

		if (currentState == SCALE_LEFT_BOTTOM) {
			cropShelterRect.left = (int) event.getX();
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (resutlWidth / shelterRatio);
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;

			test("SCALE_LEFT_BOTTOM");
		}

		if (currentState == SCALE_RIGHT_BOTTOM) {
			cropShelterRect.right = (int) event.getX();
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (resutlWidth / shelterRatio);
			cropShelterRect.bottom = cropShelterRect.top + resultHeight;

			test("SCALE_RIGHT_BOTTOM");
		}

		if (currentState == SCALE_LEFT) {
			int heigtBefore = cropShelterRect.bottom - cropShelterRect.top;
			int predictLeft = (int) event.getX();
			int predictWidth = (int) (cropShelterRect.right - predictLeft);
			int predictHeight = (int) (predictWidth / shelterRatio);

			int predictDeltY = (int) ((predictHeight - heigtBefore) / 2f);
			int predictTop = cropShelterRect.top - predictDeltY;
			int predictBottom = cropShelterRect.bottom + predictDeltY;
			if (predictTop <= 0) {
				predictTop = 0;
				predictBottom = cropShelterRect.bottom + 2 * predictDeltY;

				if (predictBottom > backgroundBitmap.getHeight()) {
					predictBottom = backgroundBitmap.getHeight();
					predictLeft = cropShelterRect.left;
				}
			}

			if (predictBottom >= backgroundBitmap.getHeight()) {
				predictBottom = backgroundBitmap.getHeight();
				predictTop = cropShelterRect.top - 2 * predictDeltY;

				if (predictTop <= 0) {
					predictTop = 0;
					predictLeft = cropShelterRect.left;
				}
			}

			cropShelterRect.left = predictLeft;
			cropShelterRect.top = predictTop;
			cropShelterRect.bottom = predictBottom;
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (cropShelterRect.bottom - cropShelterRect.top);

			test("SCALE_LEFT");
		}

		if (currentState == SCALE_RIGHT) {
			int heigtBefore = cropShelterRect.bottom - cropShelterRect.top;
			int predictRight = (int) event.getX();
			int predictWidth = (int) (predictRight - cropShelterRect.left);
			int predictHeight = (int) (predictWidth / shelterRatio);

			int predictDeltY = (int) ((predictHeight - heigtBefore) / 2f);
			int predictTop = cropShelterRect.top - predictDeltY;
			int predictBottom = cropShelterRect.bottom + predictDeltY;
			if (predictTop <= 0) {
				predictTop = 0;
				predictBottom = cropShelterRect.bottom + 2 * predictDeltY;

				if (predictBottom > backgroundBitmap.getHeight()) {
					predictBottom = backgroundBitmap.getHeight();
					predictRight = cropShelterRect.right;
				}
			}

			if (predictBottom >= backgroundBitmap.getHeight()) {
				predictBottom = backgroundBitmap.getHeight();
				predictTop = cropShelterRect.top - 2 * predictDeltY;

				if (predictTop <= 0) {
					predictTop = 0;
					predictRight = cropShelterRect.right;
				}
			}

			cropShelterRect.right = predictRight;
			cropShelterRect.top = predictTop;
			cropShelterRect.bottom = predictBottom;
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (cropShelterRect.bottom - cropShelterRect.top);

			test("SCALE_RIGHT");
		}

		if (currentState == SCALE_TOP) {
			int widthBefore = cropShelterRect.right - cropShelterRect.left;
			int predictTop = (int) event.getY();
			int predictHeight = (int) (cropShelterRect.bottom - predictTop);
			int predictWidth = (int) (predictHeight * shelterRatio);

			int predictDeltX = (int) ((predictWidth - widthBefore) / 2f);
			int predictLeft = cropShelterRect.left - predictDeltX;
			int predictRight = cropShelterRect.right + predictDeltX;
			if (predictLeft <= 0) {
				predictLeft = 0;
				predictRight = cropShelterRect.right + 2 * predictDeltX;

				if (predictRight > backgroundBitmap.getWidth()) {
					predictRight = backgroundBitmap.getWidth();
					predictTop = cropShelterRect.top;
				}
			}

			if (predictRight >= backgroundBitmap.getWidth()) {
				predictRight = backgroundBitmap.getWidth();
				predictLeft = cropShelterRect.left - 2 * predictDeltX;

				if (predictLeft <= 0) {
					predictLeft = 0;
					predictTop = cropShelterRect.top;
				}
			}

			cropShelterRect.right = predictRight;
			cropShelterRect.top = predictTop;
			cropShelterRect.left = predictLeft;
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (cropShelterRect.bottom - cropShelterRect.top);

			test("SCALE_TOP");
		}

		if (currentState == SCALE_BOTTOM) {
			int widthBefore = cropShelterRect.right - cropShelterRect.left;
			int predictBottom = (int) event.getY();
			int predictHeight = (int) (predictBottom - cropShelterRect.top);
			int predictWidth = (int) (predictHeight * shelterRatio);

			int predictDeltX = (int) ((predictWidth - widthBefore) / 2f);
			int predictLeft = cropShelterRect.left - predictDeltX;
			int predictRight = cropShelterRect.right + predictDeltX;
			if (predictLeft <= 0) {
				predictLeft = 0;
				predictRight = cropShelterRect.right + 2 * predictDeltX;

				if (predictRight > backgroundBitmap.getWidth()) {
					predictRight = backgroundBitmap.getWidth();
					predictBottom = cropShelterRect.bottom;
				}
			}

			if (predictRight >= backgroundBitmap.getWidth()) {
				predictRight = backgroundBitmap.getWidth();
				predictLeft = cropShelterRect.left - 2 * predictDeltX;

				if (predictLeft <= 0) {
					predictLeft = 0;
					predictBottom = cropShelterRect.bottom;
				}
			}

			cropShelterRect.right = predictRight;
			cropShelterRect.bottom = predictBottom;
			cropShelterRect.left = predictLeft;
			resutlWidth = (int) (cropShelterRect.right - cropShelterRect.left);
			resultHeight = (int) (cropShelterRect.bottom - cropShelterRect.top);

			test("SCALE_BOTTOM");
		}
	}

	public void setScalable(boolean scalable) {
		this.scalable = scalable;
	}

	public void setShelterRatio(float ratio) {
		this.shelterRatio = ratio;
		invalidate();
	}

}
