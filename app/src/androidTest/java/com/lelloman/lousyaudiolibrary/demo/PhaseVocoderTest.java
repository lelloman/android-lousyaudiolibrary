package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.IPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.JavaPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderOld;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.PhaseVocoderTester;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.VocoderType;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;


public class PhaseVocoderTest {

	@Rule
	public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

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
		int N = 4096 * 2;
		int H = N / 16;
		double tscale = .5;
		int seconds = 5;

		long duration = PhaseVocoderTester.testVocoderPerformanceMs(JavaPhaseVocoder.class, tscale, N, H, seconds);
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed       java  vocoder = %s", duration));

		duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoderOld.class, tscale, N, H, seconds);
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed   native vocoderOld = %s", duration));


		duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoder.class, tscale, N, H, seconds);
		Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed      native vocoder = %s", duration));

		//duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoderMultiThread.class, tscale, N, H, seconds);
		//Log.d(PhaseVocoderTest.class.getSimpleName(), String.format("elapsed multithread vocoder = %s", duration));
	}

	@Test
	public void phaseVocoderFunctionalTest(){

		VocoderType[] types = PhaseVocoderTester.getFunctionalVocoderTypes(55, 7, true);
		Assert.assertEquals(VocoderType.ALL.length, types.length);

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