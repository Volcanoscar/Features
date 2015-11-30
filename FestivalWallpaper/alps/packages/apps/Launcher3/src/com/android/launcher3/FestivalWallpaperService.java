/**
 * tiandajiao add for festival wallpaper
 */
package com.android.launcher3;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class FestivalWallpaperService extends Service {

    public final static String ACTION_STOP_FESTIVAL_WALLPAPER = "stop_festival_wallpaper";  //停止节日壁纸
    public final static String ACTION_START_FESTIVAL_WALLPAPER = "start_festival_wallpaper";  //启动节日壁纸
    public final static String ACTION_CHANGE_FESTIVAL_WALLPAPER = "change_festival_wallpaper"; //更换壁纸
    public final static String ACTION_CHECK_FESTIVAL = "check_festival"; //检查是否节日
    public static final String FESTIVAL_NAME = "festival_name"; //默认壁纸
    public static final String PIC_NAME = "default.jpg";//保存
    private static boolean mFestivalWallpaperSwitch; //节日壁纸开关
    private static boolean mIsFestivalWallpaper = false; //当前壁纸是否节日壁纸
    private static String saveFilePath = "/saveWallpaper/";//位置 + 名字
    private static String lastDate = "";
    private final String SWITCH = "festival_wallpaper_switch";//保存节日壁纸开关状态

    private BroadcastReceiver mChangeWallpaperReceiver;
    private CheckFestivalReceiver mCheckFestivalReceiver;
    private TimeReceiver mTimeReceiver;

    /**
     * 主要注册了3个广播监听
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mFestivalWallpaperSwitch = (Settings.Secure.getInt(getContentResolver(),Settings.Secure.ACCESSIBILITY_FESTIVAL_WALLPAPER_ENABLED,0))==1;//获取节日壁纸开关的状态默认为关
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CHANGE_FESTIVAL_WALLPAPER);
        filter.addAction(ACTION_START_FESTIVAL_WALLPAPER);
        filter.addAction(ACTION_STOP_FESTIVAL_WALLPAPER);
        mChangeWallpaperReceiver = new ChangeFestivalWallpaperState();
        registerReceiver(mChangeWallpaperReceiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_CHECK_FESTIVAL);
        mCheckFestivalReceiver = new CheckFestivalReceiver();
        registerReceiver(mCheckFestivalReceiver, filter2);

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(Intent.ACTION_TIME_TICK);
        filter3.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter3.addAction(Intent.ACTION_TIME_CHANGED);
        mTimeReceiver = new TimeReceiver();
        registerReceiver(mTimeReceiver, filter3);
    }

    /**
     * 更换壁纸操作
     * parm context 提供更换壁纸的上下文
     * pram name 节日的名称
     */
    private void setWallPaper(Context context, String name) {
        if (!mFestivalWallpaperSwitch) {
            return;
        }
        name = name + ".jpg";
        String[] festivalList;
        AssetManager assetManager = getAssets();
        boolean exist = false;
        try {
            festivalList = assetManager.list("");
            for (String festivalName : festivalList) {
                if (festivalName.equals(name)) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                InputStream in = assetManager.open(name);
                WallpaperManager.getInstance(context).setStream(in);
                mIsFestivalWallpaper = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复节日壁纸为默认
     */
    private void setWallpaperToDefault(Context context) {
        String path = context.getFilesDir() + saveFilePath;
        File file = new File(path + PIC_NAME);
        InputStream in = null;
        if (file.exists()) {
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                WallpaperManager.getInstance(context).setStream(in);
                mIsFestivalWallpaper = false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 保存默认的壁纸
     */
    private void saveDefaultWallpaper(Context context) {

        if (mIsFestivalWallpaper) {
            return;
        }


        String path = context.getFilesDir() + saveFilePath;
        File wallpaper = new File(path);
        OutputStream out = null;
        if (!wallpaper.exists()) {
            wallpaper.mkdirs();
        }
        if (wallpaper.exists()) {
            try {
                File file = new File(path + PIC_NAME);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                out = new BufferedOutputStream(new FileOutputStream(file));
                BitmapDrawable bd = (BitmapDrawable) WallpaperManager
                        .getInstance(context).getDrawable();
                Bitmap bitmap = bd.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 注销广播监听
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mChangeWallpaperReceiver);
        unregisterReceiver(mCheckFestivalReceiver);
        unregisterReceiver(mTimeReceiver);
    }

    /**
     * 接收广播，判断ACTION 进行不同操作
     * ACTION_START_FESTIVALWALLPAPER  将开关打开 判断是否节日 是则更换壁纸
     * ACTION_CHANGE_FESTIVAL_WALLPAPER  修改壁纸为指定节日
     * ACTION_STOP_FESTIVAL_WALLPAPER 关闭开关 恢复默认壁纸
     */
    class ChangeFestivalWallpaperState extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHANGE_FESTIVAL_WALLPAPER.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                if (null != bundle) {
                    saveDefaultWallpaper(context);
                    String name = bundle.getString(FESTIVAL_NAME);
                    setWallPaper(context, name);
                    mIsFestivalWallpaper = true;
                }
            } else if (ACTION_START_FESTIVAL_WALLPAPER
                    .equals(intent.getAction())) {
                mFestivalWallpaperSwitch = true;
                getSharedPreferences(SWITCH, MODE_PRIVATE).edit().putBoolean(
                        SWITCH, true);
                Calendar calendar = Calendar.getInstance();
                CalendarUtil calendarUtil = new CalendarUtil(calendar);
                if (calendarUtil.isFestival()) {
                    String festivalName = calendarUtil.getFestival();
                    Intent changeWallpaper = new Intent();
                    changeWallpaper.setAction(ACTION_CHANGE_FESTIVAL_WALLPAPER);
                    changeWallpaper.putExtra(FESTIVAL_NAME, festivalName);
                    sendBroadcast(changeWallpaper);
                }
            } else if (ACTION_STOP_FESTIVAL_WALLPAPER
                    .equals(intent.getAction())) {
                mFestivalWallpaperSwitch = false;
                getSharedPreferences(SWITCH, MODE_PRIVATE).edit().putBoolean(
                        SWITCH, false);
                setWallpaperToDefault(context);
            }
        }
    }

    /**
     * 接收广播，判断当前日期是否为节日
     * 是节日则发出更换壁纸广播
     * 不是节日 则判断当前壁纸是否 节日壁纸 是则更换
     */
    class CheckFestivalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHECK_FESTIVAL.equals(intent.getAction())) {
                Calendar calendar = Calendar.getInstance();
                CalendarUtil calendarUtil = new CalendarUtil(calendar);
                if (calendarUtil.isFestival()) {
                    String festivalName = calendarUtil.getFestival();
                    Intent changeWallpaper = new Intent();
                    changeWallpaper.setAction(ACTION_CHANGE_FESTIVAL_WALLPAPER);
                    changeWallpaper.putExtra(FESTIVAL_NAME, festivalName);
                    sendBroadcast(changeWallpaper);
                } else {
                    if (mIsFestivalWallpaper) {
                        setWallpaperToDefault(context);
                    }
                }
            }
        }

    }

    /**
     * 接收系统广播，根据广播ACTION 执行不同的操作
     * Intent.ACTION_TIME_TICK 系统每分钟发出一次 收到后发出检查时间广播
     * ACTION_TIME_CHANGED 用户设置时间发出 收到后判断节日
     * ACTION_TIMEZONE_CHANGED 用户时区更改发出 收到后判断节日
     */
    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mFestivalWallpaperSwitch) {
                return;
            }
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                Calendar calendar = Calendar.getInstance();
                String currentDate = calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH)
                        + calendar.get(Calendar.DAY_OF_MONTH) + "";
                if ("".equals(lastDate)) {
                    lastDate = currentDate;
                } else {
                    if (lastDate.equals(currentDate)) {
                        return;
                    } else {
                        lastDate = currentDate;
                    }
                }
                Intent intent2 = new Intent();
                intent2.setAction(ACTION_CHECK_FESTIVAL);
                context.sendBroadcast(intent2);
            } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                Intent intent2 = new Intent();
                intent2.setAction(ACTION_CHECK_FESTIVAL);
                context.sendBroadcast(intent2);
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                Intent intent2 = new Intent();
                intent2.setAction(ACTION_CHECK_FESTIVAL);
                context.sendBroadcast(intent2);
            }

        }

    }
}


