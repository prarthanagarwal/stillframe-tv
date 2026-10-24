package com.gathin.tvhome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/** Returns TVs that boot into a signal input to Stillframe after their input services settle. */
public final class HomeBootReceiver extends BroadcastReceiver {
    private static final String ACTION_OPEN_HOME = "com.gathin.tvhome.action.OPEN_HOME_AFTER_BOOT";
    private static final int BOOT_HOME_REQUEST = 8401;
    private static final long STARTUP_SETTLE_MS = 12_000L;

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !HomePreferences.prefs(context).getBoolean("boot_home_after_start", true)) return;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            scheduleHome(context);
        } else if (ACTION_OPEN_HOME.equals(intent.getAction())) {
            Intent home = new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(home);
        }
    }

    private static void scheduleHome(Context context) {
        Intent launch = new Intent(context, HomeBootReceiver.class).setAction(ACTION_OPEN_HOME);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getBroadcast(context, BOOT_HOME_REQUEST, launch, flags);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + STARTUP_SETTLE_MS, pending);
        }
    }
}
