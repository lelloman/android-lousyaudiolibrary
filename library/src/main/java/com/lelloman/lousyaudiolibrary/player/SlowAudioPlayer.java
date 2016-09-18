package com.lelloman.lousyaudiolibrary.player;

import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.IPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderOld;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.nio.ByteBuffer;

public class SlowAudioPlayer extends BufferedAudioPlayer {

	protected static final int FRAME_SIZE = 4096 * 8;
	protected static final int HOP = FRAME_SIZE / 8;
	protected static final float SCALE = .5f;

	private boolean slow = false;

	private IPhaseVocoder vocoder = null;
	private ByteBuffer miniByteBuffer = ByteBuffer.allocate(2);

	public SlowAudioPlayer(EventsListener listener) {
		super(listener);
	}

	@Override
	public boolean init(IAudioReader reader) {
		boolean output = super.init(reader);
		if (output) initVocoder();
		return output;
	}

	private void initVocoder() {
		if (reader == null) return;
		vocoder = new NativePhaseVocoderOld(reader, SCALE, FRAME_SIZE, HOP);
		//vocoder = new PhaseVocoder(reader, SCALE, FRAME_SIZE, HOP);
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
