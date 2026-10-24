package com.gathin.tvhome;
/** Geometry shared by Home, its dock preview and the remote-friendly sliders. */
final class DockLayout {
    static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    static int height(int width, int thickness) { return Math.max(1, Math.round(width * 101f / 180f * clamp(thickness, 40, 140) / 100f)); }
    static int position(int bottom, int top, int percent) {
        int end = Math.min(bottom, Math.max(0, top));
        return Math.round(bottom + (end - bottom) * clamp(percent, 0, 100) / 100f);
    }
    static int blurRadius(int percent) { return Math.round(clamp(percent, 0, 100) * 0.18f); }
}
