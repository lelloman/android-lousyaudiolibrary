package com.lelloman.lousyaudiolibrary.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.lelloman.lousyaudiolibrary.player.AudioPlayer;
import com.lelloman.lousyaudiolibrary.player.LousyAudioPlayer;
import com.lelloman.lousyaudiolibrary.player.SlowAudioPlayer;
import com.lelloman.lousyaudiolibrary.reader.AudioReader;
import com.lelloman.lousyaudiolibrary.reader.NativeAudioReader;
import com.lelloman.lousyaudiolibrary.reader.volume.IVolumeReader;
import com.lelloman.lousyaudiolibrary.reader.volume.NativeVolumeReader;
import com.lelloman.lousyaudiolibrary.view.CompoundVolumeView;
import com.lelloman.lousyaudiolibrary.view.VolumeView;
import com.lelloman.lousyaudiolibrary.view.equalizer.EqualizerDialogFragment;


public class PlayerFragment extends Fragment implements
		View.OnClickListener,
		SeekBar.OnSeekBarChangeListener,
		CompoundVolumeView.CompoundVolumeViewListener{

	public static final String ARG_SOURCE_RES_ID = "ARG_SOURCE_RES_ID";

	private LousyAudioPlayer player = null;

	private Button btnPause;
	private Button btnPlay;
	private ImageButton btnEqualizer;
	private SeekBar seekBarSpeed;
	private CompoundVolumeView volumeView;
	private IVolumeReader volumeReader;

	private boolean hasSubWindow;
	private float subWindowStart;
	private float subWindowEnd;

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

		player = new LousyAudioPlayer(playerListener);

		try {
			AudioReader audioReader = new AudioReader(getActivity(), resId);
			//IAudioReader audioReader = new DummyAudioReader(44100 * 10, 44100, 440,0,4096);
			if (player.init(audioReader)) {
				player.start();

				int width = getResources().getDisplayMetrics().widthPixels;
				int height = getResources().getDisplayMetrics().heightPixels;

				int tot = width / VolumeView.K;

				int[] intervals = new int[]{
						tot,
						tot*2,
						tot*4,
						tot*6
				};
				volumeReader = new NativeVolumeReader(new NativeAudioReader(getActivity(), resId), intervals);
				//volumeReader = new VolumeReader(new DummyAudioReader(44100 * 10, 44100, 440,0,4096), intervals);
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

		btnPlay = (Button) rootView.findViewById(R.id.btnPlay);
		btnPause = (Button) rootView.findViewById(R.id.btnPause);
		btnEqualizer = (ImageButton) rootView.findViewById(R.id.btnEqualizer);
		seekBarSpeed = (SeekBar) rootView.findViewById(R.id.seekbarSpeed);
		volumeView = (CompoundVolumeView) rootView.findViewById(R.id.volumeView);

		if (volumeReader != null) {
			volumeView.setVolumeReader(volumeReader);
		}

		if(hasSubWindow){
			volumeView.setWindow(subWindowStart, subWindowEnd);
		}

		volumeView.setCursor((float) player.getCurrentPercent());

		btnPlay.setOnClickListener(this);
		btnPause.setOnClickListener(this);
		seekBarSpeed.setOnSeekBarChangeListener(this);
		volumeView.setListener(this);
		btnEqualizer.setOnClickListener(this);

		return rootView;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		btnEqualizer = null;
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
			case R.id.btnEqualizer:
				EqualizerDialogFragment fragment = new EqualizerDialogFragment(new float[]{.5f,.5f,.5f,.5f,.5f,.5f});
				fragment.setTargetFragment(this, 123);
				fragment.show(getFragmentManager(),EqualizerDialogFragment.class.getSimpleName());
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

		player.setSlowScale(seekBar.getProgress() / 10000. * .8 + .2);
	}

	@Override
	public void onMoveCursor(CompoundVolumeView compoundVolumeView, float percentX) {
		player.seek(percentX);
		if (volumeView != null) {
			volumeView.setCursor(percentX);
		}
	}

	@Override
	public void onWindowSelected(CompoundVolumeView compoundVolumeView, float start, float end) {
		hasSubWindow = true;
		subWindowStart = start;
		subWindowEnd = end;

		if(volumeView != null){
			volumeView.setWindow(start, end);
		}
	}

	@Override
	public void onWindowUnselected(CompoundVolumeView compoundVolumeView) {
		hasSubWindow = false;
		if(volumeView != null){
			volumeView.unSetWindow();
		}
	}
}
