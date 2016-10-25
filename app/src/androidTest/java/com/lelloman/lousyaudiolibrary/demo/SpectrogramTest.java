package com.lelloman.lousyaudiolibrary.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;

import com.lelloman.lousyaudiolibrary.algorithm.Spectrogram;
import com.lelloman.lousyaudiolibrary.reader.DummyAudioReader;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

public class SpectrogramTest {

    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Test
    public void mweh(){

        IAudioReader reader = new DummyAudioReader(44100*3,44100,440,0,2000);
        int size = 4096*2;
        Spectrogram spectrogram = new Spectrogram(reader,size, 4).make();

        Assert.assertTrue(spectrogram != null);
    }
}
