package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class VolumeView extends View implements VolumeReader.OnVolumeReadListener {

	public interface OnClickListener {
		void onClick(VolumeView volumeView, float percentX);
	}

	public static final int K = 4;
	private static final Object BITMAP_LOCK = 0xb00b5;

	private Paint paint = new Paint();
	private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap bitmap;
	private Canvas canvas;
	private Rect srcRect, dstRect;
	private Path framePath = new Path();

	private float cursor = 0;
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

		cursorPaint.setColor(Color.YELLOW);
		cursorPaint.setStyle(Paint.Style.STROKE);
		cursorPaint.setStrokeWidth(2*getResources().getDisplayMetrics().density);
		dstRect = new Rect(0, 0, 1, 1);
	}

	public void setVolumeReader(VolumeReader volumeReader){
		this.volumeReader = volumeReader;
		volumeReader.setOnVolumeReadListener(this);
		selectZoomLevel(getWidth(), getHeight());
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


		selectZoomLevel(w, h);
		drawBitmap();
	}

	private void selectZoomLevel(int width, int height){
		if(volumeReader == null || width < 1 || height < 1) return;
		width /= K;

		int[] levels = volumeReader.getZoomLevels();
		zoomLevel = levels.length-1;

		for(int i = 0; i< levels.length; i++){
			if(levels[i] >= width){
				this.zoomLevel = i;
				break;
			}
		}

		synchronized (BITMAP_LOCK) {
			if (bitmap != null) {
				bitmap.recycle();
			}

			bitmap = Bitmap.createBitmap(levels[zoomLevel], height, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			srcRect = new Rect(0, 0, levels[zoomLevel], height);
		}
	}

	private void drawBitmap() {
		if(volumeReader == null) return;

		synchronized (BITMAP_LOCK){
			int i = 0;
			Double value = volumeReader.getVolume(zoomLevel, i);

			while(value != null){
				drawFrame(i++, value);
				value = volumeReader.getVolume(zoomLevel, i);

			}
		}
	}

	@Override
	public void onNewFrame(int zoomLevel, int frameIndex, int totFrames, Double value) {

		if(canvas == null || zoomLevel != this.zoomLevel) return;

		drawFrame(frameIndex, value);

		postInvalidate();
	}

	private void drawFrame(int i1, Double value0){
		synchronized (BITMAP_LOCK) {

			if (value0 == null) return;

			int height = getHeight();

			int barLength0 = minHeight + (int) (value0 * maxHeight);
			int y0up = (height - barLength0) / 2;
			int y0down = height - y0up;

			float x0 = i1;

			canvas.drawLine(x0, y0up, x0, y0down, paint);
		}
	}

	public void setCursor(float d){
		this.cursor = d;
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(0xff888888);

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		synchronized (BITMAP_LOCK) {
			if (bitmap != null) {
				dstRect.set(0, 0, width, height);
				canvas.drawBitmap(bitmap, srcRect, dstRect, null);
			}
		}

		int x = (int) (width * cursor);
		canvas.drawLine(x,height,x,0,cursorPaint);

	}

	protected void onClick(float clickX){
		if(onClickListener != null){
			float x = clickX / getWidth();
			onClickListener.onClick(this, x);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {

		int action = motionEvent.getAction();

		switch (action){
			case MotionEvent.ACTION_DOWN:
				onClick(motionEvent.getX());
				return true;
		}

		return false;
	}

	public float getCursor(){
		return cursor;
	}
}
