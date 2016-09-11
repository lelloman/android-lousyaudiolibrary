package com.lelloman.lousyaudiolibrary.demo;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fragmentManager = getSupportFragmentManager();
		if(fragmentManager.findFragmentById(R.id.container) == null){
			fragmentManager.beginTransaction()
					.add(R.id.container, PlayerFragment.newInstance(R.raw.paintmono2m), PlayerFragment.class.getSimpleName())
					.commit();
		}

	}


}
