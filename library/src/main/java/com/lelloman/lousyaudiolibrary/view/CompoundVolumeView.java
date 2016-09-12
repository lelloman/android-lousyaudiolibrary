package com.lelloman.lousyaudiolibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.lelloman.lousyaudiolibrary.reader.VolumeReader;


public class CompoundVolumeView extends LinearLayout {

	private VolumeView volumeViewFull;
	private VolumeView volumeViewSub;

	private VolumeView.VolumeViewListener volumeViewListener;
	private boolean showingFull = true;

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
	}

	public void setVolumeReader(VolumeReader volumeReader){
		volumeViewFull.setVolumeReader(volumeReader);
	}

	public void setCursor(float percent){
		volumeViewFull.setCursor(percent);
	}

	public void setListener(VolumeView.VolumeViewListener l){
		volumeViewListener = l;
		if(showingFull){
			volumeViewFull.setListener(l);
		}
	}

	public void setWindow(float start, float end){
		LayoutParams layoutParams = (LayoutParams) volumeViewFull.getLayoutParams();
		layoutParams.height = 0;
		layoutParams.weight = 1;
		volumeViewFull.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) volumeViewSub.getLayoutParams();
		layoutParams.height = 0;
		layoutParams.weight = 5;
		volumeViewSub.setLayoutParams(layoutParams);

		volumeViewSub.setVolumeReader(volumeViewFull.getVolumeReader().subWindow(start,end));
	}
}
