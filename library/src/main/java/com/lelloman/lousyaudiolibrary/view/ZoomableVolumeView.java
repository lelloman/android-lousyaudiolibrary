package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;


public class ZoomableVolumeView extends VolumeView {

	public interface ZoomableViewListener extends VolumeView.OnClickListener {
		void onWindowSelected(ZoomableVolumeView volumeView, float start, float end);
	}

	private GestureDetector longTapDetector;
	private boolean dragging;
	private float draggingX;
	private Paint draggingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private boolean hasSecondCursor;
	private ZoomableViewListener listener;

	public ZoomableVolumeView(Context context) {
		this(context, null);
	}

	public ZoomableVolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		longTapDetector = new GestureDetector(context, new LongPressListener());
		draggingPaint.setStyle(Paint.Style.STROKE);
		draggingPaint.setColor(Color.YELLOW);
		draggingPaint.setAlpha(155);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		if (l instanceof ZoomableViewListener) {
			listener = (ZoomableViewListener) l;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		longTapDetector.onTouchEvent(ev);

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
		super.onDraw(canvas);

		if (dragging || hasSecondCursor) {
			canvas.drawLine(draggingX, 0, draggingX, canvas.getHeight(), draggingPaint);
		}
	}

	private class LongPressListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			ZoomableVolumeView.super.onClick(event.getX());
			dragging = false;
			return true;
		}

		@Override
		public void onLongPress(MotionEvent event) {
			dragging = true;
			draggingX = event.getX();
			postInvalidate();
		}
	}
}
