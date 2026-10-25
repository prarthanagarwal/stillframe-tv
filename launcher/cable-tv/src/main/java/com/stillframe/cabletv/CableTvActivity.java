package com.stillframe.cabletv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.widget.Toast;

/** A direct HDMI 2 shortcut for the configured BPL/Changhong AI PONT hall TV. */
public final class CableTvActivity extends Activity {
    private static final String HDMI_2 = "com.mediatek.tvinput/.hdmi.HDMIInputService/HW5";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Intent cable = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(TvContract.buildChannelUriForPassthroughInput(HDMI_2),
                        "vnd.android.cursor.item/channel")
                .setPackage("com.mediatek.wwtv.tvcenter")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(cable);
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            Toast.makeText(this, R.string.input_unavailable, Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }
}
