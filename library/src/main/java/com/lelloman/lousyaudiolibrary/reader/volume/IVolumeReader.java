package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.LinkedList;
import java.util.List;

public abstract class IVolumeReader {

	public interface OnVolumeReadListener {
		void onNewFrame(int zoomLevel, int frameIndex, int totFrames, Double value);
		void onFrameReadingEnd();
	}

	protected IAudioReader audioReader;
	protected int[] zoomLevels;
	protected long totalFrames;
	protected OnVolumeReadListener listener;
	protected boolean reading;
	protected Double[][] data;
	protected List<IVolumeReader> children = new LinkedList<>();

	protected IVolumeReader(IVolumeReader parent, float start, float end){
		this.zoomLevels = new int[parent.zoomLevels.length];
		this.totalFrames = parent.totalFrames;
		data = new Double[zoomLevels.length][];

		float span = end - start;

		for(int i=0;i<data.length;i++){
			int subZoomLevel = (int) (parent.zoomLevels[i] * span);
			this.zoomLevels[i] = subZoomLevel;
			data[i] = new Double[subZoomLevel];
			int startJ = (int) (parent.zoomLevels[i] * start);

			for(int j=0;j<subZoomLevel;j++){
				Double d = parent.data[i][j+startJ];
				if(d == null){
					break;
				}
				data[i][j] = d;
			}
		}
	}

	public IVolumeReader(IAudioReader audioReader, int...zoomLevels){
		this.audioReader = audioReader;
		this.zoomLevels = zoomLevels;
		data = new Double[zoomLevels.length][];
		totalFrames = audioReader.getDurationFrames();
	}

	public abstract IVolumeReader subWindow(float start, float end);

	public Double getVolume(int zoom, int index){
		Double[] volume = data[zoom];
		if(volume == null || index >= volume.length){
			return null;
		}
		return volume[index];
	}

	public int[] getZoomLevels(){
		return zoomLevels;
	}

	public void setOnVolumeReadListener(OnVolumeReadListener listener){
		this.listener = listener;
	}

}
