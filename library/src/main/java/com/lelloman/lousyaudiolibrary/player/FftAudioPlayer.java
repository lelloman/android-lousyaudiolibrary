package com.lelloman.lousyaudiolibrary.player;


import org.jtransforms.fft.DoubleFFT_1D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FftAudioPlayer extends SlowAudioPlayer{

	public interface EventsListener extends AudioPlayer.EventsListener {
		void onFftFrame(double[] fftFrame);
	}

	private EventsListener listener;

	private int lastChunkSize = -1;
	DoubleFFT_1D fft = new DoubleFFT_1D(2);
	private double[] fftHolder = new double[2];
	private double[] fftOutput = new double[2];
	private double[] window = new double[2];

	public FftAudioPlayer(EventsListener listener) {
		super(listener);

		this.listener = listener;
	}

	@Override
	protected void addChunkToBuffer(byte[] chunk) {
		super.addChunkToBuffer(chunk);

		if(isSlow()){
			listener.onFftFrame(getVocoder().getCurrentFftFrame());
		}else{
			listener.onFftFrame(computeFftFrame(chunk));
		}

	}

	private void makeWindow(int N){
		window = new double[N];
		for (int i = 0; i < N; i++) {
			double j = (2 * Math.PI * i) / (N - 1);
			double k = 1 - Math.cos(j);
			window[i] = .5 * k;
		}
	}

	private double[] computeFftFrame(byte[] pcmFrame){

		if(pcmFrame == null || pcmFrame.length < 2){
			return fftHolder;
		}

		if(pcmFrame.length != lastChunkSize){
			fftHolder = new double[pcmFrame.length * 2];
			fftOutput = new double[fftHolder.length / 4];
			makeWindow(pcmFrame.length / 2);
			lastChunkSize = pcmFrame.length;
			fft = new DoubleFFT_1D(fftHolder.length);
		}

		int n = window.length;
		int N = fftHolder.length;

		ByteBuffer shortByteBuffer = ByteBuffer.wrap(pcmFrame);
		shortByteBuffer.order(ByteOrder.nativeOrder());
		double shortMax = Short.MAX_VALUE;

		for(int i=0;i<n;i++){
			double v = shortByteBuffer.getShort() / shortMax;
			fftHolder[i] = window[i] * v;
		}
		for(int i=n;i<N;i++){
			fftHolder[i] = 0;
		}

		fft.realForward(fftHolder);

		for(int i=0;i<fftOutput.length;i++){
			int i2 = i*2;
			double a = fftHolder[i2];
			double b = fftHolder[i2+1];
			double mag = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
			fftOutput[i] = mag;
		}

		return fftOutput;
	}
}
