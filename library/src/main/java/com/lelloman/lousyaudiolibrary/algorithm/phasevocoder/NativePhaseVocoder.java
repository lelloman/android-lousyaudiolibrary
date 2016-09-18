package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class NativePhaseVocoder implements IPhaseVocoder {

	public static final double PI2 = Math.PI * 2;

	private double tscale;
	private int N, H, N2, halfN, NmH;

	private double[] phi;// = new double[N];
	private ByteBuffer phiNio;
	private double[] out;// = new double[N*2]; // complex
	private ByteBuffer outNio;
	private double[] spec1;// = new double[N*2]; // complex
	private double[] spec2;// = new double[N*2]; // complex
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
	private Fft fft;// = new DoubleFFT_1D(N);

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
		phi = new double[N];
		phiNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N);
		phiNio.order(ByteOrder.nativeOrder());
		out = new double[N2];
		outNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		outNio.order(ByteOrder.nativeOrder());

		spec1 = new double[N2];
		spec2 = new double[N2];
		specNio1 = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		specNio1.order(ByteOrder.nativeOrder());
		specNio2 = ByteBuffer.allocateDirect(Double.SIZE / 8 * N2);
		specNio2.order(ByteOrder.nativeOrder());
		sigout = new double[N];
		sigoutNio = ByteBuffer.allocateDirect(Double.SIZE / 8 * N);
		sigoutNio.order(ByteOrder.nativeOrder());

		fft = new Fft(N2);
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

	@Override
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
		sigoutNio.asDoubleBuffer().put(sigout);

		makeSpec(bufferNio, specNio1, specNio2, phiNio, sigoutNio, outNio, windowNio, N2, H);

	//	specNio1.asDoubleBuffer().get(spec1);
		specNio2.asDoubleBuffer().get(spec2);
		phiNio.asDoubleBuffer().get(phi);
		sigoutNio.asDoubleBuffer().get(sigout);
		outNio.asDoubleBuffer().get(out);

	//	makePhi();
	//	makeOut();

		for (int i = 0; i < N2; i++)
			spec2[i] = Math.abs(spec2[i]) * out[i];

		fft.realInverse(spec2);
		//jfft.realInverse(spec2, true);

		for (int i = 0; i < N; i++)
			sigout[i] += win[i] * spec2[i];

		System.arraycopy(sigout, 0, output, 0, H);
		return output;
	}

	@Override
	public double[] getCurrentFftFrame(){
		return spec1;
	}

	private native void makeSpec(ByteBuffer bufferNio, ByteBuffer spec1, ByteBuffer spec2, ByteBuffer phiNio, ByteBuffer sigout, ByteBuffer outNio, ByteBuffer window, int specSize, int offset);

	/*private void makePhi() {
		for (int i = 0; i < phi.length; i++) {
			int i2 = i * 2;
			int i21 = i2 + 1;

			double a1 = spec1[i2];
			double b1 = spec1[i21];
			double a2 = spec2[i2];
			double b2 = spec2[i21];

			double p = phi[i] + (Math.atan2(b2, a2) - Math.atan2(b1, a1));
			while (p < -Math.PI) p += PI2;
			while (p > Math.PI) p -= PI2;
			phi[i] = p;
		}
	}*/

	// lol
	private void makeOut() {
		for (int i = 0; i < phi.length; i++) {
			int i2 = i * 2;
			int i21 = i2 + 1;

			double p = phi[i];
			out[i2] = Math.cos(p);
			out[i21] = Math.sin(p);
		}
	}
}
