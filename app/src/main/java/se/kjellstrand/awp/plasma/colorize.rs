
#pragma version(1)
#pragma rs java_package_name(se.kjellstrand.awp.plasma)
#pragma rs_fp_relaxed 

int *color;

void root(const int *in, int *out, uint32_t x, uint32_t y) {
    *out = color[*in];
}