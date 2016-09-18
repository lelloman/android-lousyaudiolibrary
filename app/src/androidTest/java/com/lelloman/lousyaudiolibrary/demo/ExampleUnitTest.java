package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.IPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.PhaseVocoder;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;


public class ExampleUnitTest {

	@Rule
	public ActivityTestRule<VocoderTestActivity> mActivityRule = new ActivityTestRule<>(VocoderTestActivity.class);


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Test
	public void phaseVocoderPerformanceTest() {
		int N = 4096 * 4;
		int H = N / 8;
		double tscale = .5;

		IAudioReader javaReader = makeDummyAudioReader(440, 30);
		PhaseVocoder javaVocoder = new PhaseVocoder(javaReader, tscale,N, H);

		IAudioReader nativeReader = makeDummyAudioReader(440, 30);
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

	private DummyAudioReader makeDummyAudioReader(int frequency, int seconds){
		return new DummyAudioReader(44100 * seconds, 44100, frequency, 0, 4096);
	}

	@Test
	public void phaseVocoderFunctionalTest(){
		int N = 4096 * 8;
		int H = N / 4;
		double tscale = .5;

		for(int i=0;i<7;i++) {
			int expected = 55 << i;
			IAudioReader reader = makeDummyAudioReader(expected, 2);
			IPhaseVocoder vocoder = new PhaseVocoder(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(ExampleUnitTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() java expected = %s actual = %.2f", expected, actual));
			Assert.assertTrue(actual < expected + 5 && actual > expected - 5);
		}

		for(int i=0;i<7;i++) {
			int expected = 55 << i;
			IAudioReader reader = makeDummyAudioReader(expected, 2);
			IPhaseVocoder vocoder = new NativePhaseVocoder(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(ExampleUnitTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() native expected = %s actual = %.2f", expected, actual));
			Assert.assertTrue(actual < expected + 5 && actual > expected - 5);
		}

	}

	private double testVocoderWithFrequency(IPhaseVocoder vocoder, IAudioReader reader){
		int H = vocoder.getH();

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
}