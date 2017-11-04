package com.lelloman.lousyaudiolibrary.demo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lelloman.lousyaudiolibrary.algorithm.Fft;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		int size = 12;
		double[] array = new double[size];
		Arrays.fill(array, 10);
		Log.d(MainActivity.class.getSimpleName(), Arrays.toString(array));

		Fft fft = new Fft(size);
		fft.realForward(array);
		Log.d(MainActivity.class.getSimpleName(), Arrays.toString(array));

		FragmentManager fragmentManager = getSupportFragmentManager();
		if(fragmentManager.findFragmentById(R.id.container) == null){
			fragmentManager.beginTransaction()
					.add(R.id.container, PlayerFragment.newInstance(R.raw.ticotico), PlayerFragment.class.getSimpleName())
					.commit();
		}

	}


}
