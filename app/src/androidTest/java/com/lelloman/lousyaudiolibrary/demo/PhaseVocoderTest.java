package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.IPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderMultiThread;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderOld;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.JavaPhaseVocoder;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;


public class PhaseVocoderTest {

	@Rule
	public ActivityTestRule<VocoderTestActivity> mActivityRule = new ActivityTestRule<>(VocoderTestActivity.class);

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Test
	public void nativeArrayCopyPerformanceTest(){
		Fft fft = new Fft(8);

		int SIZE = 1 << 26;
		int ITERATIONS = 1 << 2;

		long start = SystemClock.elapsedRealtimeNanos();
		fft.testArrayCopySingleThread(SIZE, ITERATIONS);
		long duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("array copy single thread duration %s", duration));

		start = SystemClock.elapsedRealtimeNanos();
		fft.testArrayCopyMultiThread(SIZE, ITERATIONS);
		duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("array copy  multi thread duration %s", duration));
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Test
	public void phaseVocoderPerformanceTest() {
		int N = 4096 * 16;
		int H = N / 16;
		double tscale = .5;

		IAudioReader javaReader = makeDummyAudioReader(440, 30);
		IPhaseVocoder javaVocoder = new JavaPhaseVocoder(javaReader, tscale,N, H);

		IAudioReader nativeReaderOld = makeDummyAudioReader(440, 30);
		IPhaseVocoder nativeVocoderOld = new NativePhaseVocoderOld(nativeReaderOld, tscale, N, H);

		IAudioReader nativeReader = makeDummyAudioReader(440, 30);
		IPhaseVocoder nativeVocoder = new NativePhaseVocoder(nativeReader, tscale, N, H);

		IAudioReader nativeReaderMulti = makeDummyAudioReader(440, 30);
		IPhaseVocoder nativeVocoderMulti = new NativePhaseVocoderMultiThread(nativeReaderMulti, tscale, N, H);

		long start = SystemClock.elapsedRealtimeNanos();
		while(!javaReader.getSawOutputEOS()) {
			javaVocoder.next();
		}
		long duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed       java  vocoder = %s", duration));


		start = SystemClock.elapsedRealtimeNanos();
		while(!nativeReaderOld.getSawOutputEOS()){
			nativeVocoderOld.next();
		}
		duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed   native vocoderOld = %s", duration));


		start = SystemClock.elapsedRealtimeNanos();
		while(!nativeReader.getSawOutputEOS()){
			nativeVocoder.next();
		}
		duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed      native vocoder = %s", duration));

		start = SystemClock.elapsedRealtimeNanos();
		while(!nativeReaderMulti.getSawOutputEOS()){
			nativeVocoderMulti.next();
		}
		duration = SystemClock.elapsedRealtimeNanos() - start;
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed multithread vocoder = %s", duration));
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
			IPhaseVocoder vocoder = new JavaPhaseVocoder(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() java expected = %s actual = %.2f", expected, actual));
			Assert.assertTrue(actual < expected + 5 && actual > expected - 5);
		}

		for(int i=0;i<7;i++) {
			int expected = 55 << i;
			IAudioReader reader = makeDummyAudioReader(expected, 2);
			IPhaseVocoder vocoder = new NativePhaseVocoderOld(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() native old expected = %s actual = %.2f", expected, actual));
			Assert.assertTrue(actual < expected + 5 && actual > expected - 5);
		}

		for(int i=0;i<7;i++) {
			int expected = 55 << i;
			IAudioReader reader = makeDummyAudioReader(expected, 2);
			IPhaseVocoder vocoder = new NativePhaseVocoder(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() native expected = %s actual = %.2f", expected, actual));
			Assert.assertTrue(actual < expected + 5 && actual > expected - 5);
		}

		for(int i=0;i<7;i++) {
			int expected = 55 << i;
			IAudioReader reader = makeDummyAudioReader(expected, 2);
			IPhaseVocoder vocoder = new NativePhaseVocoderMultiThread(reader, tscale, N, H);
			double actual = testVocoderWithFrequency(vocoder, reader);
			Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("phaseVocoderFunctionalTest() multi thread expected = %s actual = %.2f", expected, actual));
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