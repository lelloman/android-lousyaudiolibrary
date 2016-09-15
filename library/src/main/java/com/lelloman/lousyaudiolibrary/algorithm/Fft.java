package com.lelloman.lousyaudiolibrary.algorithm;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

public class Fft {
	static {
		System.loadLibrary("mylib");
	}

	// size of the output (complex)
	public final int size;

	ByteBuffer byteBuffer;

	public Fft(int size){
		this.size = size;

		int nativeBufferSize = Double.BYTES * size;
		byteBuffer = ByteBuffer.allocateDirect(nativeBufferSize);
		byteBuffer.order(ByteOrder.nativeOrder());

	}

	public void realForward(double[] data){
		DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
		doubleBuffer.put(data);
		dummy(byteBuffer, size);
		byteBuffer.asDoubleBuffer().get(data);
	}

	private native void dummy(ByteBuffer byteBuffer, int size);
}
