package com.lelloman.lousyaudiolibrary.view.equalizer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lelloman.lousyaudiolibrary.Util;

public class EqualizerView extends View {

	public interface OnBandUpdatesListener {
		void onBandsUpdate(float[] bands);
	}

	private float[] bands;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Path path = new Path();
	private OnBandUpdatesListener listener;
	private Util.ColorSet colorSet;
	private Bitmap bitmap;

	int bgColor = 0xffeeeeee;

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

	public void setOnBandsUpdateListener(OnBandUpdatesListener listener){
		this.listener = listener;
	}

	private void init(){
		paint.setColor(0xffff0000);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(getResources().getDisplayMetrics().density);

		gridPaint.setStyle(Paint.Style.FILL);
		colorSet = new Util.ColorSet(getContext());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if(w == 0 || h == 0) return;

		int[] colors = new int[]{
				colorSet.accent,
				colorSet.primary,
				colorSet.windowBackground
		};
		float[] positions = new float[]{
			0,.5f,1.f
		};

		LinearGradient gradient = new LinearGradient(0,0,0,h,colors,positions, Shader.TileMode.CLAMP);
		paint.setShader(gradient);

		bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setStyle(Paint.Style.FILL);
		bgPaint.setShader(gradient);
		bgPaint.setAlpha(100);
		canvas.drawRect(0,0, w, h, bgPaint);

		Paint linesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linesPaint.setStyle(Paint.Style.STROKE);
		linesPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 2);
		linesPaint.setShader(gradient);
		int N = 20;
		float xStep = w / (N - 1.f);
		float yStep = h / (N - 1.f);

		for(int i=0;i<N;i++){
			float x = i * xStep;
			float y = i * yStep;
			canvas.drawLine(x, 0, x, h, linesPaint);
		//	canvas.drawLine(0, y, w, y, linesPaint);
		}

		gridPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//canvas.drawColor(bgColor);
		if(bands == null) return;

		path.reset();
		float height = canvas.getHeight();
		float width = canvas.getWidth();

		float x = 0;
		float y = y = (1-bands[0]) * height;
		float nextX, nextY;
		float xStep = width / (bands.length - 1);

		path.moveTo(0, y);
		for(int i = 0; i< bands.length-1; i++){
			nextX = x + xStep;
			y = (1-bands[i]) * height;;

			nextY = (1-bands[i+1]) * height;

			path.lineTo(nextX, nextY);
			x = nextX;
		}

		canvas.drawPath(path, paint);

		path.lineTo(width, y);
		path.lineTo(width, height);
		path.lineTo(0, height);
		path.lineTo(0, (1-bands[0]) *height );

		canvas.drawPath(path, gridPaint);
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
			case MotionEvent.ACTION_UP:
				updateBands(event.getX(), event.getY());
				return true;
		}

		return super.onTouchEvent(event);
	}

	private void updateBands(float x, float y){
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

		float v = 1 - y / getHeight();
		if(v < 0) v = 0;
		else if(v > 1) v = 1;
		bands[closestBand] = v;

		postInvalidate();
		if(listener != null) listener.onBandsUpdate(bands);
	}
}
