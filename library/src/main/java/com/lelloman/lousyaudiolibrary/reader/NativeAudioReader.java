package com.lelloman.lousyaudiolibrary.reader;


import android.content.Context;

import java.nio.ByteBuffer;

public class NativeAudioReader extends AudioReader {

	static {
		System.loadLibrary("mylib");
	}

	ByteBuffer nativeOutput;

	public NativeAudioReader(String src) throws Exception {
		super(src);
	}

	public NativeAudioReader(Context context, int resId) throws Exception {
		super(context, resId);
	}

	@Override
	protected void readHeader() throws Exception {
		super.readHeader();
		nativeOutput = ByteBuffer.allocateDirect(info.size);
	}

	public ByteBuffer nextNativeChunk() {

		synchronized (this) {
			if (sawOutputEOS || released) return nativeOutput;

			processInputBuffer();
			noOutputCounter++;
			processOutputBuffer();

			if (noOutputCounter > NO_OUTPUT_COUNTER_LIMIT) sawOutputEOS = true;

		}
		return nativeOutput;
	}

	public int getNativeChunkSize(){
		return info.size;
	}

	@Override
	protected void processNativeOutputBuffer() {
		nativeOutput.position(0);
		if(nativeOutput.limit() != outputBuffer.limit()){
			nativeOutput = ByteBuffer.allocateDirect(outputBuffer.limit());
		}
		nativeOutput.put(outputBuffer);
	}
}
