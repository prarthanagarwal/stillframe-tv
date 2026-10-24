package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

/** Personal launcher controls, fully usable with the TV remote and without a computer. */
public final class LauncherSettingsActivity extends Activity {
    static final String EXTRA_REORDER = "com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    private static final int IMPORT = 41;
    private SharedPreferences prefs;
    private LinearLayout body;
    private ScrollView scroll;
    private int rows;
    private int category;
    private DockPreview preview;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); prefs = HomePreferences.prefs(this); render();
    }
    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private void render() {
        int focus = getCurrentFocus() == null ? 2000 + category : getCurrentFocus().getId();
        int y = scroll == null ? 0 : scroll.getScrollY(); rows = 0; preview = null;
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(46), dp(24), dp(46), dp(24)); page.setBackgroundColor(0xFF111A21);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this); title.setText("Home Settings"); title.setTextSize(30); title.setTextColor(0xFFF0F5F7);
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button tv = new Button(this); tv.setId(R.id.personal_tv_settings); tv.setText("TV Settings"); tv.setTextSize(15); tv.setTextColor(0xFFF0F5F7);
        tv.setAllCaps(false); tv.setBackgroundResource(R.drawable.settings_choice_bg); tv.setPadding(dp(20),0,dp(20),0);
        tv.setOnClickListener(v -> open(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        header.addView(tv,new LinearLayout.LayoutParams(dp(150),dp(46))); page.addView(header);
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(-1,0,1);layoutParams.topMargin=dp(22);page.addView(layout,layoutParams);
        LinearLayout navigation = new LinearLayout(this);navigation.setOrientation(LinearLayout.VERTICAL);
        String[] tabs={"Dock", "Apps", "Background", "Weather", "Receivers"};
        for(int i=0;i<tabs.length;i++) {
            final int at=i; Button tab=new Button(this);tab.setId(2000+i);tab.setText(tabs[i]);tab.setTextSize(17);
            tab.setAllCaps(false);tab.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);tab.setPadding(dp(16),0,dp(16),0);
            tab.setTextColor(0xFFF0F5F7);tab.setBackgroundResource(R.drawable.settings_choice_bg);tab.setSelected(i==category);
            tab.setOnClickListener(v->{category=at;scroll=null;render();});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.bottomMargin=dp(8);navigation.addView(tab,lp);
        }
        TextView brand=new TextView(this);brand.setText("PERSONAL HOME\n3.1");brand.setTextSize(12);brand.setTextColor(0xFF91A9B7);brand.setPadding(dp(16),dp(20),0,0);navigation.addView(brand);
        layout.addView(navigation,new LinearLayout.LayoutParams(dp(166),-1));
        scroll = new ScrollView(this);scroll.setFillViewport(true);scroll.setBackground(panelBackground());
        LinearLayout.LayoutParams contentParams=new LinearLayout.LayoutParams(0,-1,1);contentParams.setMarginStart(dp(22));layout.addView(scroll,contentParams);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(18),dp(24),dp(24));scroll.addView(body);
        setContentView(page);
        if(category==0) dockSettings();
        else if(category==1) {
            section("Apps", "Choose what appears on Home.");
            row("Choose dock apps", dockSummary(), this::chooseDock);
            row("Arrange apps", "Choose an app, move it with the arrows, press Back to save", ()->{setResult(RESULT_OK,new Intent().putExtra(EXTRA_REORDER,true));finish();});
            row("Visible apps", "Hide shortcuts while keeping their apps installed",this::chooseVisible);
            text("Hold OK on any app on Home for dock and arrangement controls.",14,0xFFA9BDC9,18);
        } else if(category==2) {
            section("Background", "Give your Home a view.");
            row("Landscape",wallpaperName(),this::chooseWallpaper);
            row("Use my own picture","Choose a picture from USB or a file provider",this::importPicture);
            slider(body,"Dimming","custom_wallpaper_dim",0,70,22,v->v+"%");
            row("Landscape rotation",prefs.getInt("custom_rotation",0)==0?"Fixed picture":"Every "+prefs.getInt("custom_rotation",0)+" minutes",()->choice("Rotate landscapes",new String[]{"Fixed picture","Every 15 minutes","Every 30 minutes","Every hour"},i->{prefs.edit().putInt("custom_rotation",new int[]{0,15,30,60}[i]).remove(Config.WALLPAPER_CHOICE_KEY).apply();changed();}));
        } else if(category==3) {
            section("Weather", "Current conditions for your chosen city.");
            row("Location",prefs.getString("weather_city","Surat"),this::searchCity);
            toggleRow("Show weather","weather_enabled",true);
            row("Temperature",prefs.getBoolean("weather_fahrenheit",false)?"Fahrenheit":"Celsius",()->choice("Temperature unit",new String[]{"Celsius","Fahrenheit"},i->{prefs.edit().putBoolean("weather_fahrenheit",i==1).apply();changed();}));
            text("Weather by Open-Meteo.com · CC BY 4.0",13,0xFFA9BDC9,24);
        } else {
            section("Receivers", "Play from your phone or computer.");
            row("AirPlay settings","Mi TV AirPlay · receiver name, PIN and startup",()->openPackage("io.github.jqssun.airplay"));
            text("Chromecast and voice search remain available through the TV.\n\nPersonal Home is your independent fork of CleanYourTV.",15,0xFFADBFCA,24);
        }
        page.post(()->{View selected=findViewById(focus);if(selected!=null)selected.requestFocus();scroll.scrollTo(0,y);});
    }
    private android.graphics.drawable.Drawable panelBackground() {
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();
        d.setColor(0xFF1B2933);d.setCornerRadius(dp(22));d.setStroke(dp(1),0xFF2C414F);return d;
    }
    private void dockSettings() {
        section("Dock", "Use Left / Right to adjust. Return Home to see your changes.");
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout picture=new LinearLayout(this);picture.setOrientation(LinearLayout.VERTICAL);
        preview=new DockPreview(this);picture.addView(preview,new LinearLayout.LayoutParams(-1,dp(124)));
        TextView caption=new TextView(this);caption.setText("Layout preview");caption.setTextSize(13);caption.setTextColor(0xFFA9BDC9);caption.setPadding(0,dp(10),0,0);picture.addView(caption);
        panel.addView(picture,new LinearLayout.LayoutParams(dp(220),-2));
        LinearLayout controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.setMarginStart(dp(20));panel.addView(controls,cp);
        slider(controls,"Glass opacity",Config.DOCK_OPACITY_KEY,0,100,55,v->v+"%");
        slider(controls,"Glass blur","dock_blur",0,100,35,v->v==0?"Clear":v+"%");
        slider(controls,"Position","dock_position",0,100,0,v->v==0?"Bottom":v==100?"Top":v+"% up");
        slider(controls,"Thickness","dock_thickness",40,140,HomePreferences.dockThickness(this),v->v+"%");
        body.addView(panel,new LinearLayout.LayoutParams(-1,-2));
        toggleRow("Animate focus","animations",true);
        row("Reset dock appearance","Restore the starting position, thickness and glass",()->{
            prefs.edit().remove("dock_position").remove("dock_thickness").remove("compact_dock").remove("dock_blur").remove(Config.DOCK_OPACITY_KEY).apply();changed();
        });
    }
    private void section(String title,String detail){text(title,25,0xFFF1F5F7,0);text(detail,14,0xFFA9BDC9,6);}
    private void slider(LinearLayout parent,String title,String key,int min,int max,int fallback,java.util.function.IntFunction<String> format) {
        LinearLayout container=new LinearLayout(this);container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(10),dp(6),dp(10),dp(3));container.setBackgroundResource(R.drawable.settings_choice_bg);
        LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);
        TextView label=new TextView(this);label.setText(title);label.setTextSize(15);label.setTextColor(0xFFF0F4F7);line.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        TextView value=new TextView(this);value.setTextColor(0xFFB8D8E7);value.setTextSize(14);line.addView(value);
        container.addView(line);
        SeekBar slider=new SeekBar(this);slider.setId(101+rows++);slider.setMin(min);slider.setMax(max);slider.setKeyProgressIncrement(1);
        int chosen=DockLayout.clamp(prefs.getInt(key,fallback),min,max);slider.setProgress(chosen);value.setText(format.apply(chosen));
        slider.setThumbTintList(android.content.res.ColorStateList.valueOf(0xFFE6F4FA));slider.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF90C6DE));
        slider.setContentDescription(title+", "+format.apply(chosen));slider.setOnFocusChangeListener((v,on)->container.setSelected(on));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user){
                value.setText(format.apply(progress));bar.setContentDescription(title+", "+format.apply(progress));
                if(user){prefs.edit().putInt(key,progress).apply();HomePreferences.changed(LauncherSettingsActivity.this);setResult(RESULT_OK);if(preview!=null)preview.invalidate();}
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
        container.addView(slider,new LinearLayout.LayoutParams(-1,dp(36)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(10);parent.addView(container,p);
    }
    private void toggleRow(String title,String key,boolean fallback) {
        Switch toggle=new Switch(this);toggle.setId(101+rows++);toggle.setText(title);toggle.setTextSize(16);toggle.setTextColor(0xFFF0F4F7);
        toggle.setPadding(dp(16),0,dp(16),0);toggle.setBackgroundResource(R.drawable.settings_choice_bg);toggle.setChecked(prefs.getBoolean(key,fallback));
        toggle.setOnCheckedChangeListener((v,on)->{prefs.edit().putBoolean(key,on).apply();HomePreferences.changed(this);setResult(RESULT_OK);});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.topMargin=dp(12);body.addView(toggle,p);
    }
    private void text(String value,int size,int color,int margin) {
        TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(margin);p.bottomMargin=dp(10);body.addView(v,p);
    }
    private void row(String title,String detail,Runnable action) {
        LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(16),dp(10),dp(16),dp(10));v.setMinimumHeight(dp(58));v.setBackgroundResource(R.drawable.settings_choice_bg);v.setFocusable(true);v.setClickable(true);v.setId(101+rows++);
        TextView name=new TextView(this);name.setText(title);name.setTextSize(17);name.setTextColor(0xFFF2F5F6);v.addView(name);
        TextView sub=new TextView(this);sub.setText(detail);sub.setTextSize(13);sub.setTextColor(0xFFAFBFCA);v.addView(sub);
        v.setContentDescription(title+", "+detail);v.setOnClickListener(w->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(10);body.addView(v,p);
    }
    private void changed(){HomePreferences.changed(this);setResult(RESULT_OK);render();}
    private void choice(String title,String[] labels,java.util.function.IntConsumer action){new AlertDialog.Builder(this).setTitle(title).setItems(labels,(d,i)->action.accept(i)).setNegativeButton(android.R.string.cancel,null).show();}
    private List<String[]> apps() {
        PackageManager pm=getPackageManager();List<String[]> items=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),0)) {
            String p=r.activityInfo.packageName;
            if(p.equals(getPackageName()) || !seen.add(p))continue;
            items.add(new String[]{p,r.loadLabel(pm).toString()});
        }
        items.sort((a,b)->a[1].compareToIgnoreCase(b[1]));return items;
    }
    private String dockSummary() {
        List<String> names=new ArrayList<>();for(String p:HomePreferences.currentDock(this,Config.load(this))) {
            try { names.add(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(p,0)).toString()); }
            catch(PackageManager.NameNotFoundException ignored) { }
        }return String.join(" · ",names);
    }
    private void chooseDock() {
        List<String[]> apps=apps();List<String> selected=HomePreferences.currentDock(this,Config.load(this));
        apps.sort((a,b)-> {int ai=selected.indexOf(a[0]),bi=selected.indexOf(b[0]);return ai>=0&&bi>=0?Integer.compare(ai,bi):ai>=0?-1:bi>=0?1:a[1].compareToIgnoreCase(b[1]);});
        String[] labels=new String[apps.size()];boolean[] checks=new boolean[apps.size()];
        for(int i=0;i<apps.size();i++){labels[i]=apps.get(i)[1];checks[i]=selected.contains(apps.get(i)[0]);}
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Dock apps · choose up to 7")
            .setMultiChoiceItems(labels,checks,(d,i,on)-> {
                String pkg=apps.get(i)[0];
                if(on&&selected.size()>=7){((AlertDialog)d).getListView().setItemChecked(i,false);Toast.makeText(this,"The dock holds up to 7 apps",Toast.LENGTH_SHORT).show();return;}
                if(on&&!selected.contains(pkg))selected.add(pkg);else if(!on)selected.remove(pkg);
            }).setPositiveButton("Apply",null).setNegativeButton(android.R.string.cancel,null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(selected.isEmpty()){Toast.makeText(this,"Choose at least one dock app",Toast.LENGTH_SHORT).show();return;}
            HomePreferences.saveDock(this,selected);dialog.dismiss();setResult(RESULT_OK);render();
        }));dialog.show();
    }
    private void chooseVisible() {
        List<String[]> items=apps();Set<String> hidden=new HashSet<>(Config.load(this).hiddenApps);
        hidden.removeAll(LibraryApps.load(this)); hidden.addAll(LibraryApps.loadRemoved(this));
        String[] labels=new String[items.size()];boolean[] checks=new boolean[items.size()];
        for(int i=0;i<items.size();i++){labels[i]=items.get(i)[1];checks[i]=!hidden.contains(items.get(i)[0]);}
        final boolean[] originalChecks=checks.clone();
        new AlertDialog.Builder(this).setTitle("Visible apps")
            .setMultiChoiceItems(labels,checks,(d,i,on)->{if(on)hidden.remove(items.get(i)[0]);else hidden.add(items.get(i)[0]);})
            .setPositiveButton("Apply",(d,i)->{for(int at=0;at<items.size();at++) if(originalChecks[at]!=!hidden.contains(items.get(at)[0])) LibraryApps.save(this,items.get(at)[0],!hidden.contains(items.get(at)[0]));prefs.edit().putString("custom_hidden",String.join(",",hidden)).apply();changed();})
            .setNegativeButton(android.R.string.cancel,null).show();
    }
    private String wallpaperName() {
        String s=prefs.getString(Config.WALLPAPER_CHOICE_KEY,"Mountain valley.jpg");
        return "none".equals(s)?"Plain background":s.replace(".jpg","");
    }
    private void chooseWallpaper() {
        File dir=Config.dir(this);List<File> files=WallpaperRotation.images(dir==null?null:new File(dir,"wallpapers"));
        String[] labels=new String[files.size()+1];for(int i=0;i<files.size();i++)labels[i]=files.get(i).getName().replace(".jpg","");labels[files.size()]="Plain background";
        choice("Choose a landscape",labels,i->{prefs.edit().putString(Config.WALLPAPER_CHOICE_KEY,i==files.size()?"none":files.get(i).getName()).putInt("custom_rotation",0).apply();changed();});
    }
    private void importPicture() {
        try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*"),IMPORT); }
        catch(ActivityNotFoundException e){Toast.makeText(this,"No image picker is available. Connect a USB drive and install a file provider.",Toast.LENGTH_LONG).show();}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=IMPORT||result!=RESULT_OK||data==null||data.getData()==null)return;
        android.net.Uri uri=data.getData();Toast.makeText(this,"Importing landscape…",Toast.LENGTH_SHORT).show();
        worker.execute(()->{
            Bitmap bitmap=null;
            try {
                BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
                try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,options);}
                if(options.outWidth<=0||options.outHeight<=0)throw new IOException("Unreadable image");
                int sample=1;while(options.outWidth/sample>2560||options.outHeight/sample>1440)sample*=2;
                options.inJustDecodeBounds=false;options.inSampleSize=sample;
                try(InputStream in=getContentResolver().openInputStream(uri)){bitmap=BitmapFactory.decodeStream(in,null,options);}
                if(bitmap==null)throw new IOException("Unreadable image");
                File dir=Config.dir(this);if(dir==null)throw new IOException("Storage unavailable");
                File folder=new File(dir,"wallpapers");if(!folder.isDirectory()&&!folder.mkdirs())throw new IOException("Storage unavailable");
                File temp=new File(folder,"import.tmp");
                try(FileOutputStream stream=new FileOutputStream(temp)){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream))throw new IOException("Image conversion failed");}
                File dest=new File(folder,"My landscape.jpg");
                try(InputStream in=new FileInputStream(temp)){HomePreferences.copy(in,dest);}finally{temp.delete();}
                runOnUiThread(()->{if(isFinishing()||isDestroyed())return;prefs.edit().putString(Config.WALLPAPER_CHOICE_KEY,dest.getName()).putInt("custom_rotation",0).apply();changed();Toast.makeText(this,"Landscape imported",Toast.LENGTH_SHORT).show();});
            } catch(Exception|OutOfMemoryError e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Could not import this image",Toast.LENGTH_LONG).show();});}
            finally{if(bitmap!=null)bitmap.recycle();}
        });
    }
    private void searchCity() {
        EditText input=new EditText(this);input.setSingleLine(true);input.setHint("City, state or country");input.setText(prefs.getString("weather_city","Surat"));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Weather location").setView(input).setPositiveButton("Search",null).setNegativeButton(android.R.string.cancel,null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String q=input.getText().toString().trim();if(q.length()<2){input.setError("Enter a city name");return;}
            dialog.dismiss();Toast.makeText(this,"Searching locations…",Toast.LENGTH_SHORT).show();
            worker.execute(()->{
                try {
                    JSONArray results=WeatherLine.json("https://geocoding-api.open-meteo.com/v1/search?name="+URLEncoder.encode(q,"UTF-8")+"&count=8&language=en&format=json").optJSONArray("results");
                    if(results==null||results.length()==0)throw new IOException("No matching cities");
                    List<JSONObject> cities=new ArrayList<>();List<String> labels=new ArrayList<>();
                    for(int i=0;i<results.length();i++){JSONObject c=results.getJSONObject(i);double lat=c.getDouble("latitude"),lon=c.getDouble("longitude");if(!Double.isFinite(lat)||!Double.isFinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180)continue;cities.add(c);labels.add(c.getString("name")+", "+c.optString("admin1")+", "+c.optString("country"));}
                    if(cities.isEmpty())throw new IOException("No matching cities");
                    runOnUiThread(()->{if(isFinishing()||isDestroyed())return;choice("Choose a location",labels.toArray(new String[0]),i->{JSONObject c=cities.get(i);prefs.edit().putString("weather_city",c.optString("name")).putString("weather_lat",String.valueOf(c.optDouble("latitude"))).putString("weather_lon",String.valueOf(c.optDouble("longitude"))).apply();changed();});});
                }catch(Exception e){runOnUiThread(()->{if(!isDestroyed())Toast.makeText(this,"Location search unavailable. Please try again.",Toast.LENGTH_LONG).show();});}
            });
        }));dialog.show();
    }
    private void openPackage(String pkg) {
        Intent i=getPackageManager().getLeanbackLaunchIntentForPackage(pkg);if(i==null)i=getPackageManager().getLaunchIntentForPackage(pkg);
        if(i==null)Toast.makeText(this,"AirPlay receiver is not installed",Toast.LENGTH_SHORT).show();else open(i);
    }
    private void open(Intent i) { try{startActivity(i);}catch(ActivityNotFoundException e){Toast.makeText(this,"This screen is unavailable",Toast.LENGTH_SHORT).show();} }
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
