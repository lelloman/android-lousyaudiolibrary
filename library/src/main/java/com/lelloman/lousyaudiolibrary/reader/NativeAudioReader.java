package com.lelloman.lousyaudiolibrary.reader;


import android.content.Context;

import java.nio.ByteBuffer;

public class NativeAudioReader extends AudioReader {

	ByteBuffer nativeOutput;

	public NativeAudioReader(String src) throws Exception {
		super(src);
	}

	public NativeAudioReader(Context context, int resId) throws Exception {
		super(context, resId);
	}

	public ByteBuffer nextNativeChunk() {

		synchronized (this) {
			if (sawOutputEOS || released) return null;

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
		if(nativeOutput == null){
			nativeOutput = ByteBuffer.allocateDirect(info.size);
		}
		nativeOutput.put(outputBuffer);
	}
}
