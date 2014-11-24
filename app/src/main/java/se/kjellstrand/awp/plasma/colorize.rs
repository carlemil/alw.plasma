
#pragma version(1)
#pragma rs java_package_name(se.kjellstrand.awp.plasma)
#pragma rs_fp_relaxed 

int *color;
int colorSize;
int width;
float *xwave;
float *ywave;

void root(const int *in, int *out, uint32_t x) {
	int x_ = x % width;
	int y_ = x / width;
	//rsDebug("==MyApplication== x y size", x_, y_, colorSize);
	int c = (xwave[x_] + ywave[y_]) * colorSize;
	//rsDebug("==MyApplication== waves", (int)(xwave[x_]*1000), (int)(ywave[y_]*1000));
	
	//rsDebug("==MyApplication== color ", c);
    *out = color[c];
}