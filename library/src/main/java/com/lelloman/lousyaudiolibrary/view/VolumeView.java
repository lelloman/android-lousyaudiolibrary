package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;
import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class VolumeView extends View {

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private IAudioReader audioReader;
	private Bitmap bitmap;
	private Rect srcRect, dstRect;
	private Thread bitmapDrawer;

	public VolumeView(Context context) {
		this(context, null);
	}

	public VolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		dstRect = new Rect(0, 0, 1, 1);
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

		final Bitmap holder = bitmap;
		final Canvas canvas = new Canvas(holder);
		bitmap = null;


		double framesCount = (audioReader.getDurationMs() / 1000.) * audioReader.getSampleRate();
		final int pcmFramesPerVolumeFrame = (int) (framesCount / width);
		Log.d("VolumeView", String.format("frameCount = %.2f, framesPerVolum = %s", framesCount, pcmFramesPerVolumeFrame));
		final int height = getHeight();
		final int minHeight = (int) (height * .05f);
		final int maxHeight = (int) (height * .95f);

		bitmapDrawer = new Thread(new Runnable() {

			@Override
			public void run() {
				VolumeReader volumeReader = new VolumeReader(audioReader, pcmFramesPerVolumeFrame);
				Log.d(VolumeView.class.getSimpleName(), "start bitmap drawer");
				for (int i = 0; i < width; i++) {
					Double v = volumeReader.nextFrame();
					if (v == null) {
						break;
					}
					int barLength = minHeight + (int) (v * maxHeight);
					int d = (height - barLength) / 2;
					canvas.drawLine(i, height - d, i, d, paint);
				}
				bitmap = holder;
				Log.d(VolumeView.class.getSimpleName(), "end bitmap drawer");
				postInvalidate();
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

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(0xff888888);

		if (bitmap == null) {
			return;
		}

		dstRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
		canvas.drawBitmap(bitmap, srcRect, dstRect, null);
	}
}
