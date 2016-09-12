package com.lelloman.lousyaudiolibrary.reader;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VolumeReader {

	public interface OnVolumeReadListener {
		void onNewFrame(int zoomLevel, int frameIndex, int totFrames, Double value);
	}

	private IAudioReader audioReader;
	private int chunkCursor;
	private byte[] chunk;
	private byte[] miniByteBuffer;
	private Double[][] data;
	private int cursor;
	private OnVolumeReadListener listener;
	private long totalFrames;
	private int[] zoomLevels;
	private List<VolumeReader> children = new LinkedList<>();
	private boolean reading;

	private VolumeReader(VolumeReader parent, float start, float end){
		this.zoomLevels = new int[parent.zoomLevels.length];
		this.totalFrames = parent.totalFrames;
		this.miniByteBuffer = new byte[2];
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

		Log.d(VolumeReader.class.getSimpleName(), String.format("subWindow original levels = %s - sub %s", Arrays.toString(parent.zoomLevels), Arrays.toString(this.zoomLevels)));
	}

	public VolumeReader(final IAudioReader audioReader, int... zoomLevels) {

		this.audioReader = audioReader;
		this.miniByteBuffer = new byte[2];
		totalFrames = audioReader.getDurationFrames();
		data = new Double[zoomLevels.length][];
		this.zoomLevels = zoomLevels;

		final VolumeMaker[] makers = new VolumeMaker[zoomLevels.length];
		for(int i = 0; i< zoomLevels.length; i++){
			data[i] = new Double[zoomLevels[i]];
			int pcmFramesPerVolume = (int) (totalFrames / zoomLevels[i]);
			makers[i] = new VolumeMaker(i, pcmFramesPerVolume, data[i]);
		}

		reading = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(cursor < totalFrames && !audioReader.getSawOutputEOS()){
					miniByteBuffer[1] = getNextByte();
					miniByteBuffer[0] = getNextByte();
					short s = ByteBuffer.wrap(miniByteBuffer).getShort();
					short v = (short) Math.abs(s);
					for(VolumeMaker maker : makers) maker.nextSample(v);
					cursor++;
				}
				reading = false;
				synchronized (children){
					children.clear();
				}
			}
		}).start();
	}

	public VolumeReader subWindow(float start, float end){

		VolumeReader output = new VolumeReader(this, start, end);
		if(reading) {
			synchronized (children) {
				children.add(output);
			}
		}

		return output;
	}

	public int getVolumeLength(int zoomLevel) {
		return data[zoomLevel].length;
	}

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

	private byte getNextByte() {
		if (chunk == null || chunkCursor >= chunk.length) {
			chunk = audioReader.nextChunk();
			chunkCursor = 0;
		}
		int i = 0;
		while (i < 5 && chunk == null || chunk.length < 2) {
			chunk = audioReader.nextChunk();
			chunkCursor = 0;
			i++;
		}
		if (chunk != null) {
			return chunk[chunkCursor++];
		}else{
			return 0;
		}
	}

	private class VolumeMaker{

		final int pcmFramesPerVolumeFrame;
		final Double[] volume;
		final int index;
		double max;
		int pcmcursor;
		int volumeCursor;

		public VolumeMaker(int index, int pcmFramesPerVolumeFrame, Double[] volume){
			this.index = index;
			this.pcmFramesPerVolumeFrame = pcmFramesPerVolumeFrame;
			this.volume = volume;
		}

		public void nextSample(Short sample){

			if(pcmcursor >= pcmFramesPerVolumeFrame){
				if(volumeCursor >= volume.length) return;

				double output = max / (double) Short.MAX_VALUE;
				pcmcursor = 0;
				volume[volumeCursor] = output;
				if(listener != null){
					listener.onNewFrame(index, volumeCursor++, volume.length, output);
				}
				max = 0;
			}else{
				if(sample > max){
					max = sample;
				}
				pcmcursor++;
			}

		}
	}
}
