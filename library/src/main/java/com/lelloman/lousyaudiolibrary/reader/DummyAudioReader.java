package com.lelloman.lousyaudiolibrary.reader;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DummyAudioReader implements IAudioReader {


	private int lengthFrames;
	private int frameRate;
	double frameRateDouble;
	private int frequency;
	private int duration;
	private int bufferSize;
	private double[] doubleBuffer;
	private byte[] byteBuffer;
	private double durationMs;

	private int cursor;
	private double phase;
	private double phaseStep;

	public DummyAudioReader(int lengthFrames, int frameRate, int frequency, int duration, int bufferSize){
		this.lengthFrames = lengthFrames;
		this.frameRate = frameRate;
		this.frameRateDouble = frameRate;
		this.frequency = frequency;
		this.duration = duration;
		this.bufferSize = bufferSize;
		this.durationMs = lengthFrames / (frameRate * 1000.);

		this.doubleBuffer = new double[bufferSize];
		this.byteBuffer = new byte[bufferSize*2];

		phaseStep = (frequency / frameRateDouble) * 2 * Math.PI;
	}

	@Override
	public byte[] nextChunk() {

		double amplitude = Short.MAX_VALUE * .7;
		ByteBuffer bb = ByteBuffer.wrap(byteBuffer);
		bb.order(ByteOrder.nativeOrder());
		nextChunkDouble();
		for(int i=0;i<bufferSize;i++){
			short s = (short) (doubleBuffer[i] * amplitude);
			bb.putShort(s);
		}

		return byteBuffer;
	}

	@Override
	public double[] nextChunkDouble() {
		double amplitude = .7;
		for(int i=0;i<bufferSize;i++){
			doubleBuffer[i] = Math.sin(cursor++ * phaseStep) * amplitude;
		}
		return doubleBuffer;
	}

	@Override
	public long getDurationMs() {
		return (long) (durationMs);
	}

	@Override
	public long getDurationFrames() {
		return lengthFrames;
	}

	@Override
	public long getCurrentMs() {
		return (long) ((cursor * durationMs) / lengthFrames);
	}

	@Override
	public double getPercent() {
		return cursor / (double) lengthFrames;
	}

	@Override
	public int getSampleRate() {
		return frameRate;
	}

	@Override
	public int getChannels() {
		return 1;
	}

	@Override
	public int getBitRate() {
		return 0;
	}

	@Override
	public boolean getSawOutputEOS() {
		return cursor > lengthFrames;
	}

	@Override
	public void reset() {
		cursor = 0;
	}

	@Override
	public void seek(long pos) {
		cursor = (int) (((durationMs*1000.) / pos) * lengthFrames);
	}

	@Override
	public void seek(double percent) {
		cursor = (int) (lengthFrames * percent);
	}

	@Override
	public void release() {

	}
}
