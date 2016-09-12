package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class CompoundVolumeView extends LinearLayout implements VolumeView.VolumeViewListener {

	public interface CompoundVolumeViewListener {
		void onMoveCursor(CompoundVolumeView compoundVolumeView, float percentX);
		void onWindowSelected(CompoundVolumeView compoundVolumeView, float start, float end);
	}

	private VolumeView volumeViewFull;
	private VolumeView volumeViewSub;

	private CompoundVolumeViewListener listener;
	private boolean showingFull = true;

	private float windowStart, windowEnd;

	public CompoundVolumeView(Context context) {
		this(context, null);
	}

	public CompoundVolumeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);

		volumeViewFull = new VolumeView(context);
		volumeViewSub = new VolumeView(context);

		volumeViewFull.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		volumeViewSub.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0));

		addView(volumeViewFull);
		addView(volumeViewSub);

		volumeViewFull.setListener(this);
		volumeViewSub.setListener(this);
	}

	public void setVolumeReader(VolumeReader volumeReader) {
		volumeViewFull.setVolumeReader(volumeReader);
	}

	public void setCursor(float percent) {
		volumeViewFull.setCursor(percent);
	}

	public void setListener(CompoundVolumeViewListener l) {
		listener = l;
	}

	public void setWindow(float start, float end) {

		windowStart = start;
		windowEnd = end;

		LayoutParams layoutParams = (LayoutParams) volumeViewFull.getLayoutParams();
		layoutParams.height = 0;
		layoutParams.weight = 1;
		volumeViewFull.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) volumeViewSub.getLayoutParams();
		layoutParams.height = 0;
		layoutParams.weight = 5;
		volumeViewSub.setLayoutParams(layoutParams);

		volumeViewSub.setVolumeReader(volumeViewFull.getVolumeReader().subWindow(start, end));

		showingFull = false;
	}

	@Override
	public void onDoubleTap(VolumeView volumeView, float percentX) {

		if (!showingFull && volumeView == volumeViewFull) {
			return;
		} else if (volumeView == volumeViewFull) {
			if (listener != null) {
				listener.onMoveCursor(this, percentX);
			}
		} else if (volumeView == volumeViewSub) {
			if (listener != null) {
				float span = windowEnd - windowStart;
				float totPercentX = windowStart + percentX * span;
				listener.onMoveCursor(this, totPercentX);
			}
		}

	}

	@Override
	public void onWindowSelected(VolumeView volumeView, float start, float end) {
		if(volumeView == volumeViewFull && showingFull && listener != null){
			listener.onWindowSelected(this, start, end);
		}
	}
}
