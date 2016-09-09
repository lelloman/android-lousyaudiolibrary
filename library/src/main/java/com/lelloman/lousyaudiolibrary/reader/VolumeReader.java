package com.lelloman.lousyaudiolibrary.reader;

import java.nio.ByteBuffer;

public class VolumeReader {

	public interface OnVolumeReadListener {
		void onNewFrame(int frameIndex, int totFrames, Double value);
	}

	private IAudioReader audioReader;
	private int chunkCursor;
	private byte[] chunk;
	private int pcmFramesPerVolumeFrame;
	private byte[] miniByteBuffer;
	private Double[] volume;
	private int cursor;
	private boolean run = true;
	private OnVolumeReadListener listener;

	public VolumeReader(final IAudioReader audioReader, final int pcmFramesPerVolumeFrame) {
		this.audioReader = audioReader;
		this.pcmFramesPerVolumeFrame = pcmFramesPerVolumeFrame;
		this.miniByteBuffer = new byte[2];
		int totalFrames = (int) (audioReader.getDurationFrames() / pcmFramesPerVolumeFrame);
		volume = new Double[totalFrames];

		new Thread(new Runnable() {
			@Override
			public void run() {

				int volumeLength = volume.length;
				while(run && cursor < volume.length && !audioReader.getSawOutputEOS()){

					double max = 0;

					for (int i = 0; i < pcmFramesPerVolumeFrame; i++) {
						miniByteBuffer[1] = getNextByte();
						miniByteBuffer[0] = getNextByte();
						short s = ByteBuffer.wrap(miniByteBuffer).getShort();
						short v = (short) Math.abs(s);
						if(v > max){
							max = v;
						}
					}
					double output = max / (double) Short.MAX_VALUE;

					volume[cursor] = output;
					if(listener != null){
						listener.onNewFrame(cursor++, volumeLength, output);
					}

				}
			}
		}).start();
	}

	public Double getVolume(int index){
		if(volume == null || index >= volume.length){
			return null;
		}
		return volume[index];
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
		while (i < 5 && chunk == null) {
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
}
