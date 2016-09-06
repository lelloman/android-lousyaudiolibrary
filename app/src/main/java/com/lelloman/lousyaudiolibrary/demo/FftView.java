package com.lelloman.lousyaudiolibrary.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class FftView extends View {

	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	double[] fftFrame;

	Bitmap bitmapA, bitmapB;

	public FftView(Context context) {
		this(context, null);
	}

	public FftView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setFftFrame(double[] fftFrame){
		this.fftFrame = fftFrame;
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(fftFrame == null || fftFrame.length < 2) return;

		int fftFrameLength = fftFrame.length / 2;

		if(bitmapA == null || bitmapA.getWidth() != fftFrameLength || bitmapA.getHeight() != canvas.getHeight()){
			if(bitmapA != null){
				bitmapA.recycle();
				bitmapB.recycle();
			}

			bitmapA = Bitmap.createBitmap(fftFrameLength, canvas.getHeight(), Bitmap.Config.ARGB_8888);
			bitmapB = Bitmap.createBitmap(fftFrameLength, canvas.getHeight(), Bitmap.Config.ARGB_8888);
		}

		double kWidth = canvas.getWidth() / (double) fftFrameLength;


		int height = canvas.getHeight();

		/*double max = Double.MIN_VALUE;
		for(int i=0;i<fftFrameLength;i++){
			double v = fftFrame[i];
			if(v > max){
				max = v;
			}
		}*/
		double max = (double) fftFrameLength / 4;

		for(int i=0;i<fftFrameLength;i++){

			int x = (int) (i * kWidth);
			int y = (int) ((fftFrame[i] / max) * height);
			canvas.drawLine(x, 0, x,y, paint);
		}
	}
}
