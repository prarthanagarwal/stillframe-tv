package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** A small layout preview; the real dock material is prepared only for Home's wallpaper. */
final class DockPreview extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF rect = new RectF();
    private final Bitmap picture;
    private final int count;
    DockPreview(Context c) {
        super(c); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        Bitmap decoded = null;
        try {
            Config config = Config.load(c);
            java.io.File file = config.wallpaper;
            if (file == null && !config.wallpapers.isEmpty()) file = config.wallpapers.get(0);
            if (file != null) { BitmapFactory.Options o = new BitmapFactory.Options(); o.inSampleSize = 8; decoded = BitmapFactory.decodeFile(file.getPath(), o); }
        } catch (Exception | OutOfMemoryError ignored) { }
        picture = decoded;
        count = Math.min(7, Math.max(4, Config.load(c).dockApps.size()));
    }
    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        paint.setColor(0xFF203943); c.drawRect(0,0,w,h,paint);
        if (picture != null) {
            float scale = Math.max(w / picture.getWidth(), h / picture.getHeight());
            float pw = picture.getWidth()*scale, ph = picture.getHeight()*scale;
            paint.setAlpha(170); c.drawBitmap(picture,null,new RectF((w-pw)/2,(h-ph)/2,(w+pw)/2,(h+ph)/2),paint); paint.setAlpha(255);
        }
        paint.setColor(0xFFF1F5F7); paint.setTextSize(h*.13f); c.drawText("10:24",w*.055f,h*.22f,paint);
        paint.setColor(0xBDE7EFF2); paint.setTextSize(h*.055f); c.drawText("Your Home",w*.055f,h*.31f,paint);
        android.content.SharedPreferences p=HomePreferences.prefs(getContext());
        float bh=h*.16f*HomePreferences.dockThickness(getContext())/100f;
        float top=DockLayout.position(Math.round(h-bh-h*.08f),Math.round(h*.40f),p.getInt("dock_position",0));
        rect.set(w*.045f,top,w*.955f,top+bh);
        int opacity=DockLayout.clamp(p.getInt(Config.DOCK_OPACITY_KEY,55),0,100);
        paint.setColor((Math.round(30+opacity*1.5f)<<24)|0xD4E1E6); c.drawRoundRect(rect,bh*.20f,bh*.20f,paint);
        float gap=rect.width()*.025f, cw=(rect.width()-gap*(count+1))/count;
        paint.setColor(0xEAE8F0F3);
        for(int i=0;i<count;i++){float x=rect.left+gap+i*(cw+gap);c.drawRoundRect(x,top+bh*.21f,x+cw,top+bh*.79f,bh*.10f,bh*.10f,paint);}
    }
}
