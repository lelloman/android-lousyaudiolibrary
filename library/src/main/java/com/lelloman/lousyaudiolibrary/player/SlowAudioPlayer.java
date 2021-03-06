package com.lelloman.lousyaudiolibrary.player;

import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.IPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderMultiThread;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.nio.ByteBuffer;

public class SlowAudioPlayer extends BufferedAudioPlayer {

	protected static final int DEFAULT_FRAME_SIZE = 4096 * 2;
	protected static final int DEFAULT_HOP = DEFAULT_FRAME_SIZE / 8;
	protected static final float DEFAULT_SCALE = .2f;

	private boolean slow = false;
	private int frameSize;
	private int hop;
	private float scale;

	private IPhaseVocoder vocoder = null;
	private ByteBuffer miniByteBuffer = ByteBuffer.allocate(2);

	public SlowAudioPlayer(EventsListener listener, int frameSize, int hop, float scale){
		super(listener);
		this.frameSize = frameSize;
		this.hop = hop;
		this.scale = scale;
	}
	public SlowAudioPlayer(EventsListener listener, int frameSize, int hop){
		this(listener, frameSize, hop, DEFAULT_SCALE);
	}

	public SlowAudioPlayer(EventsListener listener) {
		this(listener, DEFAULT_FRAME_SIZE, DEFAULT_HOP);
	}

	@Override
	public boolean init(IAudioReader reader) {
		if(!super.init(reader)) return false;

		initVocoder();
		return true;
	}

	private void initVocoder() {
		if (reader == null) return;
//		vocoder = new NativePhaseVocoderMultiThread(reader, scale, frameSize, hop);
		vocoder = new NativePhaseVocoder(reader, scale, frameSize, hop);
		//vocoder = new JavaPhaseVocoder(reader, DEFAULT_SCALE, DEFAULT_FRAME_SIZE, DEFAULT_HOP);
	}

	@Override
	protected int fillBuffer() {

		byte[] theChunk;
		if (slow) {
			double[] chunk = vocoder.next();
			theChunk = new byte[chunk.length * 2];
			for (int i = 0; i < chunk.length; i++) {
				short x = (short) (chunk[i] * Short.MAX_VALUE);
				miniByteBuffer.position(0);
				miniByteBuffer.putShort(x);
				byte[] arr = miniByteBuffer.array();
				int i2 = i * 2;
				theChunk[i2 + 1] = arr[0];
				theChunk[i2] = arr[1];
			}

		} else {
			byte[] chunk = reader.nextChunk();

			if (chunk == null) return -1;
			if (chunk.length == 0) return -1;

			byte[] chunkClone = chunk.clone();
			theChunk = chunkClone;
		}

		addChunkToBuffer(theChunk);

		return 0;
	}

	public IPhaseVocoder getVocoder(){
		return vocoder;
	}

	protected void addChunkToBuffer(byte[] chunk){
		synchronized (buffer){
			buffer.add(chunk);
		}
	}

	public void setSlowScale(double scale) {
		slow = scale < .99;
		this.vocoder.setScale(scale);
	}

	public boolean isSlow() {
		return slow;
	}
}
