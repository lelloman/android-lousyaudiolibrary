package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class VolumeView extends View implements View.OnTouchListener, VolumeReader.OnVolumeReadListener {

	public interface OnClickListener {
		void onClick(VolumeView volumeView, double percentX);
	}

	public static final int K = 4;
	private static final Object BITMAP_LOCK = 0xb00b5;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap bitmap;
	private Canvas canvas;
	private Rect srcRect, dstRect;

	private double cursor = 0;
	private OnClickListener onClickListener;
	private VolumeReader volumeReader;

	private int minHeight, maxHeight;
	private int zoomLevel;

	public VolumeView(Context context) {
		this(context, null);
	}

	public VolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);

		cursorPaint.setColor(Color.YELLOW);
		cursorPaint.setStyle(Paint.Style.STROKE);
		cursorPaint.setStrokeWidth(2*getResources().getDisplayMetrics().density);
		dstRect = new Rect(0, 0, 1, 1);
		setOnTouchListener(this);
	}

	public void setVolumeReader(VolumeReader volumeReader){
		this.volumeReader = volumeReader;
		volumeReader.setOnVolumeReadListener(this);
		selectZoomLevel(getWidth());
		if(bitmap != null){
			drawBitmap();
		}
	}

	public void setOnClickListener(OnClickListener l) {
		this.onClickListener = l;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (w == 0 || h == 0) return;

		int height = getHeight();
		minHeight = (int) (height * .05f);
		maxHeight = (int) (height * .95f);

		synchronized (BITMAP_LOCK) {
			if (bitmap != null) {
				bitmap.recycle();
			}

			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			srcRect = new Rect(0, 0, w, h);
		}
		selectZoomLevel(w);
		drawBitmap();
	}

	private void selectZoomLevel(int width){
		if(volumeReader == null) return;
		width /= K;

		int[] levels = volumeReader.getZoomLevels();
		zoomLevel = levels.length-1;

		for(int i = 0; i< levels.length; i++){
			if(levels[i] >= width){
				this.zoomLevel = i;
				break;
			}
		}
	}

	private void drawBitmap() {

		synchronized (BITMAP_LOCK){
			int i = 0;
			Double d = volumeReader.getVolume(zoomLevel, i);
			double dx = getWidth() / (double) volumeReader.getVolumeLength(zoomLevel);
			while(d != null){
				double x = dx * i++;
				drawFrame((int) x, d);
				d = volumeReader.getVolume(zoomLevel, i);
			}
		}
	}

	@Override
	public void onNewFrame(int zoomLevel, int frameIndex, int totFrames, Double value) {

		if(canvas == null || zoomLevel != this.zoomLevel) return;

		double dx = getWidth() / (double) volumeReader.getVolumeLength(zoomLevel);
		drawFrame((int) (frameIndex*dx), value);

		postInvalidate();
	}

	private void drawFrame(int x, double value){
		int height = getHeight();

		int barLength = minHeight + (int) (value * maxHeight);
		int d = (height - barLength) / 2;
		canvas.drawLine(x, height - d, x, d, paint);
	}

	public void setCursor(double d){
		this.cursor = d;
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(0xff888888);

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (bitmap != null) {
			dstRect.set(0, 0, width, height);
			canvas.drawBitmap(bitmap, srcRect, dstRect, null);
		}

		int x = (int) (width * cursor);
		canvas.drawLine(x,height,x,0,cursorPaint);

	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {

		int action = motionEvent.getAction();

		switch (action){
			case MotionEvent.ACTION_DOWN:
				if(onClickListener != null) {
					double x = motionEvent.getX() / getWidth();
					onClickListener.onClick(this, x);
					return true;
				}
				break;
		}

		return false;
	}
}
