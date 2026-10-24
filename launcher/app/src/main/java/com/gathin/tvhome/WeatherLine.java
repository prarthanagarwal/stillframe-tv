package com.gathin.tvhome;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/** One cached header line. Network work runs only while Home is visible, at most once per half hour. */
final class WeatherLine {
    private final Activity owner;
    private final TextView label;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean active, loading;
    private final Runnable tick = () -> refresh();
    WeatherLine(Activity a, LinearLayout block) {
        owner = a; label = new TextView(a);
        label.setTextColor(0xEDF4F6F6); label.setTextSize(16);
        label.setPadding(0, Math.round(10 * a.getResources().getDisplayMetrics().density), 0, 0);
        label.setSingleLine(true); block.addView(label);
    }
    void start() { active = true; refresh(); }
    void stop() { active = false; handler.removeCallbacks(tick); }
    void close() { stop(); worker.shutdownNow(); }
    private void refresh() {
        if (!active) return;
        SharedPreferences p = HomePreferences.prefs(owner);
        label.setVisibility(p.getBoolean("weather_enabled", true) ? View.VISIBLE : View.GONE);
        if (label.getVisibility() != View.VISIBLE) return;
        String city = p.getString("weather_city", "Surat");
        double lat = Double.parseDouble(p.getString("weather_lat", "21.19594"));
        double lon = Double.parseDouble(p.getString("weather_lon", "72.83023"));
        String key = lat + "," + lon;
        long age = System.currentTimeMillis() - p.getLong("weather_time", 0);
        boolean cached = key.equals(p.getString("weather_key", "")) && age >= 0 && age < 6 * 3600000L;
        if (cached) {
            double t = Double.longBitsToDouble(p.getLong("weather_temperature", 0));
            boolean f = p.getBoolean("weather_fahrenheit", false);
            label.setText(city + "  ·  " + WeatherModel.degrees(t, f) + (f ? "°F" : "°C") + "  ·  "
                    + WeatherModel.condition(p.getInt("weather_code", -1)) + (age > 3600000L ? "  ·  Cached" : ""));
        } else label.setText(city + "  ·  " + (loading ? "Updating weather…" : "Weather unavailable"));
        handler.removeCallbacks(tick); handler.postDelayed(tick, 30 * 60000L);
        if (loading || (cached && age < 30 * 60000L)) return;
        loading = true;
        worker.execute(() -> {
            try {
                JSONObject j = json("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                        + "&current=temperature_2m,weather_code&forecast_days=1").getJSONObject("current");
                double temp = j.getDouble("temperature_2m");
                if (!WeatherModel.valid(temp)) throw new IOException("Invalid weather temperature");
                p.edit().putString("weather_key", key).putLong("weather_time", System.currentTimeMillis())
                        .putLong("weather_temperature", Double.doubleToLongBits(temp))
                        .putInt("weather_code", j.getInt("weather_code")).apply();
                handler.post(() -> { loading = false; if (active) refresh(); });
            } catch (Exception e) {
                handler.post(() -> { loading = false; if (active && !cached) label.setText(city + "  ·  Weather unavailable"); });
            }
        });
    }
    static JSONObject json(String address) throws Exception {
        HttpsURLConnection c = (HttpsURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(10000); c.setReadTimeout(10000);
        try {
            if (c.getResponseCode() != 200) throw new IOException("Weather service unavailable");
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[4096]; int n;
                while ((n = in.read(b)) != -1) {
                    if (out.size() + n > 262144) throw new IOException("Response too large");
                    out.write(b, 0, n);
                }
                return new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally { c.disconnect(); }
    }
}
