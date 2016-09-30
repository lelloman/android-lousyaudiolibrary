package com.lelloman.lousyaudiolibrary.view.equalizer;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EqualizerView extends View {

	private float[] bands;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Path path = new Path();

	int bgColor = 0xffbbbbbb;

	public EqualizerView(Context context) {
		super(context);
		init();
	}

	public EqualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EqualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public EqualizerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init(){
		paint.setColor(0xffff0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(getResources().getDisplayMetrics().density);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawColor(bgColor);
		if(bands == null) return;

		path.reset();
		float height = canvas.getHeight();

		float x = 0;
		float nextX, y, nextY;
		float xStep = canvas.getWidth() / (bands.length - 1);

		for(int i = 0; i< bands.length-1; i++){
			nextX = x + xStep;

			y = bands[i] * height;
			nextY = bands[i+1] * height;

			path.moveTo(x, y);
			path.lineTo(nextX, nextY);
			x = nextX;
		}

		canvas.drawPath(path, paint);
	}

	public void setBands(float[] bands){
		this.bands = bands;
		postInvalidate();
	}

	public float[] getBands(){
		return bands;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				onTouchMove(event.getX(), event.getY());
				return true;

			case MotionEvent.ACTION_UP:
				onTouchUp(event.getX(), event.getY());
				return true;
		}

		return super.onTouchEvent(event);
	}

	private void onTouchMove(float x, float y){
		if(bands == null) return;

		int closestBand = -1;
		double minDistance = Double.MAX_VALUE;

		double iStep = getWidth() / (bands.length-1);

		for(int i = 0; i< bands.length; i++){
			double bandValue = i * iStep;
			double distance = Math.abs(x - bandValue);
			if(distance < minDistance){
				minDistance  = distance;
				closestBand = i;
			}
		}

		if(closestBand < 0) return;

		float v = y / getHeight();
		if(v < 0) v = 0;
		else if(v > 1) v = 1;
		bands[closestBand] = v;

		postInvalidate();
	}

	private void onTouchUp(float x, float y){

	}
}
