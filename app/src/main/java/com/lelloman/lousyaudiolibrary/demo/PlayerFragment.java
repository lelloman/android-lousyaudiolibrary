package com.lelloman.lousyaudiolibrary.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lelloman.lousyaudiolibrary.player.AudioPlayer;
import com.lelloman.lousyaudiolibrary.player.SlowAudioPlayer;
import com.lelloman.lousyaudiolibrary.reader.AudioReader;
import com.lelloman.lousyaudiolibrary.reader.VolumeReader;
import com.lelloman.lousyaudiolibrary.view.CompoundVolumeView;
import com.lelloman.lousyaudiolibrary.view.VolumeView;


public class PlayerFragment extends Fragment implements
		View.OnClickListener,
		SeekBar.OnSeekBarChangeListener,
		VolumeView.VolumeViewListener{

	public static final String ARG_SOURCE_RES_ID = "ARG_SOURCE_RES_ID";

	private SlowAudioPlayer player = null;

	private Button btnPause;
	private Button btnPlay;
	private ToggleButton btnSlow;
	private SeekBar seekBarSpeed;
	private CompoundVolumeView volumeView;
	private VolumeReader volumeReader;

	private boolean hasSubWindow;
	private double subWindowStart;
	private double subWindowEnd;

	private int resId;

	public static PlayerFragment newInstance(int resId) {
		PlayerFragment frag = new PlayerFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SOURCE_RES_ID, resId);
		frag.setArguments(args);

		return frag;
	}

	SlowAudioPlayer.EventsListener playerListener = new SlowAudioPlayer.EventsListener() {
		@Override
		public void onStart(AudioPlayer player) {

		}

		@Override
		public void onPlaybackUpdate(AudioPlayer player, double percent, long timeMs) {

			if (volumeView != null)
				volumeView.setCursor((float) percent);

			if(hasSubWindow){
				if(percent > subWindowEnd){
					player.seek(subWindowStart);
				}
			}

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

		resId = -1;
		if (args != null)
			resId = args.getInt(ARG_SOURCE_RES_ID, resId);

		player = new SlowAudioPlayer(playerListener);

		try {
			AudioReader audioReader = new AudioReader(getActivity(), R.raw.paintmono2m);

			if (player.init(audioReader)) {
				player.start();
				int width = getResources().getDisplayMetrics().widthPixels;
				int height = getResources().getDisplayMetrics().heightPixels;

				int[] intervals = new int[]{
						(width / VolumeView.K),
						(height/ VolumeView.K)
				};
				volumeReader = new VolumeReader(new AudioReader(getActivity(), R.raw.paintmono2m), intervals);
			} else {
				throw new Exception("mboh");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_audio_player, container, false);

		btnSlow = (ToggleButton) rootView.findViewById(R.id.btnSlow);
		btnPlay = (Button) rootView.findViewById(R.id.btnPlay);
		btnPause = (Button) rootView.findViewById(R.id.btnPause);
		seekBarSpeed = (SeekBar) rootView.findViewById(R.id.seekbarSpeed);
		volumeView = (CompoundVolumeView) rootView.findViewById(R.id.volumeView);
		if (volumeReader != null) {
			volumeView.setVolumeReader(volumeReader);
		}

		btnPlay.setOnClickListener(this);
		btnPause.setOnClickListener(this);
		btnSlow.setOnClickListener(this);
		seekBarSpeed.setOnSeekBarChangeListener(this);
		volumeView.setListener(this);

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

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
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		player.setSlowScale(seekBar.getProgress() / 10000. * .6 + .4);
		btnSlow.setChecked(player.isSlow());
	}

	@Override
	public void onDoubleTap(VolumeView volumeView, float percentX) {
		player.seek(percentX);
		if (volumeView != null) {
			volumeView.setCursor(percentX);
		}
	}

	@Override
	public void onWindowSelected(VolumeView view, float start, float end) {
		Toast.makeText(getActivity(), String.format("%.2f - %.2f", start, end), Toast.LENGTH_SHORT).show();

		hasSubWindow = true;
		subWindowStart = start;
		subWindowEnd = end;

		if(volumeView != null){
			volumeView.setWindow(start, end);
		}
	}
}
