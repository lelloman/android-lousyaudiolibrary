package com.lelloman.lousyaudiolibrary.demo;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.Util;
import com.lelloman.lousyaudiolibrary.algorithm.Fft;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import org.junit.Rule;
import org.junit.Test;

public class FftTest {

	@Rule
	public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

	@Test
	public void testFft(){

		int framerate = 44100;
		int length = framerate * 5;
		int frequency = 220;
		int bufferSize = 4096*2;
		double[] window = Util.hanning(bufferSize);
		int stepSize = bufferSize / 4;
		double binSize = 44100. / bufferSize;

		double[] fftHolder = new double[bufferSize * 2];

		IAudioReader reader = new DummyAudioReader(length, framerate, frequency, 0, 999);

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

			fft.realForward(fftHolder);

			double maxValue = 0;
			int maxIndex = 0;
			for(int i=0;i<bufferSize;i++){
				int i2 = i*2;
				double v = Math.sqrt(Math.pow(fftHolder[i2], 2) + Math.pow(fftHolder[i2+1], 2));
				if(v > maxValue){
					maxValue = v;
					maxIndex = i;
				}
			}

			Log.d(FftTest.class.getSimpleName(), String.format("max freq = %.2f v = %.2f", binSize * maxIndex, maxValue));
		}
	}
}
