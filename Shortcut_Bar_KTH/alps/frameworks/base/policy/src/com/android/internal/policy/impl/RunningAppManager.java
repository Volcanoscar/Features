package com.android.internal.policy.impl;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kongtianhao for shortcut bar on 2015/9/14.
 * kth add for LFZSF-10 
 * Load the runningapp and recentapp.
 */
public class RunningAppManager {
    private final String TAG = "RunningAppManager";
    private static final int MAX_RUNNING_TASKS = 16;
    private static final int MAX_RECENT_TASKS = MAX_RUNNING_TASKS * 2; // allow for the max num of recent task

    Context mContext;
    ActivityManager am;
    private static List<HashMap<String, Object>> appInfos = new ArrayList<HashMap<String, Object>>();
    List<ActivityManager.RecentTaskInfo> recentTasks;
    List<ActivityManager.RunningTaskInfo> runningTasks;
    List<ActivityManager.RunningAppProcessInfo> runningProcess;

    public RunningAppManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * @return The lists of the RunningApps
     */
    public List<HashMap<String, Object>> getRunningApp() {
        appInfos.clear();
        final PackageManager pm = mContext.getPackageManager();
        am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        runningProcess = am.getRunningAppProcesses();
        runningTasks = am.getRunningTasks(MAX_RUNNING_TASKS);//The maximum number of running list
        Log.i(TAG, "runningProcess = " + runningProcess.size());
        Log.i(TAG, "runningTasks = " + runningTasks.size());
        for (int i = 0; i< runningTasks.size(); i++){
            ActivityManager.RunningTaskInfo rt = runningTasks.get(i);
            if (i == 0){
                //skip the front task, also see the top of tasks stack
                continue;
            }
            HashMap<String, Object> runningAppInfo = new HashMap<String, Object>();
            ComponentName component = rt.baseActivity;
            String pkgName = component.getPackageName();
            String className = component.getClassName();
            String shortclassName = component.getShortClassName();
            ApplicationInfo applicationInfo = null;
			Log.i(TAG, "runningTasks pkgName = " + pkgName);
            if (pkgName.equals("com.android.launcher3") || pkgName.equals("com.android.systemui")) {
                // skip the launcher and systemui, the special tasks.
                continue;
            }
            try {
                applicationInfo = pm.getApplicationInfo(pkgName, 0);
                // skip the system app
//				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
//					continue;
//				}
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } finally {
            }

            String applicationName = (String) pm.getApplicationLabel(applicationInfo);
            Drawable icon = pm.getApplicationIcon(applicationInfo);
            int id = rt.id;
            ActivityManager.TaskThumbnail taskThumbnail = am.getTaskThumbnail(id);
            Bitmap thumbnail = taskThumbnail.mainThumbnail;
            runningAppInfo.put("pkgname", pkgName);
            runningAppInfo.put("appname", applicationName);
            runningAppInfo.put("thumbnail", thumbnail);
            runningAppInfo.put("taskid", id);
            runningAppInfo.put("icon", icon);
            appInfos.add(runningAppInfo);
        }
        return appInfos;
    }

    /**
     * reload the recentApp it is not used
     *
     * @return The lists of the RecentApps
     */
    public List<HashMap<String, Object>> reloadRecentApp() {
        appInfos.clear();
        final PackageManager pm = mContext.getPackageManager();
        am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        recentTasks = am.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        ActivityInfo frontInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm,
                0);
        int numTasks = recentTasks.size();
        Log.i(TAG, "numTasks = " + numTasks);
        for (int j = 0; j < numTasks && (j < MAX_RUNNING_TASKS); ++j) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(j);
            HashMap<String, Object> singleAppInfo = new HashMap<String, Object>();
            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }
            // Skip the current home activity.
            if (frontInfo != null) {
                if (frontInfo.packageName.equals(intent.getComponent().getPackageName())
                        && frontInfo.name.equals(intent.getComponent().getClassName())) {
                    continue;
                }
            }
            intent.setFlags(
                    (intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                final String title = activityInfo.loadLabel(pm).toString();
                final int taskid = info.id;
                Drawable icon = activityInfo.loadIcon(pm);
                Log.i(TAG, "title = " + title + "taskid = " + taskid);
                if (title != null && title.length() > 0 && icon != null) {
                    singleAppInfo.put("title", title);
                    singleAppInfo.put("icon", icon);
                    singleAppInfo.put("taskid", taskid);
                    singleAppInfo.put("intent", intent);
                    appInfos.add(singleAppInfo);
                }
            }
        }
        return appInfos;
    }
}