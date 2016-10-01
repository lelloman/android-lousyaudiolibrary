package com.lelloman.lousyaudiolibrary;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

public class Util {

	public static class ColorSet {

		public final int primary, primaryDark, accent, windowBackground;

		public ColorSet(Context context){
			TypedValue typedValue = new TypedValue();

			TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] {
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

	private Util(){}

}
