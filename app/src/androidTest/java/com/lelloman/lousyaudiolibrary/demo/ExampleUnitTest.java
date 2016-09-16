package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.PhaseVocoder;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import org.junit.Rule;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

	@Rule
	public ActivityTestRule<VocoderTestActivity> mActivityRule = new ActivityTestRule<>(VocoderTestActivity.class);


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Test
	public void phaseVocoderTest() {
		int N = 4096 * 4;
		int H = N / 8;
		double tscale = .5;

		IAudioReader javaReader = makeDummyAudioReader();
		PhaseVocoder javaVocoder = new PhaseVocoder(javaReader, tscale,N, H);

		IAudioReader nativeReader = makeDummyAudioReader();
		NativePhaseVocoder nativeVocoder = new NativePhaseVocoder(nativeReader, tscale, N, H);

		long start = SystemClock.elapsedRealtimeNanos();
		while(!javaReader.getSawOutputEOS()) {
			javaVocoder.next();
		}
		long duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(ExampleUnitTest.class.getSimpleName(), String.format("elapsed java vocoder = %s", duration));

		start = SystemClock.elapsedRealtimeNanos();
		while(!nativeReader.getSawOutputEOS()){
			nativeVocoder.next();
		}
		duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(ExampleUnitTest.class.getSimpleName(), String.format("elapsed natives vocoder = %s", duration));
	}

	private DummyAudioReader makeDummyAudioReader(){
		return new DummyAudioReader(44100 * 10, 44100, 440, 0, 4096);
	}
}