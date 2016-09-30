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

	private float[] values;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Path path = new Path();

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
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(values == null) return;

		path.reset();
		float height = canvas.getHeight();

		float x = 0;
		float nextX, y, nextY;
		float xStep = canvas.getWidth() / (values.length - 1);

		for(int i=0;i<values.length-1;i++){
			nextX = x + xStep;

			y = values[i] * height;
			nextY = values[i+1] * height;

			path.moveTo(x, y);
			path.lineTo(nextX, nextY);
			x = nextX;
			y = nextY;
		}
	}

	public void setValues(float[] values){
		this.values = values;
		postInvalidate();
	}

	public float[] getValues(){
		return values;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch(event.getAction()){
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

	}

	private void onTouchUp(float x, float y){

	}
}
