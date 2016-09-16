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

		forward(byteBuffer, size);
		byteBuffer.asDoubleBuffer().get(data);
	}

	public void realInverse(double[] data){
		DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
		doubleBuffer.put(data);

		inverse(byteBuffer, size, true);
		byteBuffer.asDoubleBuffer().get(data);
	}

	private native void forward(ByteBuffer byteBuffer, int size);
	private native void inverse(ByteBuffer byteBuffer, int size, boolean scale);
	private native void dummy(ByteBuffer byteBuffer, int size);
}
