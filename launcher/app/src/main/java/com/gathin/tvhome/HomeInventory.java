package com.gathin.tvhome;
import android.content.*;
import android.content.pm.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
final class HomeInventory {
    static void write(Context c) {
        try {
            PackageManager pm = c.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS);
            apps.sort(Comparator.comparing(a -> a.packageName));
            JSONArray rows = new JSONArray();
            for (ApplicationInfo a : apps) {
                JSONObject row = new JSONObject();
                row.put("package", a.packageName); row.put("name", pm.getApplicationLabel(a).toString());
                row.put("enabled", a.enabled); row.put("system", (a.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                row.put("tv_app", pm.getLeanbackLaunchIntentForPackage(a.packageName) != null);
                row.put("launchable", pm.getLaunchIntentForPackage(a.packageName) != null);
                rows.put(row);
            }
            File dir = c.getExternalFilesDir(null);
            if (dir != null) HomePreferences.copy(new ByteArrayInputStream(rows.toString(2).getBytes(StandardCharsets.UTF_8)), new File(dir, "app-inventory.json"));
        } catch (Exception e) { android.util.Log.e("Home.Inventory", "Export failed", e); }
    }
}
