package com.lelloman.lousyaudiolibrary.algorithm;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.Util;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.LinkedList;
import java.util.List;

public class Spectrogram {

	public static final String TAG = Spectrogram.class.getSimpleName();

	public final int bufferSize;
	public final int fftSize; // not counting complex
	private Fft fft;
	private IAudioReader audioReader;
	private BufferManager bufferManager;
	private double[] window;
	private double[] fftHolder;
	public final double resolution;
	private List<double[]> data;

	public Spectrogram(IAudioReader audioReader, int bufferSize, int stepFactor) {
		this.audioReader = audioReader;
		this.bufferSize = bufferSize;
		this.fftSize = bufferSize * 4;
		fft = new Fft(fftSize * 2);
		resolution = 44100. / fftSize;
		fftHolder = new double[fftSize * 2];
		int stepSize = bufferSize / stepFactor;
		bufferManager = new BufferManager(audioReader, bufferSize, stepSize);

		window = Util.hanning(bufferSize);
	}

	public List<double[]> getData(){
		return data;
	}

	public Spectrogram make() {

		int fftSize2 = fftSize * 2;
		data = new LinkedList<>();
		double k = 127. / fftSize2;

		while (!audioReader.getSawOutputEOS()) {
			double[] chunk = bufferManager.next();
			for (int i = 0; i < bufferSize; i++) {
				fftHolder[i] = window[i] * chunk[i];
			}
			for (int i = bufferSize; i < fftSize2; i++) {
				fftHolder[i] = 0;
			}
			fft.realForward(fftHolder);

			double[] values = new double[bufferSize];
			for (int i = 0; i < bufferSize; i++) {
				int i2 = i * 2;
				double value = Math.sqrt(Math.pow(fftHolder[i2], 2) + Math.pow(fftHolder[i2 + 1], 2));
				values[i] = value * k;
			}
			data.add(values);
		}

		return this;
	}
}
