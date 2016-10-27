package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.Spectrogram;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

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
        Spectrogram spectrogram = new Spectrogram(reader,size, stepFactor).make();

		List<double[]> data = spectrogram.getData();
		double resolution = spectrogram.resolution;
		double min = frequency - 5;
		double max = frequency + 5;
		for(double[] array : data) {
			int maxIndex = -1;
			double maxValue = 0;
			for (int i = 0; i < array.length; i++) {
				double v = array[i];
				if(v > maxValue){
					maxValue = v;
					maxIndex = i;
				}
			}
			double maxFreq = resolution * maxIndex;
			Log.d(SpectrogramTest.class.getSimpleName(), String.format("max freq %.2f", maxFreq));
			Assert.assertTrue(maxFreq < max && maxFreq > min);
		}

        Assert.assertTrue(spectrogram != null);
    }
}
