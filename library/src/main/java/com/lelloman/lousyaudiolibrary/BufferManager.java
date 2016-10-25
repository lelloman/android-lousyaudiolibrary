package com.lelloman.lousyaudiolibrary;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

public class BufferManager {

	public final int bufferSize, stepSize;

	private double[] buffer;
	private double[] chunk;
	private IAudioReader reader;
	private int cursor = 0;
	private boolean end = false;

	public BufferManager(IAudioReader reader, int bufferSize, int stepSize) {

		this.bufferSize = bufferSize-stepSize;
		this.stepSize = stepSize;
		this.reader = reader;

		this.buffer = new double[bufferSize];

		initBuffer();
	}

	private void initBuffer() {

		int n=0;
		while ( (chunk == null || chunk.length < 1) && n++ < 10)
			chunk = reader.nextChunkDouble();

		for (int i = stepSize; i < buffer.length; i++) {
			buffer[i] = chunk[cursor++];
			if (cursor >= chunk.length) {
				chunk = reader.nextChunkDouble();
				if (chunk == null) {
					end = true;
					chunk = new double[buffer.length];
				}
				cursor = 0;
			}
		}
	}

	public boolean hasNext(){
		return !reader.getSawOutputEOS();
	}

	public double[] next() {

		if (end) return null;


		for (int i = 0; i < bufferSize; i++)
			buffer[i] = buffer[i + stepSize];


		for (int i = bufferSize; i < buffer.length; i++) {
			buffer[i] = chunk[cursor++];
			if (cursor >= chunk.length) {
				chunk = reader.nextChunkDouble();
				if (chunk == null) {
					end = true;
					chunk = new double[buffer.length];
				}
				cursor = 0;
			}
		}
		return buffer;
	}

}
