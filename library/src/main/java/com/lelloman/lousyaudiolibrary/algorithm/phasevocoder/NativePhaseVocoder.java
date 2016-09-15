package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import org.jtransforms.fft.DoubleFFT_1D;


public class NativePhaseVocoder implements IPhaseVocoder {

	public static final double PI2 = Math.PI * 2;

	private double tscale;
	private int N, H, N2, halfN, NmH;

	private double[] phi;// = new double[N];
	private double[] out;// = new double[N*2]; // complex
	private double[] spec1;// = new double[N*2]; // complex
	private double[] spec2;// = new double[N*2]; // complex
	private double[] sigout;// = new double[(int) (L / tscale+N)];
	private double[] win;// = new double[N];
	private double[] buffer;
	private double[] output;// = new double[H];
	private Fft fft;// = new DoubleFFT_1D(N);
	private DoubleFFT_1D jfft;

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
		out = new double[N2];
		spec1 = new double[N2];
		spec2 = new double[N2];
		sigout = new double[N];
		fft = new Fft(N2);
		jfft = new DoubleFFT_1D(N);
		output = new double[H];

		win = new double[N];
		for (int i = 0; i < N; i++) {
			double j = (2 * Math.PI * i) / (N - 1);
			double k = 1 - Math.cos(j);
			win[i] = .5 * k;
		}

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


		for (int i = 0; i < NmH; i++)
			sigout[i] = sigout[i + H];
		for (int i = NmH; i < N; i++)
			sigout[i] = 0;

		makeSpec(spec1, 0);
		makeSpec(spec2, H);

		makePhi();
		makeOut();

		for (int i = 0; i < N2; i++)
			spec2[i] = Math.abs(spec2[i]) * out[i];


		jfft.realInverse(spec2, true);
	//	fft.realInverse(spec2);

		for (int i = 0; i < N; i++)
			sigout[i] += win[i] * spec2[i];

		System.arraycopy(sigout, 0, output, 0, H);
		return output;
	}

	@Override
	public double[] getCurrentFftFrame(){
		return spec1;
	}

	private void makeSpec(double[] spec, int offset) {
		for (int i = 0; i < halfN; i++)
			spec[i] = win[i] * buffer[i + offset];
		for (int i = halfN; i < N; i++)
			spec[i] = 0;

		fft.realForward(spec);
	}

	private void makePhi() {
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
	}

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
