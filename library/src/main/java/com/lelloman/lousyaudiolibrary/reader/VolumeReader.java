package com.lelloman.lousyaudiolibrary.reader;

import java.nio.ByteBuffer;

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
	private boolean run = true;
	private OnVolumeReadListener listener;
	private long totalFrames;
	private int[] zoomLevels;

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

		new Thread(new Runnable() {
			@Override
			public void run() {

				while(run && cursor < totalFrames && !audioReader.getSawOutputEOS()){

					miniByteBuffer[1] = getNextByte();
					miniByteBuffer[0] = getNextByte();
					short s = ByteBuffer.wrap(miniByteBuffer).getShort();
					short v = (short) Math.abs(s);
					for(VolumeMaker maker : makers) maker.nextSample(v);
				}
			}
		}).start();
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
