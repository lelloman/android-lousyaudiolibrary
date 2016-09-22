package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class IVolumeReader {

	public interface OnVolumeReadListener {
		void onNewFrame(int zoomLevel, int frameIndex, int totFrames, double value);
		void onFrameReadingEnd();
	}

	protected IAudioReader audioReader;
	protected int[] zoomLevels;
	protected long totalFrames;
	protected OnVolumeReadListener listener;
	protected boolean reading;
	protected double[][] data;
	protected List<IVolumeReader> children = new LinkedList<>();
	protected int cursor;

	protected IVolumeReader(IVolumeReader parent, float start, float end){
		this.zoomLevels = new int[parent.zoomLevels.length];
		this.totalFrames = parent.totalFrames;
		data = new double[zoomLevels.length][];

		float span = end - start;

		for(int i=0;i<data.length;i++){
			int subZoomLevel = (int) (parent.zoomLevels[i] * span);
			this.zoomLevels[i] = subZoomLevel;
			data[i] = new double[subZoomLevel];
			Arrays.fill(data[i], Double.POSITIVE_INFINITY);
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
		data = new double[zoomLevels.length][];
		totalFrames = audioReader.getDurationFrames();

		for(int i=0;i<zoomLevels.length;i++){
			data[i] = new double[zoomLevels[i]];
		}
	}

	public abstract IVolumeReader subWindow(float start, float end);

	public double getVolume(int zoom, int index){
		double[] volume = data[zoom];
		if( index >= volume.length){
			return Double.POSITIVE_INFINITY;
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
