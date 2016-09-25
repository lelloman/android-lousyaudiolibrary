package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class NativePhaseVocoder implements IPhaseVocoder {
	static {
		System.loadLibrary("mylib");
	}

	public static final double PI2 = Math.PI * 2;

	private double tscale;
	private int N, H, N2, halfN, NmH;

	private ByteBuffer phiNio;
	private ByteBuffer outNio;
	private ByteBuffer specNio1;
	private ByteBuffer specNio2;
	private double[] sigout;// = new double[(int) (L / tscale+N)];
	private ByteBuffer sigoutNio;

	private double[] win;// = new double[N];
	private ByteBuffer windowNio;
	private double[] buffer;
	private ByteBuffer bufferNio;
	private int bufferNioSize;
	private double[] output;// = new double[H];

	private IAudioReader audioReader;
	private BufferManager manager;
	private boolean slow;

	public NativePhaseVocoder(IAudioReader reader, double tscale, int N, int H) {

		this.audioReader = reader;
		this.N = N;
		this.H = H;
		setScale(tscale);

		N2 = N * 2;
		halfN = N / 2;
		NmH = N - H;

		phiNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N);
		phiNio.order(ByteOrder.nativeOrder());
		outNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		outNio.order(ByteOrder.nativeOrder());

		specNio1 = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		specNio1.order(ByteOrder.nativeOrder());
		specNio2 = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		specNio2.order(ByteOrder.nativeOrder());
		sigout = new double[N];
		sigoutNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N);
		sigoutNio.order(ByteOrder.nativeOrder());

		output = new double[H];

		win = new double[N];
		for (int i = 0; i < N; i++) {
			double j = (2 * Math.PI * i) / (N - 1);
			double k = 1 - Math.cos(j);
			win[i] = .5 * k;
		}
		windowNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N);
		windowNio.order(ByteOrder.nativeOrder());
		windowNio.asDoubleBuffer().put(win);
	}

	@Override
	public int getH() {
		return H;
	}

	@Override
	public int getN() {
		return N;
	}

	@Override
	public void setScale(double v) {
		this.tscale = v;
		int stepSize = (int) (H * tscale);
		this.manager = new BufferManager(audioReader, N + H, stepSize);
		slow = v < .99;
	}

	public double[] next() {

		buffer = manager.next();
		if (buffer == null) return null;

		if(buffer.length != bufferNioSize || bufferNio == null){
			bufferNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * buffer.length);
			bufferNio.order(ByteOrder.nativeOrder());
			bufferNioSize = buffer.length;
		}
		bufferNio.position(0);
		bufferNio.asDoubleBuffer().put(buffer);

		next(bufferNio, specNio1, specNio2, phiNio, sigoutNio, outNio, windowNio, N2, H);

		sigoutNio.asDoubleBuffer().get(sigout);


		System.arraycopy(sigout, 0, output, 0, H);
		return output;
	}

	private native void next(ByteBuffer bufferNio, ByteBuffer spec1, ByteBuffer spec2, ByteBuffer phiNio, ByteBuffer sigout, ByteBuffer outNio, ByteBuffer window, int specSize, int offset);

}
