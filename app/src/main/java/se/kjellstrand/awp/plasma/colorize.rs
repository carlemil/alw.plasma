
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
	int c = (xwave[x_] + ywave[y_]) * colorSize;
    *out = color[c];
}