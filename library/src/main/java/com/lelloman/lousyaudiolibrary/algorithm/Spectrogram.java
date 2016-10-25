package com.lelloman.lousyaudiolibrary.algorithm;

import android.util.Log;

import com.lelloman.lousyaudiolibrary.BufferManager;
import com.lelloman.lousyaudiolibrary.Util;
import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Spectrogram {

    public static final String TAG = Spectrogram.class.getSimpleName();

    public final int fftSize;
    private Fft fft;
    private IAudioReader audioReader;
    private BufferManager bufferManager;
    private double[] window;
    private double[] fftHolder;
    private List<byte[]> data;

    public Spectrogram(IAudioReader audioReader, int fftSize, int stepFactor){
        this.audioReader = audioReader;
        this.fftSize = fftSize;
        fft = new Fft(fftSize * 2);
        fftHolder = new double[fftSize * 2];
        int stepSize = fftSize / stepFactor;
        bufferManager = new BufferManager(audioReader, fftSize - stepSize, stepSize);

        window = Util.hanning(fftSize);
    }

    public Spectrogram make(){
        int fftSize2 = fftSize * 2;
        data = new LinkedList<>();
		double k = 127. / fftSize2;
		double binSize = 44100. / fftSize;

        while(!audioReader.getSawOutputEOS()){
            double[] chunk = bufferManager.next();
            for(int i=0;i<fftSize;i++){
                fftHolder[i] = window[i] * chunk[i];
            }
            for(int i=fftSize;i<fftSize2;i++){
                fftHolder[i] = 0;
            }
            fft.realForward(fftHolder);

            byte[] values = new byte[fftSize];
			double maxValue = 0;
			int maxIndex = -1;
            for(int i=0;i<fftSize;i++){
                int i2 = i*2;
				double value = Math.sqrt(Math.pow(fftHolder[i2], 2) + Math.pow(fftHolder[i2+1], 2));
				value *= k;
				if(value > maxValue){
					maxValue = value;
					maxIndex = i;
				}
                if(value > 127) value = 127;

                values[i] = (byte) value;
            }
            data.add(values);
            Log.d(TAG, String.format("max value = %s max index %s freq %s", maxValue, maxIndex, maxIndex * binSize));
        }

        return this;
    }
}
