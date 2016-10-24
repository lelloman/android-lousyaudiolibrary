package com.lelloman.lousyaudiolibrary;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

	public static class ColorSet {

		public final int primary, primaryDark, accent, windowBackground;

		public ColorSet(Context context) {
			TypedValue typedValue = new TypedValue();

			TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{
					R.attr.colorPrimary,
					R.attr.colorPrimaryDark,
					R.attr.colorAccent,
					android.R.attr.windowBackground
			});

			primary = a.getColor(0, Color.BLACK);
			primaryDark = a.getColor(1, Color.BLACK);
			accent = a.getColor(2, Color.BLACK);
			windowBackground = a.getColor(3, Color.BLACK);
		}
	}

	private Util() {
	}

	public static double[] hanning(int N){
		double[] win = new double[N];
		for (int i = 0; i < N; i++) {
			double j = (2 * Math.PI * i) / (N - 1);
			double k = 1 - Math.cos(j);
			win[i] = .5 * k;
		}
		return win;
	}

	public static final String md5(final String s) {
		final String MD5 = "MD5";
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "";
	}
}
