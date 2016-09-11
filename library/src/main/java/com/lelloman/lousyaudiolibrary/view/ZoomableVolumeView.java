package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class ZoomableVolumeView extends VolumeView {

	public interface ZoomableViewListener extends VolumeViewListener {
		void onWindowSelected(ZoomableVolumeView volumeView, float start, float end);
	}

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
		draggingPaint.setStyle(Paint.Style.STROKE);
		draggingPaint.setColor(Color.YELLOW);
		draggingPaint.setAlpha(155);
	}

	@Override
	public void setVolumeViewListener(VolumeViewListener l) {
		super.setVolumeViewListener(l);
		if (l instanceof ZoomableViewListener) {
			listener = (ZoomableViewListener) l;
		}
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
		super.onDraw(canvas);

		if (dragging || hasSecondCursor) {
			canvas.drawLine(draggingX, 0, draggingX, canvas.getHeight(), draggingPaint);
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
}
