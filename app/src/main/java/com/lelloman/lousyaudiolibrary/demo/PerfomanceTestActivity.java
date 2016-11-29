package com.lelloman.lousyaudiolibrary.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.JavaPhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoder;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderMultiThread;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.NativePhaseVocoderOld;
import com.lelloman.lousyaudiolibrary.algorithm.phasevocoder.PhaseVocoderTester;

public class PerfomanceTestActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Thread(){
			@Override
			public synchronized void start() {
				super.start();
				int N = 4096 * 2;
				int H = N / 16;
				double tscale = .5;
				int seconds = 5;

				//setPriority(Thread.MAX_PRIORITY);

				log("test start!");
				long duration = PhaseVocoderTester.testVocoderPerformanceMs(JavaPhaseVocoder.class, tscale, N, H, seconds);
				log("elapsed       java  vocoder = %s", duration);

				duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoderOld.class, tscale, N, H, seconds);
				log("elapsed   native vocoderOld = %s", duration);

				duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoder.class, tscale, N, H, seconds);
				log("elapsed      native vocoder = %s", duration);

				duration = PhaseVocoderTester.testVocoderPerformanceMs(NativePhaseVocoderMultiThread.class, tscale, N, H, seconds);
				log("elapsed multithread vocoder = %s", duration);
			}
		}.start();
	}

	private void log(String msg, Object...args){
		Log.d(PerfomanceTestActivity.class.getSimpleName(), String.format(msg, args));
	}
}
