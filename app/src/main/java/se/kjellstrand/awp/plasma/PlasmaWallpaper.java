package se.kjellstrand.awp.plasma;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by Carl-Emil Kjellstrand on 10/31/14.
 */
public class PlasmaWallpaper extends WallpaperService {

	private static final String TAG = PlasmaWallpaper.class.getCanonicalName();

	private static final int FPS = 60;

	public static final float SCALE = 2.0f;

	@Override
	public Engine onCreateEngine() {

		return new PlasmaWPEngine();
	}

	class PlasmaWPEngine extends Engine {
		private PlasmaGenerator plasmaGenerator = null;
		private Handler handler = new Handler();
		private boolean visible;
		private int frame = (int) System.currentTimeMillis() / 1000;
		private int width;
		private int height;

		private Runnable iteration = new Runnable() {
			public void run() {
				iteration();
				drawFrame();
			}
		};

		@Override
		public void onDestroy() {
			super.onDestroy();
			// stop the animation
			handler.removeCallbacks(iteration);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;

			if (visible) {
				plasmaGenerator = new PlasmaGenerator(getApplicationContext(),
						(int) (width / SCALE), (int) (height / SCALE));
				iteration();
				drawFrame();
			} else {
				// stop the animation
				handler.removeCallbacks(iteration);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			visible = false;
			// stop the animation
			handler.removeCallbacks(iteration);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			iteration();
			drawFrame();
		}

		protected void drawFrame() {
			SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					draw(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}

		private void draw(Canvas c) {
			Bitmap bitmap = plasmaGenerator.getBitmapForFrame(frame++);
			Paint paint = new Paint();
			paint.setColor(0xffffffff);

			Matrix matrix = new Matrix();
			matrix.reset();
			matrix.setScale(SCALE, SCALE);
			c.drawBitmap(bitmap, matrix, paint);
		}

		protected void iteration() {
			handler.removeCallbacks(iteration);
			if (visible) {
				handler.postDelayed(iteration, 1000 / FPS);
			}
		}
	}
}
