package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.*;
import java.util.*;

final class HomePreferences {
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE); }
    static int dockThickness(Context c) {
        return DockLayout.clamp(prefs(c).getInt("dock_thickness", prefs(c).getBoolean("compact_dock", true) ? 77 : 100), 40, 140);
    }
    static void changed(Context c) {
        SharedPreferences p = prefs(c);
        p.edit().putLong(Config.SETTINGS_REVISION_KEY, p.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }
    static List<String> dock(Context c, List<String> fallback) {
        String value = prefs(c).getString("custom_dock", null);
        if (value == null) return new ArrayList<>(fallback);
        List<String> result = DockSelection.parse(value);
        return result.isEmpty() ? new ArrayList<>(fallback) : result;
    }
    static List<String> currentDock(Context c, Config config) {
        Order order = Order.load(Order.file(c));
        if (order == null || !order.dock.equals(Order.dockKey(config.dockApps))) return new ArrayList<>(config.dockApps);
        List<String> result = new ArrayList<>();
        int count = Math.min(config.columns, order.docked >= 0 ? order.docked : config.columns);
        for (String key : order.keys.subList(0, Math.min(count, order.keys.size()))) {
            int slash = key.indexOf('/');
            if (slash > 0) {
                String pkg = key.substring(0, slash);
                if (!pkg.equals(c.getPackageName()) && !result.contains(pkg)) result.add(pkg);
            }
        }
        return result.isEmpty() ? new ArrayList<>(config.dockApps) : result;
    }
    static void saveDock(Context c, List<String> packages) {
        List<String> valid = DockSelection.parse(String.join(",", packages));
        if (valid.isEmpty()) return;
        prefs(c).edit().putString("custom_dock", String.join(",", valid))
                .putInt("columns", Math.max(4, valid.size())).apply();
        Set<String> hidden = new HashSet<>(Config.load(c).hiddenApps);
        Set<String> removed = LibraryApps.loadRemoved(c);
        for (String pkg : valid) if (hidden.contains(pkg) || removed.contains(pkg)) LibraryApps.save(c, pkg, true);
        changed(c);
    }
    static void seedWallpapers(Context c) {
        File dir = Config.dir(c);
        if (dir == null) return;
        try {
            File images = new File(dir, "wallpapers");
            if (!images.isDirectory() && !images.mkdirs()) return;
            for (String name : c.getAssets().list("wallpapers")) {
                File dest = new File(images, name);
                if (!dest.exists()) copy(c.getAssets().open("wallpapers/" + name), dest);
            }
            File initial = new File(dir, Config.WALLPAPER_NAME);
            if (!initial.exists()) copy(c.getAssets().open("wallpapers/Mountain valley.jpg"), initial);
        } catch (IOException e) { android.util.Log.w("Home.Wallpaper", "Could not seed landscapes", e); }
    }
    static void copy(InputStream input, File output) throws IOException {
        android.util.AtomicFile atomic = new android.util.AtomicFile(output);
        FileOutputStream stream = null;
        try (InputStream in = input) {
            stream = atomic.startWrite();
            byte[] data = new byte[16384]; int n;
            while ((n = in.read(data)) != -1) stream.write(data, 0, n);
            atomic.finishWrite(stream);
        } catch (IOException e) { if (stream != null) atomic.failWrite(stream); throw e; }
    }
}
