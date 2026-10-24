package com.gathin.tvhome;
/** Two separable box passes over a small cached wallpaper sample, never over animation frames. */
final class PixelBlur {
    static void apply(int[] pixels, int width, int height, int radius) {
        if (width <= 0 || height <= 0 || pixels.length != width * height) throw new IllegalArgumentException("Invalid image dimensions");
        radius = Math.min(24, Math.max(0, radius));
        if (radius == 0) return;
        int[] temp = new int[pixels.length];
        for (int pass = 0; pass < 2; pass++) {
            line(pixels, temp, width, height, radius, false);
            line(temp, pixels, height, width, radius, true);
        }
    }
    private static void line(int[] src, int[] dst, int length, int lines, int r, boolean vertical) {
        int stride = vertical ? lines : 1, baseStep = vertical ? 1 : length, size = r * 2 + 1;
        for (int row = 0; row < lines; row++) {
            int base = row * baseStep, red = 0, green = 0, blue = 0;
            for (int i = -r; i <= r; i++) {
                int c = src[base + DockLayout.clamp(i, 0, length - 1) * stride];
                red += (c >> 16) & 255; green += (c >> 8) & 255; blue += c & 255;
            }
            for (int x = 0; x < length; x++) {
                dst[base + x * stride] = 0xFF000000 | ((red / size) << 16) | ((green / size) << 8) | blue / size;
                int a = src[base + DockLayout.clamp(x - r, 0, length - 1) * stride];
                int b = src[base + DockLayout.clamp(x + r + 1, 0, length - 1) * stride];
                red += ((b >> 16) & 255) - ((a >> 16) & 255);
                green += ((b >> 8) & 255) - ((a >> 8) & 255); blue += (b & 255) - (a & 255);
            }
        }
    }
}
