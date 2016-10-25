package com.lelloman.lousyaudiolibrary.demo;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.Util;
import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

public class FftTest {

	@Rule
	public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

	@Test
	public void testFftFunctionality(){
		int[] frequencies = new int[]{
				55, 110, 220, 440, 880
		};

		for(int freq : frequencies)
			testFftWithFrequency(freq);
	}

	public void testFftWithFrequency(int frequency){

		int framerate = 44100;
		int length = framerate * 5;
		double min = frequency - 5;
		double max = frequency + 5;
		int bufferSize = 4096*2;
		double[] window = Util.hanning(bufferSize);
		int stepSize = bufferSize / 4;
		double binSize = 44100. / bufferSize;

		double[] fftHolder = new double[bufferSize * 2];

		IAudioReader reader = new DummyAudioReader(length, framerate, frequency, .9, 999);

		BufferManager bufferManager = new BufferManager(reader, bufferSize,stepSize);
		Fft fft = new Fft(bufferSize * 2);

		while(bufferManager.hasNext()){
			double[] chunk = bufferManager.next();

			for(int i=0;i<chunk.length;i++){
				fftHolder[i] = chunk[i] * window[i];
			}
			for(int i=chunk.length;i<fftHolder.length;i++){
				fftHolder[i] = 0;
			}

			fft.realForwardMagnitude(fftHolder);

			double maxValue = 0;
			int maxIndex = 0;
			for(int i=0;i<bufferSize;i++){
				double v = fftHolder[i];
				if(v > maxValue){
					maxValue = v;
					maxIndex = i;
				}
			}
			double maxFreq = binSize * maxIndex;
			Assert.assertTrue(maxFreq < max && maxFreq > min);

			Log.d(FftTest.class.getSimpleName(), String.format("max freq = %.2f v = %.2f", maxFreq, maxValue));
		}
	}
}
