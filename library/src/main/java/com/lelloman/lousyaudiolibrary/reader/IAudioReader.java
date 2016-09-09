package com.lelloman.lousyaudiolibrary.reader;


public interface IAudioReader {
	byte[] nextChunk();

	double[] nextChunkDouble();

	long getDurationMs();

	long getCurrentMs();

	double getPercent();

	int getSampleRate();

	int getChannels();

	int getBitRate();

	boolean getSawOutputEOS();

	void reset();

	void seek(long pos);

	void seek(double percent);

	void release();
}
