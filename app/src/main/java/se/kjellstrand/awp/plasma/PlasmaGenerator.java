package se.kjellstrand.awp.plasma;

import android.content.Context;
import android.graphics.Bitmap;
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

	private int paletteSize = 512;

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

		int[] colors = new int[] { 0xff000000, 0xffff0000, 0xffffff00,
				0xffffffff };
		setupPalette(context, colors);

		Element type = Element.F32(rs);
		xWaveAllocation = Allocation.createSized(rs, type, width);
		yWaveAllocation = Allocation.createSized(rs, type, height);
		coloriseScript.bind_xwave(xWaveAllocation);
		coloriseScript.bind_ywave(yWaveAllocation);
		
		coloriseScript.set_colorSize(paletteSize);
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
		for (int x = 0; x < xwave.length; x++) {
			xwave[x] = getSeed(frame+x, 73f, width) * 0.5f;
			//Log.d("tag", "x: "+xwave[x]);
		}
		for (int y = 0; y < ywave.length; y++) {
			ywave[y] = getSeed(frame+y, 44f, height) * 0.5f;
			//Log.d("tag", "y: "+ywave[y]);
		}

		xWaveAllocation.copy1DRangeFrom(0, width, xwave);
		yWaveAllocation.copy1DRangeFrom(0, height, ywave);
	}

	private float getSeed(int seed, float frequency, int width) {
		return (float) (Math.sin(seed / frequency) + 1) / 2f;
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

	private void setupPalette(Context context, int[] colors) {
		int[] d = Palette.getPalette(context, colors, paletteSize);

		Element type = Element.I32(rs);
		Allocation colorAllocation = Allocation.createSized(rs, type,
				paletteSize);
		coloriseScript.bind_color(colorAllocation);

		colorAllocation.copy1DRangeFrom(0, paletteSize, d);
	}
}
