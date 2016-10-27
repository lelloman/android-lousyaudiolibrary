package com.lelloman.lousyaudiolibrary.algorithm.spectrogram;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

public class KeyboardSpectrogram extends Spectrogram {

	private static final double[] frequencies = new double[84];
	static {
		for(int i=0;i<84;i++){
			int index = i;
			double fi = frequency(index);
			frequencies[i] = fi + (frequency(index+1) - fi) * .5;
		}
	}
	public static double frequency(int n){
		return 55.0 * Math.pow(1.0594630943592953,n);
	}

	public interface KeyboardSpectrogramListener {
		void onNewKeyboardFrame(double[] frame);
	}

	private KeyboardSpectrogramListener listener;

	public KeyboardSpectrogram(IAudioReader audioReader, int bufferSize, int stepFactor, KeyboardSpectrogramListener listener) {
		super(audioReader, bufferSize, stepFactor);
		this.listener = listener;
	}

	@Override
	protected void onData(double[] data, double resolution) {
		double[] pitches = new double[84];
		int dataLength = data.length;
		for(int i=2;i<dataLength;i++){
			double actualF = i * resolution;
			if(actualF < 54) continue;

			for(int j=0;j<84;j++){
				if(actualF < frequencies[j]){
					pitches[j] += data[i];
					break;
				}
			}
		}

		listener.onNewKeyboardFrame(pitches);
	}
}
