package com.lelloman.lousyaudiolibrary.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.Arrays;

public class AudioPlayer implements Runnable {

	public interface EventsListener {
		void onStart(AudioPlayer player);

		void onPlaybackUpdate(AudioPlayer player, double percent, long timeMs);

		void onRelease(AudioPlayer player);
	}

	public static final int CREATED = 0;
	public static final int READY_TO_PLAY = 1;
	public static final int PLAYING = 2;
	public static final int PAUSED = 3;
	public static final int RELEASED = 4;
	public static final int ABORTED = 5;

	protected IAudioReader reader;
	private AudioTrack audioTrack;

	protected boolean running = false;
	protected byte[] emptyChunk;

	private EventsListener listener;

	private int state = CREATED;

	private long currentMs = 0;
	private double percent = 0;

	public AudioPlayer(EventsListener listener) {
		this.listener = listener;
	}

	public boolean init(IAudioReader reader) {
		try {
			this.reader = reader;
			initAudioTrack();
			state = READY_TO_PLAY;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			state = ABORTED;
			return false;
		}
	}

	protected void initAudioTrack() {
		int sampleRate = reader.getSampleRate();
		int channelConfiguration = reader.getChannels() == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
		int minSize = AudioTrack.getMinBufferSize(sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT);

		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelConfiguration,
				AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);

		audioTrack.play();

		emptyChunk = new byte[minSize];
		Arrays.fill(emptyChunk, (byte) 0);
	}

	public boolean start() {
		if (state == READY_TO_PLAY) {
			running = true;
			new Thread(this).start();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		state = PAUSED;

		listener.onStart(this);

		while (running) {

			byte[] chunk = emptyChunk;

			if (state != PAUSED) {
				chunk = getNextChunk();
				updateCurrentPosition();
				this.listener.onPlaybackUpdate(this, percent, currentMs);
			}
			if (chunk == null || chunk.length == 0) {
				chunk = emptyChunk;
				if (reader.getSawOutputEOS()) {
					reader.reset();
					this.listener.onPlaybackUpdate(this, percent, currentMs);
					pause();
				}
			}

			audioTrack.write(chunk, 0, chunk.length);
		}

		listener.onRelease(this);
		state = RELEASED;
		reader.release();

		audioTrack.flush();
		audioTrack.release();
		audioTrack = null;
	}

	protected byte[] getNextChunk() {
		return reader.nextChunk();
	}

	public double getCurrentPercent() {
		return percent;
	}

	public void kill() {
		running = false;
	}

	public void pause() {
		if (state == PLAYING)
			state = PAUSED;
		else
			Log.w(AudioPlayer.class.getSimpleName(), String.format("pause() called on player not in state PLAYING (current state is %s", state));
	}

	public void play() {
		if (state == PAUSED)
			state = PLAYING;
		else
			Log.w(AudioPlayer.class.getSimpleName(), String.format("play() called on player not in state PAUSED (current state is %s)", state));
	}

	private void updateCurrentPosition() {
		this.percent = reader.getPercent();
		this.currentMs = reader.getCurrentMs();
	}

	public void seek(long pos) {
		reader.seek(pos);
		updateCurrentPosition();
	}

	public void seek(double percent) {
		reader.seek(percent);
		updateCurrentPosition();

	}

	public long getCurrentMs() {
		return currentMs;
	}

	public long getDurationMs() {
		return reader.getDurationMs();
	}
}
