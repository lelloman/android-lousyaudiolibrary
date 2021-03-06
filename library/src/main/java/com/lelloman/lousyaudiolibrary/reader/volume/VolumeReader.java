package com.lelloman.lousyaudiolibrary.reader.volume;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.nio.ByteBuffer;

public class VolumeReader extends IVolumeReader{

	private int chunkCursor;
	private byte[] chunk;
	private byte[] miniByteBuffer = new byte[2];

	private VolumeReader(VolumeReader parent, float start, float end){
		super(parent, start, end);
	}

	public VolumeReader(final IAudioReader audioReader, int... zoomLevels) {
		super(audioReader, zoomLevels);

		final VolumeMaker[] makers = new VolumeMaker[zoomLevels.length];
		for(int i = 0; i< zoomLevels.length; i++){
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

	@Override
	public IVolumeReader subWindow(float start, float end){

		VolumeReader output = new VolumeReader(this, start, end);
		if(reading) {
			synchronized (children) {
				children.add(output);
			}
		}

		return output;
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

		public static final double SHORT_MAX = Short.MAX_VALUE;

		final int pcmFramesPerVolumeFrame;
		final double[] volume;
		final int index;
		double max;
		int pcmcursor;
		int volumeCursor;

		public VolumeMaker(int index, int pcmFramesPerVolumeFrame, double[] volume){
			this.index = index;
			this.pcmFramesPerVolumeFrame = pcmFramesPerVolumeFrame;
			this.volume = volume;
		}

		public void nextSample(Short sample){

			if(pcmcursor >= pcmFramesPerVolumeFrame){
				if(volumeCursor >= volume.length) return;

				double output = max / SHORT_MAX;
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
