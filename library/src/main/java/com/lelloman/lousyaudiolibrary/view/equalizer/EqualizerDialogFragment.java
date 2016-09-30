package com.lelloman.lousyaudiolibrary.view.equalizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.lelloman.lousyaudiolibrary.R;


public abstract class EqualizerDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{

	public interface OnEqualizerSetListener {
		void onEqualizerSet(float[] bands);
	}

	public static final String ARG_BANDS = "argNBands";

	private OnEqualizerSetListener listener;
	private EqualizerView equalizerView;

	public EqualizerDialogFragment(float[] bands){
		Bundle args = new Bundle();
		args.putFloatArray(ARG_BANDS, bands);
		setArguments(args);
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
		equalizerView.setValues(bands);

		return new AlertDialog.Builder(context)
				.setTitle(getTitle())
				.setView(equalizerView)
				.setPositiveButton(getOkString(), this)
				.setNeutralButton(getResetString(), this)
				.create();
	}

	protected ViewGroup.LayoutParams getEqualizerViewLayoutParams(){

		int width = ViewGroup.LayoutParams.MATCH_PARENT;
		int height = (int) getResources().getDimension(R.dimen.dialog_equilizer_view_height);
		return new ViewGroup.LayoutParams(width, height);
	}

	protected String getOkString(){
		return getContext().getString(R.string.equilizer_dialog_ok);
	}
	protected String getResetString(){
		return getContext().getString(R.string.equilizer_dialog_reset);
	}
	protected String getTitle(){
		return getContext().getString(R.string.equilizer_dialog_title);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		OnEqualizerSetListener listener = this.listener;
		if(listener == null){
			Fragment fragment = getTargetFragment();
			if(fragment != null && fragment instanceof OnEqualizerSetListener){
				listener = (OnEqualizerSetListener) fragment;
			}
		}

		if(listener != null && equalizerView != null){
			listener.onEqualizerSet(equalizerView.getValues());
		}
	}
}
