package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class ZoomableVolumeView extends VolumeView {

	private boolean dragging;
	private float draggingX;
	private Paint draggingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private boolean hasSecondCursor;

	private boolean hasWindow;
	private float windowStart;
	private float windowEnd;
	private float windowRatio;

	public ZoomableVolumeView(Context context) {
		this(context, null);
	}

	public ZoomableVolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		draggingPaint.setStyle(Paint.Style.STROKE);
		draggingPaint.setColor(Color.YELLOW);
		draggingPaint.setAlpha(155);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		super.onTouchEvent(ev);

		int action = ev.getAction();

		if (dragging && action == ev.ACTION_MOVE) {
			draggingX = ev.getX();
		} else if (dragging && action == ev.ACTION_UP) {
			draggingX = ev.getX();
			dragging = false;
			hasSecondCursor = true;
			if(listener != null){
				float secondCursor = draggingX / getWidth();
				listener.onWindowSelected(this, Math.min(getCursor(), secondCursor), Math.max(getCursor(), secondCursor));
			}
		}
		postInvalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(hasWindow){
			float upperWaveHeight = .2f;
			canvas.save();
			canvas.scale(1,upperWaveHeight,getWidth()*.5f,0);
			super.onDraw(canvas);
			canvas.restore();

			if (dragging || hasSecondCursor) {
				int miniHeight = (int) (canvas.getHeight() * upperWaveHeight);
				canvas.drawLine(draggingX, miniHeight, draggingX, canvas.getHeight(), draggingPaint);
			}
		}else{
			super.onDraw(canvas);
			if (dragging || hasSecondCursor) {
				canvas.drawLine(draggingX, 0, draggingX, canvas.getHeight(), draggingPaint);
			}
		}


	}

	@Override
	protected void onDoubleTap(MotionEvent event) {
		super.onDoubleTap(event);
		dragging = false;
	}

	@Override
	protected void onLongPress(MotionEvent event) {
		dragging = true;
		draggingX = event.getX();
		postInvalidate();
	}

	public void setWindow(float windowStart, float windowEnd){
		hasSecondCursor = false;
		hasWindow = true;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		windowRatio = windowEnd - windowStart;
		postInvalidate();
	}
}
