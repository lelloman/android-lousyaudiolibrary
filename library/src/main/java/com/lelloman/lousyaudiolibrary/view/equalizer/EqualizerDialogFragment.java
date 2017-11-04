package com.lelloman.lousyaudiolibrary.view.equalizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lelloman.lousyaudiolibrary.R;


public class EqualizerDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, EqualizerView.OnBandUpdatesListener {

	public interface OnEqualizerSetListener {
		void onEqualizerSet(float[] bands);
		void onEqualizerReset(float[] bands);
	}

	public static EqualizerDialogFragment newInstance(float[] bands){
		Bundle args = new Bundle();
		args.putFloatArray(ARG_BANDS, bands);

		EqualizerDialogFragment fragment = new EqualizerDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public static final String ARG_BANDS = "argNBands";

	private OnEqualizerSetListener listener;
	private EqualizerView equalizerView;

	public EqualizerDialogFragment(){

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if(context instanceof OnEqualizerSetListener){
			listener = (OnEqualizerSetListener) context;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onStart(){
		super.onStart();

		if (getDialog() == null)
			return;

		int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * .9f);
		int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_equilizer_view_height);

		getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

	}

	@Override
	public void onBandsUpdate(float[] bands) {
		if(getListener() != null) listener.onEqualizerSet(bands);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		float[] bands = null;
		Bundle args = getArguments();
		if(args != null){
			bands = args.getFloatArray(ARG_BANDS);
		}

		Context context = getActivity();
		equalizerView = new EqualizerView(context);
		equalizerView.setLayoutParams(getEqualizerViewLayoutParams());
		equalizerView.setBands(bands);
		equalizerView.setOnBandsUpdateListener(this);

		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.setTitle(getTitle())
				.setView(equalizerView)
				.setPositiveButton(getOkString(), this)
				.setNeutralButton(getResetString(), this)
				.create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {

						float[] bands = equalizerView.getBands();
						for(int i=0;i<bands.length;i++){
							bands[i] = .5f;
						}
						OnEqualizerSetListener listener = getListener();
						if(listener != null) listener.onEqualizerReset(bands);
						equalizerView.setBands(bands);
					}
				});
			}
		});

		return alertDialog;
	}

	protected ViewGroup.LayoutParams getEqualizerViewLayoutParams(){

		int width = ViewGroup.LayoutParams.MATCH_PARENT;
		int height = getResources().getDimensionPixelSize(R.dimen.dialog_equilizer_view_height);
		return new ViewGroup.LayoutParams(width, height);
	}

	protected String getOkString(){
		return getContext().getString(R.string.equalizer_dialog_ok);
	}
	protected String getResetString(){
		return getContext().getString(R.string.equalizer_dialog_reset);
	}
	protected String getTitle(){
		return getContext().getString(R.string.equalizer_dialog_title);
	}

	private OnEqualizerSetListener getListener(){
		if(listener != null) return listener;

		Fragment fragment = getTargetFragment();
		if(fragment != null && fragment instanceof OnEqualizerSetListener){
			listener = (OnEqualizerSetListener) fragment;
		}
		return listener;
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {

		OnEqualizerSetListener listener = getListener();

		if(listener != null && equalizerView != null){
			listener.onEqualizerSet(equalizerView.getBands());
		}
	}
}
