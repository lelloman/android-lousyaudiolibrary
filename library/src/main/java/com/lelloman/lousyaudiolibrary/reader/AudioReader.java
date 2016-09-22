package com.lelloman.lousyaudiolibrary.reader;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioReader implements IAudioReader {

	public static final int CODEC_TIMEOUT_US = 1000;
	public static final int NO_OUTPUT_COUNTER_LIMIT = 10;

	private MediaExtractor extractor;
	private MediaCodec codec;
	protected MediaCodec.BufferInfo info;

	private MediaFormat format;
	private String mime;
	private int sampleRate;
	private int channels;
	private long durationUs;
	private long durationMs;
	private long durationFrames;
	private int bitRate;

	private double percent = 0;
	private long currentMs = 0;
	private long currentUs = 0;

	protected int noOutputCounter = 0;

	private byte[] chunk = null;
	private double[] chunkDouble = null;

	// pre lollipop
	ByteBuffer[] codecInputBuffers;
	ByteBuffer[] codecOutputBuffers;

	// lollipop+
	ByteBuffer inputBuffer;
	ByteBuffer outputBuffer;

	protected boolean released = false;

	private boolean sawInputEOS = false;
	protected boolean sawOutputEOS = false;

	public AudioReader(String src) throws Exception {
		extractor = new MediaExtractor();
		extractor.setDataSource(src);

		readHeader();
	}

	public AudioReader(Context context, int resId) throws Exception {
		extractor = new MediaExtractor();
		AssetFileDescriptor fd = context.getResources().openRawResourceFd(resId);
		extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
		fd.close();

		readHeader();
	}

	protected void readHeader() throws Exception {

		format = extractor.getTrackFormat(0);
		mime = format.getString(MediaFormat.KEY_MIME);
		sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
		channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

		durationUs = format.getLong(MediaFormat.KEY_DURATION);
		durationMs = durationUs / 1000;
		durationFrames = durationMs / 1000 * sampleRate;

		if (!mime.startsWith("audio/")) throw new Exception("AudioReader can only read audio file");

		codec = MediaCodec.createDecoderByType(mime);
		if (codec == null)
			throw new Exception(String.format("MediaCodec failed to instantiate for mime type <%s>", mime));

		codec.configure(format, null, null, 0);
		codec.start();

		extractor.selectTrack(0);

		info = new MediaCodec.BufferInfo();
		if (Build.VERSION.SDK_INT < 21) {
			codecInputBuffers = codec.getInputBuffers();
			codecOutputBuffers = codec.getOutputBuffers();
		}
	}

	public String getMimeType() {
		return mime;
	}

	@Override
	public byte[] nextChunk() {

		synchronized (this) {
			if (sawOutputEOS || released) return null;

			processInputBuffer();
			noOutputCounter++;
			processOutputBuffer();

			if (noOutputCounter > NO_OUTPUT_COUNTER_LIMIT) sawOutputEOS = true;

		}
		return chunk;
	}

	@Override
	public double[] nextChunkDouble() {
		byte[] chunk = nextChunk();

		if (chunk == null) return null;

		if (chunkDouble == null || chunkDouble.length != chunk.length / 2)
			chunkDouble = new double[chunk.length / 2];


		ByteBuffer bb = ByteBuffer.wrap(chunk);
		double shortMax = Short.MAX_VALUE;
		bb.order(ByteOrder.nativeOrder());
		for (int i = 0; i < chunkDouble.length; i++)
			chunkDouble[i] = bb.getShort() / shortMax;

		return chunkDouble;
	}

	protected void processInputBuffer() {

		if (sawInputEOS) return;

		int inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_US);
		if (inputBufIndex >= 0) {
			if (Build.VERSION.SDK_INT < 21)
				inputBuffer = codecInputBuffers[inputBufIndex];
			else
				inputBuffer = codec.getInputBuffer(inputBufIndex);

			int sampleSize = extractor.readSampleData(inputBuffer, 0);
			if (sampleSize < 0) {
				sawInputEOS = true;
				sampleSize = 0;
			} else {
				currentUs = extractor.getSampleTime();
				currentMs = currentUs / 1000;
				percent = durationMs == 0 ? 0 : (double) currentUs / durationUs;
			}

			codec.queueInputBuffer(inputBufIndex, 0, sampleSize, currentUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

			if (!sawInputEOS) extractor.advance();
		}
	}

	protected void processOutputBuffer() {

		int res = codec.dequeueOutputBuffer(info, CODEC_TIMEOUT_US);

		if (res >= 0) {
			if (info.size > 0) noOutputCounter = 0;

			int outputBufIndex = res;

			if (Build.VERSION.SDK_INT < 21) {
				outputBuffer = codecOutputBuffers[outputBufIndex];
			} else {
				outputBuffer = codec.getOutputBuffer(outputBufIndex);
			}

			processNativeOutputBuffer();

			outputBuffer.clear();

            /*if(chunk.length == 0)
				chunk = null;*/

			codec.releaseOutputBuffer(outputBufIndex, false);
			if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				sawOutputEOS = true;
			}
		} else if (Build.VERSION.SDK_INT < 21 && res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
			codecOutputBuffers = codec.getOutputBuffers();
		} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			MediaFormat oformat = codec.getOutputFormat();
			// TODO maybe one should do something about it other than logging
		} else {

		}
	}

	protected void processNativeOutputBuffer(){
		if (chunk == null) {
			chunk = new byte[info.size];
		} else if (chunk.length != info.size) {
			chunk = new byte[info.size];
		}

		outputBuffer.get(chunk);
	}

	@Override
	public long getDurationMs() {
		return durationMs;
	}

	@Override
	public long getCurrentMs() {
		return currentMs;
	}

	@Override
	public long getDurationFrames() {
		return durationFrames;
	}

	@Override
	public double getPercent() {
		return percent;
	}

	@Override
	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public int getChannels() {
		return channels;
	}

	@Override
	public int getBitRate() {
		return bitRate;
	}

	@Override
	public boolean getSawOutputEOS() {
		return sawOutputEOS;
	}

	@Override
	public void reset() {
		codec.flush();
		seek(0);
		sawOutputEOS = false;
		sawInputEOS = false;
	}

	@Override
	public void seek(long pos) {
		extractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	}

	@Override
	public void seek(double percent) {
		long pos = (long) (percent * durationUs);
		seek(pos);
	}

	@Override
	public void release() {

		if (released) return;

		synchronized (this) {
			codec.stop();
			codec.release();
			codec = null;
			released = true;
		}
	}
}
