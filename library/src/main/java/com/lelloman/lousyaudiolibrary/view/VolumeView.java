package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;
import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class VolumeView extends View implements View.OnTouchListener{

	public interface OnClickListener {
		void onClick(VolumeView volumeView, double percentX);
	}

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private IAudioReader audioReader;
	private Bitmap bitmap;
	private Rect srcRect, dstRect;
	private Thread bitmapDrawer;

	private double cursor = 0;
	private OnClickListener onClickListener;

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

	public void setOnClickListener(OnClickListener l) {
		this.onClickListener = l;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (w == 0 || h == 0) return;

		if (bitmap != null) {
			bitmap.recycle();
		}

		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		srcRect = new Rect(0, 0, w, h);
		drawBitmap();
	}

	private void drawBitmap() {
		final int width = getWidth();
		if (audioReader == null || bitmapDrawer != null || width <= 0) {
			return;
		}

		final Canvas canvas = new Canvas(bitmap);
		final int K = 4;
		double framesCount = (audioReader.getDurationMs() / 1000.) * audioReader.getSampleRate();
		final int pcmFramesPerVolumeFrame = (int) (framesCount / width) * K;
		Log.d("VolumeView", String.format("frameCount = %.2f, framesPerVolum = %s", framesCount, pcmFramesPerVolumeFrame));
		final int height = getHeight();
		final int minHeight = (int) (height * .05f);
		final int maxHeight = (int) (height * .95f);

		bitmapDrawer = new Thread(new Runnable() {

			@Override
			public void run() {
				VolumeReader volumeReader = new VolumeReader(audioReader, pcmFramesPerVolumeFrame);
				Log.d(VolumeView.class.getSimpleName(), "start bitmap drawer");
				for (int i = 0; i < width; i += K) {
					Double v = volumeReader.nextFrame();
					if (v == null) {
						break;
					}
					int barLength = minHeight + (int) (v * maxHeight);
					int d = (height - barLength) / 2;
					canvas.drawLine(i, height - d, i, d, paint);
					if(i % 20 == 0){
						postInvalidate();
					}
				}
				Log.d(VolumeView.class.getSimpleName(), "end bitmap drawer");
			}
		});

		bitmapDrawer.start();
	}

	public void setAudioReader(IAudioReader audioReader) {
		this.audioReader = audioReader;
		if (bitmap != null) {
			drawBitmap();
		}
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
