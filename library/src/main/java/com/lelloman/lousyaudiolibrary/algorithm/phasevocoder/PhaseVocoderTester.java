package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;


import android.os.SystemClock;

import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.lang.reflect.Constructor;

public class PhaseVocoderTester {

	public static long testVocoderPerformanceUs(Class vocoderClass, double scale, int N, int H, int durationSeconds){
		DummyAudioReader reader = new DummyAudioReader(44100 * durationSeconds, 44100, 440, 0, 4096);

		IPhaseVocoder vocoder;
		try {
			Constructor ctor = vocoderClass.getConstructor(IAudioReader.class, double.class, int.class, int.class);
			vocoder = (IPhaseVocoder) ctor.newInstance(reader, scale, N, H);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		long start = SystemClock.elapsedRealtimeNanos();
		while(!reader.getSawOutputEOS()){
			vocoder.next();
		}
		return SystemClock.elapsedRealtimeNanos() - start;
	}

	public static double testVocoderFunctionality(Class vocoderClass, double scale, int N, int H, int frequency){
		DummyAudioReader reader = new DummyAudioReader(44100 * 5, 44100, frequency, 0, 4096);

		IPhaseVocoder vocoder;
		try {
			Constructor ctor = vocoderClass.getConstructor(IAudioReader.class, double.class, int.class, int.class);
			vocoder = (IPhaseVocoder) ctor.newInstance(reader, scale, N, H);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		Fft fft = new Fft(H*2);
		double resolution = 44100. / H;
		double sum = 0;
		int count = 0;

		while(!reader.getSawOutputEOS()) {
			double[] frame = vocoder.next();
			fft.realForward(frame);
			double max = 0;
			int maxIndex = 0;
			for(int i=0;i<H/2;i+=2){
				double v = Math.sqrt(Math.pow(frame[i], 2) + Math.pow(frame[i+1], 2));
				if(v > max){
					max = v;
					maxIndex = i / 2;
				}
			}
			double freq = maxIndex * resolution;
			sum += freq;

			count++;
		}

		return sum / count;
	}

	private PhaseVocoderTester(){

	}

}
