package com.lelloman.lousyaudiolibrary.demo;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.spectrogram.Spectrogram;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static android.R.attr.data;

public class SpectrogramTest {

    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

	@Test
	public void testSpectrogramFunctionality(){
		int[] frequencies = new int[]{55,110,220,440,880};

		for(int freq : frequencies)
			testSpectrogramWithFrequency(freq);
	}

    public void testSpectrogramWithFrequency(int frequency){

        IAudioReader reader = new DummyAudioReader(44100*3,44100,frequency,.7,2000);
        int size = 4096*2;
		int stepFactor = 4;

		final double min = frequency - 5;
		final double max = frequency + 5;

        new Spectrogram(reader, size, stepFactor) {
			@Override
			protected void onData(double[] data, double resolution) {
				int maxIndex = -1;
				double maxValue = 0;
				for (int i = 0; i < data.length; i++) {
					double v = data[i];
					if(v > maxValue){
						maxValue = v;
						maxIndex = i;
					}
				}
				double maxFreq = resolution * maxIndex;
				Log.d(SpectrogramTest.class.getSimpleName(), String.format("max frequency %.2f", maxFreq));
				Assert.assertTrue(maxFreq < max && maxFreq > min);
			}
		}.make();

    }
}
