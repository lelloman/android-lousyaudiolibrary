package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.lelloman.lousyaudiolibrary.reader.volume.IVolumeReader;
import com.lelloman.lousyaudiolibrary.reader.volume.VolumeReader;


public class VolumeView extends View implements VolumeReader.OnVolumeReadListener {

	public interface VolumeViewListener {
		void onSingleTap(VolumeView volumeView, float percentX);

		void onDoubleTap(VolumeView volumeView, float percentX);

		void onWindowSelected(VolumeView volumeView, float start, float end);
	}

	public static final int K = 4;
	private static final Object BITMAP_LOCK = 0xb00b5;
	private static final int DEFAULT_BG_COLOR = 0xffbbbbbb;

	private Paint paint = new Paint();
	private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint windowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private Bitmap bitmap, windowBitmap;
	private Canvas canvas, windowCanvas;
	private Rect srcRect, dstRect;
	private Rect subWindowSrcRect, subWindowDstRect;

	private boolean dragging;
	private float draggingX;
	private Paint draggingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private boolean hasSecondCursor;

	private float cursor = 0;
	protected VolumeViewListener listener;
	private IVolumeReader volumeReader;

	private int minHeight, maxHeight;
	private int zoomLevel;
	private GestureDetector gestureDetector;
	private boolean canDrag = true;
	private boolean hasSubWindow = false;
	private float subWindowStart, subWindowEnd;

	public VolumeView(Context context) {
		this(context, null);
	}

