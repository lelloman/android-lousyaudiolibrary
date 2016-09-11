package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


public class ZoomableVolumeView extends VolumeView {

	ScaleGestureDetector scaleDetector;
	GestureDetector longTapDetector;
	float scaleFactor = 1;
	private boolean dragging;
	private float draggingX;
	private Paint draggingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public ZoomableVolumeView(Context context) {
		this(context, null);
	}

	public ZoomableVolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		longTapDetector = new GestureDetector(context, new LongPressListener());
		draggingPaint.setStyle(Paint.Style.STROKE);
		draggingPaint.setColor(Color.YELLOW);
		draggingPaint.setAlpha(155);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		scaleDetector.onTouchEvent(ev);
		longTapDetector.onTouchEvent(ev);

		int action = ev.getAction();

		if(dragging && action == ev.ACTION_MOVE){
			draggingX= ev.getX();
		}else if(action == ev.ACTION_UP){
			super.onClick(ev.getX());
			dragging = false;
		}
		postInvalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(dragging){
			canvas.drawLine(draggingX, 0, draggingX, canvas.getHeight(), draggingPaint);
		}
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float dx = detector.getPreviousSpanX() / detector.getCurrentSpanX();

			scaleFactor *= dx;
			// Don't let the object get too small or too large.
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
			Log.d(ZoomableVolumeView.class.getSimpleName(), String.format("onScale() %.2f %.2f %.2f", scaleFactor, dx, detector.getFocusX()));

			invalidate();
			return true;
		}

	}

	private class LongPressListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			dragging = true; //ZoomableVolumeView.super.onClick(e.getX());
			draggingX = e.getX();
			postInvalidate();
		}
	}
}
