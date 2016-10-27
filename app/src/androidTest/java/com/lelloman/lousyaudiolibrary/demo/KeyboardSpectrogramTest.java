package com.lelloman.lousyaudiolibrary.demo;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.spectrogram.KeyboardSpectrogram;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

public class KeyboardSpectrogramTest {

	public static final String TAG = KeyboardSpectrogramTest.class.getSimpleName();

	@Rule
	public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

	@Test
	public void testSpectrogramFunctionality() {
		int[] frequencies = new int[]{0,12,24,36,48};

		for (int freq : frequencies)
			testSpectrogramWithFrequency(freq);
	}

	public void testSpectrogramWithFrequency(final int key) {

		final int frequency = (int) KeyboardSpectrogram.frequency(key);
		IAudioReader reader = new DummyAudioReader(44100 * 3, 44100, frequency, .7, 2000);
		int size = 4096 * 2;
		int stepFactor = 4;
		new KeyboardSpectrogram(reader, size, stepFactor, new KeyboardSpectrogram.KeyboardSpectrogramListener() {
			@Override
			public void onNewKeyboardFrame(double[] frame) {
				//Log.d(TAG, Arrays.toString(frame));
				int maxIndex = -1;
				double maxValue = 0;

				for(int i=0;i<84;i++){
					double v = frame[i];
					if(maxValue < v){
						maxValue = v;
						maxIndex = i;
					}
				}
				Log.d(TAG, String.format("expected freq %s key %s actual %s", frequency, key, maxIndex));
				Assert.assertEquals(key, maxIndex);
			}

			@Override
			public void onReadKeyboardFramesEnd() {

			}
		}).make();
	}
}