	public VolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);

		windowPaint.setColor(Color.WHITE);
		windowPaint.setStyle(Paint.Style.STROKE);

		cursorPaint.setColor(Color.YELLOW);
		cursorPaint.setStyle(Paint.Style.STROKE);
		cursorPaint.setStrokeWidth(2 * getResources().getDisplayMetrics().density);


		dstRect = new Rect(0, 0, 1, 1);
		subWindowSrcRect = new Rect(0, 0, 1, 1);
		subWindowDstRect = new Rect(0, 0, 1, 1);

		draggingPaint.setStyle(Paint.Style.STROKE);
		draggingPaint.setColor(Color.YELLOW);
		draggingPaint.setAlpha(155);

		gestureDetector = new GestureDetector(context, new GestureDetecotr());
	}

	public void setVolumeReader(IVolumeReader volumeReader) {
		this.volumeReader = volumeReader;
		volumeReader.setOnVolumeReadListener(this);
		selectZoomLevel(getWidth(), getHeight());
		if (bitmap != null) {
			drawBitmap();
		}
	}

	public void setSubWindow(float start, float end) {
		hasSubWindow = true;
		subWindowStart = start;
		subWindowEnd = end;

		if (bitmap != null) {
			int subWidth = (int) (bitmap.getWidth() * (end - start));
			windowBitmap = Bitmap.createBitmap(subWidth, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			windowCanvas = new Canvas(windowBitmap);

			for(int i=0;i<windowBitmap.getWidth();i++){
				int index = (int) (bitmap.getWidth() * subWindowStart + i);
				drawSubWindowFrame(i, volumeReader.getVolume(zoomLevel, index));
			}
		}
	}


	public void unSetSubWindow() {
		hasSubWindow = false;
	}

	public void setListener(VolumeViewListener l) {
		this.listener = l;
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

	private void selectZoomLevel(int width, int height) {
		if (volumeReader == null || width < 1 || height < 1) return;
		width /= K;

		int[] levels = volumeReader.getZoomLevels();
		zoomLevel = levels.length - 1;

		for (int i = 0; i < levels.length; i++) {
			if (levels[i] >= width) {
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
			canvas.drawColor(DEFAULT_BG_COLOR);
			srcRect = new Rect(0, 0, levels[zoomLevel], height);

			if(hasSubWindow){
				int subWidth = (int) (bitmap.getWidth() * (subWindowEnd - subWindowStart));
				windowBitmap = Bitmap.createBitmap(subWidth, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
				windowCanvas = new Canvas(windowBitmap);

				for(int i=0;i<windowBitmap.getWidth();i++){
					int index = (int) (bitmap.getWidth() * subWindowStart + i);
					drawSubWindowFrame(i, volumeReader.getVolume(zoomLevel, index));
				}
			}
		}
	}

	private void drawBitmap() {
		if (volumeReader == null) return;

		synchronized (BITMAP_LOCK) {
			int i = 0;
			double value = volumeReader.getVolume(zoomLevel, i);

			while (value != Double.POSITIVE_INFINITY) {
				drawFrame(i++, value);
				value = volumeReader.getVolume(zoomLevel, i);

			}
		}
	}

	public void resetDrag() {
		dragging = false;
		postInvalidate();
	}

	@Override
	public void onNewFrame(int zoomLevel, int frameIndex, int totFrames, double value) {

		if (canvas == null || zoomLevel != this.zoomLevel) return;

		drawFrame(frameIndex, value);

		postInvalidate();
	}

	@Override
	public void onFrameReadingEnd() {
		drawBitmap();
		postInvalidate();
	}

	private void drawFrame(int i1, double value0) {
		synchronized (BITMAP_LOCK) {

			if (value0 == Double.POSITIVE_INFINITY) return;

			int height = getHeight();

			int barLength0 = minHeight + (int) (value0 * maxHeight);
			int y0up = (height - barLength0) / 2;
			int y0down = height - y0up;

			float x0 = i1;

			canvas.drawLine(x0, y0up, x0, y0down, paint);
		}
	}

	private void drawSubWindowFrame(int index, double value){
		int height = getHeight();

		int barLength = minHeight + (int) (value * maxHeight);
		int yUp = (height - barLength) / 2;
		int yDown = height - yUp;

		windowCanvas.drawLine(index,yUp,index,yDown, windowPaint);
	}

	public void setCursor(float d) {
		this.cursor = d;
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(0xffaaaaaa);

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		synchronized (BITMAP_LOCK) {
			if (bitmap != null) {
				dstRect.set(0, 0, width, height);
				canvas.drawBitmap(bitmap, srcRect, dstRect, null);
			}
		}

		if (hasSubWindow) {
			int left = (int) (width * subWindowStart);
			int right = (int) (width * subWindowEnd);
			subWindowDstRect.set(left, 0, right, height);

			left = (int) (bitmap.getWidth() * subWindowStart);
			right = (int) (bitmap.getWidth() * subWindowEnd);

			subWindowSrcRect.set(left, 0, right, bitmap.getHeight());

			canvas.drawBitmap(windowBitmap, subWindowSrcRect, subWindowDstRect, null);
			//canvas.drawBitmap(bitmap, srcRect, dstRect, hightLightPaint);
		}

		int x = (int) (width * cursor);
		canvas.drawLine(x, height, x, 0, cursorPaint);

		if (dragging || hasSecondCursor) {
			canvas.drawLine(draggingX, 0, draggingX, canvas.getHeight(), draggingPaint);
		}

	}

	protected void onSingleTup(MotionEvent event) {
		dragging = false;
		if (listener != null) {
			float x = event.getX() / getWidth();
			listener.onSingleTap(this, x);
		}
	}

	protected void onDoubleTap(MotionEvent event) {
		dragging = false;
		if (listener != null) {
			float x = event.getX() / getWidth();
			listener.onDoubleTap(this, x);
		}
	}

	protected void onLongPress(MotionEvent event) {
		if (canDrag) {
			dragging = true;
			draggingX = event.getX();
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {

		gestureDetector.onTouchEvent(motionEvent);

		int action = motionEvent.getAction();

		if (dragging && action == motionEvent.ACTION_MOVE) {
			draggingX = motionEvent.getX();
		} else if (dragging && action == motionEvent.ACTION_UP) {
			draggingX = motionEvent.getX();
			dragging = false;
			hasSecondCursor = true;
			if (listener != null) {
				float secondCursor = draggingX / getWidth();
				listener.onWindowSelected(this, Math.min(getCursor(), secondCursor), Math.max(getCursor(), secondCursor));
			}
		}
		postInvalidate();


		return true;
	}

	protected Rect getDstRect() {
		return dstRect;
	}

	public float getCursor() {
		return cursor;
	}

	public IVolumeReader getVolumeReader() {
		return volumeReader;
	}

	public void setCanDrag(boolean b) {
		canDrag = b;
		if (!canDrag) {
			hasSecondCursor = false;
			dragging = false;
		}
	}

	private class GestureDetecotr extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			VolumeView.this.onDoubleTap(event);
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			VolumeView.this.onSingleTup(event);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent event) {
			VolumeView.this.onLongPress(event);
		}
	}
}
