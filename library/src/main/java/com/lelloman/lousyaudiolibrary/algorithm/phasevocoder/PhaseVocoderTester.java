package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;


import android.os.SystemClock;

import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.lang.reflect.Constructor;

public class PhaseVocoderTester {

	public static long testVocoderUs(Class vocoderClass, double scale, int N, int H, int durationSeconds){
		DummyAudioReader audioReader = new DummyAudioReader(44100 * durationSeconds, 44100, 440, 0, 4096);

		IPhaseVocoder vocoder;
		try {
			Constructor ctor = vocoderClass.getConstructor(IAudioReader.class, double.class, int.class, int.class);
			//Constructor ctor = vocoderClass.getConstructors()[0];

			vocoder = (IPhaseVocoder) ctor.newInstance(audioReader, scale, N, H);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		long start = SystemClock.elapsedRealtimeNanos();
		while(!audioReader.getSawOutputEOS()){
			vocoder.next();
		}
		return SystemClock.elapsedRealtimeNanos() - start;
	}

	private PhaseVocoderTester(){

	}

}
