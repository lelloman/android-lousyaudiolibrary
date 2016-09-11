package com.lelloman.lousyaudiolibrary.player;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BufferedAudioPlayer extends AudioPlayer {

	public static int MIN_BUFFER_SIZE = 4;

	protected List<byte[]> buffer = Collections.synchronizedList(new ArrayList<byte[]>());
	private int minBufferSize = MIN_BUFFER_SIZE;

	private Runnable feeder = new Runnable() {

		@Override
		public void run() {
			int n = 0;

			while (running) {
				synchronized (buffer) {
					n = buffer.size();
				}
				int min = getMinBufferSize();
				if (n < min) {
					int q = min - n;
					for (int i = 0; i < q; i++) {
						i += fillBuffer();
					}
				}

			}
		}
	};

	protected int getMinBufferSize() {
		return MIN_BUFFER_SIZE;
	}

	public BufferedAudioPlayer(EventsListener listener, int minBufferSize) {
		super(listener);

		if (minBufferSize >= this.minBufferSize) {
			this.minBufferSize = minBufferSize;
		}else {
			Log.e(BufferedAudioPlayer.class.getSimpleName(), String.format("%s is not a valid minBufferSize, it must be greater than %s", minBufferSize, this.minBufferSize));
			// TODO: throw an Exception?
		}
	}

	public BufferedAudioPlayer(EventsListener listener) {
		super(listener);
	}

	@Override
	public boolean start() {
		boolean sup = super.start();
		if (!sup) return false;

		new Thread(feeder).start();
		return true;
	}

	protected int fillBuffer() {
		byte[] chunk = reader.nextChunk();

		if (chunk == null) return -1;
		if (chunk.length == 0) return -1;

		synchronized (buffer) {
			buffer.add(chunk.clone());
		}

		return 0;
	}

	@Override
	public void seek(long pos) {
		super.seek(pos);
		synchronized (buffer){
			buffer.clear();
		}
	}

	@Override
	public void seek(double percent) {
		super.seek(percent);
		synchronized (buffer){
			buffer.clear();
		}
	}

	@Override
	protected byte[] getNextChunk() {
		synchronized (buffer) {
			if (buffer.size() > 0) {
				return buffer.remove(0);
			}
		}
		return null;
	}
}
