package com.lelloman.lousyaudiolibrary.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.lelloman.lousyaudiolibrary.player.AudioPlayer;
import com.lelloman.lousyaudiolibrary.player.SlowAudioPlayer;


public class PlayerFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	public static final String ARG_SOURCE_RES_ID = "ARG_SOURCE_RES_ID";

	private SlowAudioPlayer player = null;

	private Button btnPause;
	private Button btnPlay;
	private ToggleButton btnSlow;
	private SeekBar seekBarTime;
	private SeekBar seekBarSpeed;

	public static PlayerFragment newInstance(int resId) {
		PlayerFragment frag = new PlayerFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SOURCE_RES_ID, resId);
		frag.setArguments(args);

		return frag;
	}

	AudioPlayer.EventsListener playerListener = new AudioPlayer.EventsListener() {
		@Override
		public void onStart(AudioPlayer player) {

		}

		@Override
		public void onPlaybackUpdate(AudioPlayer player, double percent, long timeMs) {
			if (seekBarTime != null)
				seekBarTime.setProgress((int) (percent * 10000));

		}

		@Override
		public void onRelease(AudioPlayer player) {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle args = getArguments();

		int resId = -1;
		if (args != null)
			resId = args.getInt(ARG_SOURCE_RES_ID, resId);

		player = new SlowAudioPlayer(playerListener);

		boolean init = player.init(getActivity(), resId);

		if (init) {
			player.start();
		} else {
			// TODO mboh
		}


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_audio_player, container, false);

		seekBarTime = (SeekBar) rootView.findViewById(R.id.seekbarTime);
		btnSlow = (ToggleButton) rootView.findViewById(R.id.btnSlow);
		btnPlay = (Button) rootView.findViewById(R.id.btnPlay);
		btnPause = (Button) rootView.findViewById(R.id.btnPause);
		seekBarSpeed = (SeekBar) rootView.findViewById(R.id.seekbarSpeed);

		btnPlay.setOnClickListener(this);
		btnPause.setOnClickListener(this);
		seekBarTime.setOnSeekBarChangeListener(this);
		btnSlow.setOnClickListener(this);
		seekBarSpeed.setOnSeekBarChangeListener(this);

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		seekBarTime = null;
		btnSlow = null;
		btnPlay = null;
		btnPause = null;
		seekBarSpeed = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		Activity act = getActivity();
		if (act != null)
			if (!act.isChangingConfigurations())
				if (player != null) player.pause();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
			case R.id.btnPlay:
				player.play();
				break;
			case R.id.btnPause:
				player.pause();
				break;
			case R.id.btnSlow:
				player.setSlowScale(btnSlow.isChecked() ? .4 : 1);
				seekBarSpeed.setProgress(btnSlow.isChecked() ? 0 : seekBarSpeed.getMax());
				break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar == seekBarTime) {
			if (fromUser)
				player.seek(progress / 10000.);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if( seekBar == seekBarSpeed){

			player.setSlowScale(seekBar.getProgress() / 10000. * .6 + .4);
			btnSlow.setChecked(player.isSlow());
		}
	}

}
