package se.kjellstrand.awp.plasma;

import android.content.Context;
import android.graphics.Color;

public class Palette {

	public static int[] getPalette(Context context, int[] colors, int precission) {

		int[] palette = new int[precission];

		setGradient(context, palette, colors);

		return palette;
	}

	private static void setGradient(Context context, int[] palette, int[] colors) {
		for (int i = 1; i < colors.length; i++) {
			int pl = palette.length;
			int cl = colors.length - 1;
			int i1 = Math.round((pl / (float) (cl)) * (i - 1));
			int i2 = Math.round((pl / (float) (cl)) * i);
			int c1 = colors[i - 1];
			int c2 = colors[i];
			int[] p = new int[i2 - i1];
			setGradient(context, p, c1, c2);
			System.arraycopy(p, 0, palette, i1, p.length);
		}
	}

	private static void setGradient(Context context, int[] palette,
			int startColor, int endColor) {
		float[] start = new float[3];
		float[] end = new float[3];
		float[] tmp = new float[3];

		Color.colorToHSV(startColor, start);
		Color.colorToHSV(endColor, end);

		for (int i = 0; i < palette.length; i++) {
			float p = ((float) i) / palette.length;
			for (int j = 0; j < 3; j++) {
				tmp[j] = start[j] * (1f - p) + end[j] * p;
			}
			palette[i] = Color.HSVToColor(tmp);

		}
	}
}
