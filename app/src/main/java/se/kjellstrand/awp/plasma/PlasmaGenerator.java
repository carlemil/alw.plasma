package se.kjellstrand.awp.plasma;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;

/**
 * Created by Carl-Emil Kjellstrand on 11/24/14.
 */
public class PlasmaGenerator {

	private static final String TAG = PlasmaGenerator.class.getCanonicalName();
	private Bitmap bitmap;
	private Allocation allocationColorized;
	private Allocation xWaveAllocation;
	private Allocation yWaveAllocation;

	public int width;
	public int height;

	private int[] bitmapValues;

	private float[] xwave;
	private float[] ywave;

	private ScriptC_colorize coloriseScript;

	private RenderScript rs;

	private float speed = 0.7f;
	private float scale = 1.0f;

	public PlasmaGenerator(Context context, int width, int height) {
		this.width = width;
		this.height = height;

		rs = RenderScript.create(context, RenderScript.ContextType.DEBUG);
		rs.setPriority(RenderScript.Priority.LOW);

		setupAllocations(width, height);

		setupColorizeScript();

		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmapValues = new int[width * height];
		xwave = new float[width];
		ywave = new float[height];
		
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		String themesKey = context.getResources().getString(R.string.pref_theme_key);
        String themeName = sharedPreferences.getString(themesKey, context.getResources().getString(R.string.theme_black_n_white));
        Theme theme = new Theme(themeName, context);

        String speedKey = context.getResources().getString(R.string.pref_speed_key);
        speed = sharedPreferences.getInt(speedKey, 10);

        String scaleKey = context.getResources().getString(R.string.pref_scale_key);
        scale = sharedPreferences.getInt(scaleKey, 10);
        
        String brightnessKey = context.getResources().getString(R.string.pref_brightness_key);
        int brightness = sharedPreferences.getInt(brightnessKey, 100);

		setupPalette(context, theme, brightness);

		Element type = Element.F32(rs);
		xWaveAllocation = Allocation.createSized(rs, type, width);
		yWaveAllocation = Allocation.createSized(rs, type, height);
		coloriseScript.bind_xwave(xWaveAllocation);
		coloriseScript.bind_ywave(yWaveAllocation);

		coloriseScript.set_width(width);
	}

	public Bitmap getBitmapForFrame(int frame) {
		long t = System.currentTimeMillis();
		renderWaves(frame);
		Log.d(TAG, "renderWaves: " + (System.currentTimeMillis() - t));

		t = System.currentTimeMillis();
		renderColors();
		rs.finish();
		Log.d(TAG, "renderColors: " + (System.currentTimeMillis() - t));

		t = System.currentTimeMillis();
		copyToBitmap();
		rs.finish();
		Log.d(TAG, "renderToBitmap: " + (System.currentTimeMillis() - t));
		return bitmap;
	}

	private void renderWaves(int frame) {
		float w1 = 0.4f;
		float w2 = 0.3f;
		float w3 = 0.3f;

		for (int x = 0; x < xwave.length; x++) {
			xwave[x] = getSeed(frame, x, 12730f, 301f, w1) + //
					getSeed(frame, x, 9077f, 167f, w2) + //
					getSeed(frame, x, 3057f, 77f, w3);
		}
		for (int y = 0; y < ywave.length; y++) {
			ywave[y] = getSeed(frame, y, 12109f, 310f, w1) + //
					getSeed(frame, y, 9371f, 158f, w2) + //
					getSeed(frame, y, 3255f, 75f, w3);
		}

		xWaveAllocation.copy1DRangeFrom(0, width, xwave);
		yWaveAllocation.copy1DRangeFrom(0, height, ywave);
	}

	private float getSeed(float frame, float n, float speedDiv, float scaleDiv,
			float weight) {
		float speedFreq = (float) (Math.sin(frame / speedDiv));
		float scaleFreq = (float) (Math.cos(frame / scaleDiv));
		// Div 2 for sin and another div 2 for x+y in renderscript.
		// TODO optimize and do * colorSize here instead of in script.
		return (float) (Math.sin(frame * speed / 10000f * speedFreq + n * scale
				/ 1000f * scaleFreq) + 1f)
				/ 2f / 2f * weight;
	}

	private void renderColors() {
		coloriseScript.forEach_root(allocationColorized, allocationColorized);
	}

	private void copyToBitmap() {
		allocationColorized.copyTo(bitmapValues);
		bitmap.setPixels(bitmapValues, 0, width, 0, 0, width, height);
	}

	private void setupAllocations(int width, int height) {
		allocationColorized = Allocation.createSized(rs, Element.I32(rs), width
				* height);
	}

	private void setupColorizeScript() {
		coloriseScript = new ScriptC_colorize(rs);
	}

	private void setupPalette(Context context, Theme theme, int brightness) {
		int paletteSize = theme.getPaletteSize();
		
		int[] d = Palette.getPalette(context, theme, brightness);

		Element type = Element.I32(rs);
		Allocation colorAllocation = Allocation.createSized(rs, type,
				paletteSize );
		coloriseScript.bind_color(colorAllocation);
		coloriseScript.set_colorSize(paletteSize);

		colorAllocation.copy1DRangeFrom(0, paletteSize, d);
	}
}
