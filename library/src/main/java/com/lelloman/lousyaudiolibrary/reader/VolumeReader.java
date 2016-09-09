package com.lelloman.lousyaudiolibrary.reader;

import java.nio.ByteBuffer;

public class VolumeReader {

	private IAudioReader audioReader;
	private int chunkCursor;
	private byte[] chunk;
	private int pcmFramesPerVolumeFrame;
	private byte[] miniByteBuffer;

	public VolumeReader(IAudioReader audioReader, int pcmFramesPerVolumeFrame) {
		this.audioReader = audioReader;
		this.pcmFramesPerVolumeFrame = pcmFramesPerVolumeFrame;
		this.miniByteBuffer = new byte[2];
	}

	public Double nextFrame() {

		if (audioReader.getSawOutputEOS()) return null;

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
		return output;
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
