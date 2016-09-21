package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

public abstract class IVolumeReader {

	public interface OnVolumeReadListener {
		void onNewFrame(int zoomLevel, int frameIndex, int totFrames, Double value);
	}

	protected IAudioReader audioReader;
	protected int[] zoomLevels;
	protected long totalFrames;
	protected OnVolumeReadListener listener;
	protected boolean reading;
	protected Double[][] data;

	public abstract IVolumeReader subWindow(float start, float end);
	public abstract Double getVolume(int zoom, int index);

	public int[] getZoomLevels(){
		return zoomLevels;
	}
}
