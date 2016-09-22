package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.lelloman.lousyaudiolibrary.reader.volume.IVolumeReader;


public class CompoundVolumeView extends LinearLayout implements VolumeView.VolumeViewListener {

	public interface CompoundVolumeViewListener {
		void onMoveCursor(CompoundVolumeView compoundVolumeView, float percentX);
		void onWindowSelected(CompoundVolumeView compoundVolumeView, float start, float end);
		void onWindowUnselected(CompoundVolumeView compoundVolumeView);
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

		volumeViewFull.setCanDrag(true);
		volumeViewSub.setCanDrag(false);
	}

	public void setVolumeReader(IVolumeReader volumeReader) {
		volumeViewFull.setVolumeReader(volumeReader);
	}

	public void setCursor(float percent) {
		volumeViewFull.setCursor(percent);
		if(showingFull) return;

		float span = windowEnd - windowStart;
		float localPercent = (percent - windowStart) / span;
		volumeViewSub.setCursor(localPercent);
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

		volumeViewFull.setCanDrag(false);
		volumeViewFull.setSubWindow(start, end);
		volumeViewSub.setCanDrag(true);
	}

	public void unSetWindow(){
		showingFull = true;

		LayoutParams layoutParams = (LayoutParams) volumeViewFull.getLayoutParams();
		layoutParams.height = LayoutParams.MATCH_PARENT;
		layoutParams.weight = 1;
		volumeViewFull.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) volumeViewSub.getLayoutParams();
		layoutParams.height = 0;
		layoutParams.weight = 0;
		volumeViewSub.setLayoutParams(layoutParams);

		volumeViewFull.setCanDrag(true);
		volumeViewFull.unSetSubWindow();
		volumeViewSub.setCanDrag(false);
	}

	@Override
	public void onSingleTap(VolumeView volumeView, float percentX) {
		if(listener == null) return;

		if(volumeView == volumeViewFull && showingFull){
			listener.onMoveCursor(this, percentX);
		}else if(volumeView == volumeViewSub && !showingFull){
			float span = windowEnd - windowStart;
			float totPercentX = windowStart + percentX * span;
			listener.onMoveCursor(this, totPercentX);
		}
	}

	@Override
	public void onDoubleTap(VolumeView volumeView, float percentX) {
		if(listener == null) return;

		listener.onWindowUnselected(this);
	}

	@Override
	public void onWindowSelected(VolumeView volumeView, float start, float end) {
		if(listener == null) return;

		if(volumeView == volumeViewFull && showingFull){
			listener.onWindowSelected(this, start, end);
		}else if(volumeView == volumeViewSub && !showingFull){
			float span = windowEnd - windowStart;
			float actualStart = windowStart + start * span;
			float actualEnd = windowStart + end * span;
			listener.onWindowSelected(this, actualStart, actualEnd);
		}

		volumeView.resetDrag();
	}
}
