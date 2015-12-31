package com.malata.superclean.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

import com.malata.superclean.bean.ProcessInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xuxiantao on 2015/9/15.
 */
public class AppUtil {

    public static List<String[]> mProcessList = null;

    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;

            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;

            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        Uri packageURI = Uri.parse("package:" + packageName);
        intent.setData(packageURI);
        context.startActivity(intent);
    }

    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
        Iterator<ActivityManager.RunningServiceInfo> infoIterator = serviceInfoList.iterator();

        while (infoIterator.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo) infoIterator.next();
            if(className.equals(runningServiceInfo.service.getClassName())) {
                isRunning = true;
            }
        }

        return isRunning;
    }

    public static boolean stopRunningService(Context context, String className) {
        Intent intent_service = null;
        boolean ret = false;
        try {
            intent_service = new Intent(context, Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(intent_service != null) {
            ret = context.stopService(intent_service);
        }

        return ret;
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager != null) {
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if(info != null && info.isConnected()) {
                    if(info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }

        return false;
    }

    public static boolean importDatabase(Context context, String dbName, int rawRes) {
        int buffer_size = 1024;
        InputStream is = null;
        FileOutputStream fos = null;
        boolean flag = false;

        try {
            String dbPath = "/data/data/" + context.getPackageName() + "/databases/" +dbName;
            File dbfile = new File(dbPath);
            if(!dbfile.exists()) {
                if(!dbfile.getParentFile().exists()) {
                    dbfile.getParentFile().mkdirs();
                }
                dbfile.createNewFile();
                is = context.getResources().openRawResource(rawRes);
                fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[buffer_size];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0 , count);
                }

                fos.flush();
            }

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return flag;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources mResources;
        if(context == null) {
            mResources = Resources.getSystem();
        } else {
            mResources = context.getResources();
        }

        DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
        return  mDisplayMetrics;
    }

    public static void showSoftInput(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void closeSoftInput(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager != null && ((Activity) context).getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            String packageName = context.getPackageName();
            info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            return packageInfo.packageName;
        } catch (Exception e) {

        }

        return "";
    }

    public static ApplicationInfo getApplicationInfo(Context context, String processName) {
        if(processName == null) {
            return null;
        }

        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for(ApplicationInfo applicationInfo : applicationInfoList) {
            if(processName.equals(applicationInfo.processName)) {
                return applicationInfo;
            }
        }

        return null;
    }

    public static ProcessInfo getMemInfo(String processName) {
        ProcessInfo process = new ProcessInfo();
        if(mProcessList == null) {
            mProcessList = getProcessRunningInfo();
        }
        String processNameTemp = "";

        for(Iterator<String[]> iterator = mProcessList.iterator(); iterator.hasNext();) {
            String[] item = (String[]) iterator.next();
            processNameTemp = item[9];
            // AbLogUtil.d(AbAppUtil.class,
            // "##"+item[9]+",NAME:"+processNameTemp);
            if (processNameTemp != null && processNameTemp.equals(processName)) {
                // AbLogUtil.d(AbAppUtil.class,
                // "##"+item[9]+","+process.memory);
                // Process ID
                process.pid = Integer.parseInt(item[0]);
                // CPU
                process.cpu = item[2];
                // S
                process.status = item[3];
                // thread
                process.threadsCount = item[4];
                // Mem
                long mem = 0;
                if (item[6].indexOf("M") != -1) {
                    mem = Long.parseLong(item[6].replace("M", "")) * 1000 * 1024;
                } else if (item[6].indexOf("K") != -1) {
                    mem = Long.parseLong(item[6].replace("K", "")) * 1000;
                } else if (item[6].indexOf("G") != -1) {
                    mem = Long.parseLong(item[6].replace("G", "")) * 1000 * 1024 * 1024;
                }
                process.memory = mem;
                // UID
                process.uid = item[8];
                // Process Name
                process.processName = item[9];
                break;
            }
        }
        if (process.memory == 0) {
            L.d(AppUtil.class, "##" + processName + ",top -n 1未找到");
        }
        return process;
    }

    public static List<String[]> getProcessRunningInfo() {
        List<String[]> processList = null;
        try {
            String result = runCommandTopN1();
            processList = parseProcessRunningInfo(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    public static String runCommandTopN1() {
        String result = null;
        try {
            String[] args = {"/system/bin/top", "-n", "1"};
            result = runCommand(args, "/system/bin/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String runCommand(String[] command, String workdirectory) {
        String result = "";
        L.d(AppUtil.class, "#" + command);
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            // set working directory
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream in = process.getInputStream();
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                String str = new String(buffer);
                result = result + str;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String[]> parseProcessRunningInfo(String info) {
        List<String[]> processList = new ArrayList<String[]>();
        int Length_ProcStat = 10;
        String tempString = "";
        boolean bIsProcInfo = false;
        String[] rows = null;
        String[] columns = null;
        rows = info.split("[\n]+");
        // 使用正则表达式分割字符串
        for (int i = 0; i < rows.length; i++) {
            tempString = rows[i];
            // AbLogUtil.d(AbAppUtil.class, tempString);
            if (tempString.indexOf("PID") == -1) {
                if (bIsProcInfo == true) {
                    tempString = tempString.trim();
                    columns = tempString.split("[ ]+");
                    if (columns.length == Length_ProcStat) {
                        // 把/system/bin/的去掉
                        if (columns[9].startsWith("/system/bin/")) {
                            continue;
                        }
                        // AbLogUtil.d(AbAppUtil.class,
                        // "#"+columns[9]+",PID:"+columns[0]);
                        processList.add(columns);
                    }
                }
            } else {
                bIsProcInfo = true;
            }
        }
        return processList;
    }

    public static long getAvailMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo.availMem;
    }

    public static long getTotalMemory(Context context) {
        String file = "/proc/meminfo";
        String memInfo;
        String[] strs;
        long memory = 0;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
            memInfo = bufferedReader.readLine();
            strs = memInfo.split("\\s+");
            for(String str : strs) {
                L.d(AppUtil.class, str + "\t");
            }
            memory = Integer.valueOf(strs[1]).intValue();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return memory*1024;
    }

}
