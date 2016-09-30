package com.lelloman.lousyaudiolibrary.player;


import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.reader.IAudioReader;

import java.util.Arrays;

public class LousyAudioPlayer extends SlowAudioPlayer {

	private Equalizer equalizer;

	public LousyAudioPlayer(EventsListener listener, int frameSize, int hop, float scale) {
		super(listener, frameSize, hop, scale);
	}

	public LousyAudioPlayer(EventsListener listener, int frameSize, int hop) {
		super(listener, frameSize, hop);
	}

	public LousyAudioPlayer(EventsListener listener) {
		super(listener);
	}

	@Override
	public boolean init(IAudioReader reader) {
		if(!super.init(reader)) return false;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				equalizer = new Equalizer(0, getAudioSessionId());

				short[] range = equalizer.getBandLevelRange();
				short nBands = equalizer.getNumberOfBands();
				short r = (short) (range[1] - range[0]);
				short mid = (short) (r / 2);

				for(short j=0;j<nBands;j++){
					try {
						equalizer.setBandLevel(j, mid);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				Log.d("PlayerFragment", "onCreate: "+ Arrays.toString(range));
				equalizer.setEnabled(true);
			}
		}, 2000);

		return true;
	}

	public void setEqualizerBands(float[] bands){
		if(equalizer == null) return;

		float min = equalizer.getBandLevelRange()[0];
		float max = equalizer.getBandLevelRange()[1];
		float span = max - min;

		for (short i = 0; i < bands.length; i++) {
			short v = (short) ((bands[i] * span) + min);
			equalizer.setBandLevel(i, v);
		}

	}

	public float[] getEqualizerBands(){
		if(equalizer == null) return null;

		float[] bands = new float[equalizer.getNumberOfBands()];
		float min = equalizer.getBandLevelRange()[0];
		float max = equalizer.getBandLevelRange()[1];
		float span = max - min;

		for (short i = 0; i <bands.length; i++) {
			bands[i] = (equalizer.getBandLevel(i) - min) / span;
		}

		return bands;
	}
}
