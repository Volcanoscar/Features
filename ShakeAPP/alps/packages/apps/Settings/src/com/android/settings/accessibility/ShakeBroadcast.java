package com.android.settings.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.util.Log;
import android.provider.Settings;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

/*
* this class used to receive broadcast, to deal with the background service
*/


public class ShakeBroadcast extends BroadcastReceiver {

    private static final String TAG="ShakeBroadcast";
    private static final String BOOTCOMPLETED = Intent.ACTION_BOOT_COMPLETED;
	private static final String PACKAGEREMOVED= Intent.ACTION_PACKAGE_REMOVED;
    private static final String SWITCHPREFERENCEON = "com.malata.open.shake";
    private static final String SWITCHPREFERENCEOFF = "com.malata.close.shake";
    private static final String SAVESELECTSTATUS = "saveSelectStatus";
	private static final String UPDATESHAREDPREFERENCE="updateSharedPreference";
	private static final String SHAKESHAREDPREFERENCE="shake_spf";

    @Override
    public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"onReceive:"+intent.getAction());
		//When the boot is complete,get switch state,if switch status is true start service
		boolean shakeSwitchStatus = Settings.Secure.getInt(
            context.getContentResolver(), Settings.Secure.ACCESSIBILITY_SHAKE_ENABLED,
            0) == 1;
        if (intent.getAction().equals(BOOTCOMPLETED)) {
            if (shakeSwitchStatus) {
                context.startService(new Intent(context, ShakeService.class));
                Log.i(TAG, intent.getAction() + "");
            } else {
                return;
            }
		//if switch	is turn on,start service
        } else if (intent.getAction().equals(SWITCHPREFERENCEON)) {
            Log.i(TAG, intent.getAction() + "");
            context.startService(new Intent(context, ShakeService.class));
		//if switch	is turn of,stop service
        } else if (intent.getAction().equals(SWITCHPREFERENCEOFF)) {
            Log.i(TAG, intent.getAction() + "");     
            context.stopService(new Intent(context, ShakeService.class));
		//if delete application,update SharedPreference data
        } else if(intent.getAction().equals(PACKAGEREMOVED)){
			String packageName = intent.getData().getSchemeSpecificPart();
			SharedPreferences spf = context.getSharedPreferences(SHAKESHAREDPREFERENCE,
                    Activity.MODE_PRIVATE);
            String mLastSelect = spf.getString(SAVESELECTSTATUS, "null");
            if (mLastSelect.equals(packageName)) {
                PackageManager mPM = context.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> apps = mPM.queryIntentActivities(mainIntent, 0);
                String appPackageName=apps.get(0).activityInfo.applicationInfo.packageName;   
                SharedPreferences sharedPreferences = context
                        .getSharedPreferences(SHAKESHAREDPREFERENCE, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SAVESELECTSTATUS, appPackageName);
                editor.commit();  
				Intent mIntent=new Intent(UPDATESHAREDPREFERENCE);
				context.sendBroadcast(mIntent);
            }
		    Log.i(TAG, intent.getAction() + "\n"+packageName);    
		}
    }
}
