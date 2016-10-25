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
		int nativeBufferSize = Double.SIZE / 8 * size;
		byteBuffer = ByteBuffer.allocateDirect(nativeBufferSize);
		byteBuffer.order(ByteOrder.nativeOrder());

	}

	public void realForward(double[] data){
		DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
		doubleBuffer.put(data);

		forward(byteBuffer, size/2);
		byteBuffer.asDoubleBuffer().get(data);
	}

	public void realForwardMagnitude(double[] data){
		DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
		doubleBuffer.put(data);

		forwardMagnitude(byteBuffer, size/2);
		byteBuffer.asDoubleBuffer().get(data);
	}

	public void realInverse(double[] data){
		DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
		doubleBuffer.put(data);

		inverse(byteBuffer, size/2, true);
		byteBuffer.asDoubleBuffer().get(data);
	}

	private native void forward(ByteBuffer byteBuffer, int size);
	private native void forwardMagnitude(ByteBuffer byteBuffer, int size);
	private native void inverse(ByteBuffer byteBuffer, int size, boolean scale);
	private native void dummy(ByteBuffer byteBuffer, int size);
	public native void testArrayCopySingleThread(int size, int iterations);
	public native void testArrayCopyMultiThread(int size, int iterations);
}
