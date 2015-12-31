package com.malata.superclean.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by xuxiantao on 2015/9/17.
 */
public class Utils {

    public static boolean isSystemApp(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public static boolean isSystemUpdateApp(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public static boolean isUserApp(PackageInfo packageInfo) {
        return (!isSystemApp(packageInfo) && !isSystemUpdateApp(packageInfo));
    }

    public static long getPkgSize(final Context context, String pkgName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        long pkgSize = 0;
        Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
        method.invoke(context.getPackageManager(), new Object[]{
                pkgName,
                new IPackageStatsObserver.Stub() {

                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        Looper.prepare();

                        Looper.loop();
                    }
                }
        });

        return pkgSize;
    }

    public static void launchBrowser(Activity from, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_uri = Uri.parse(url);
        intent.setData(content_uri);
        from.startActivity(intent);
    }

    public static boolean isIntentSafe(Activity activity, Uri uri) {
        Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);

        return activities.size() > 0;
    }

}
