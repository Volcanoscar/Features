package com.android.settings.accessibility;

import android.app.Activity;
import android.app.Service;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;
import android.util.Log;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;

/*
* this class used to open background service,listen in Shake the phone
*/
public class ShakeService extends Service {

    
    private static final String SAVESELECTSTATUS = "saveSelectStatus";
	private static final String UPDATESHAREDPREFERENCE="updateSharedPreference";
	private static final String SHAKESHAREDPREFERENCE="shake_spf";
	// if the speed is greater than Speed threshold it will be produce effect
    private static final int SPEEDTHRESHOLD = 1300;//400
    // Two detection interval
    private static final int UPTATEINTERVALTIME = 70;
	private String action = null;
	private SensorManager sensorManager;
    private String mLastSelect;
    private Vibrator vibrator;
	//Judgment screen light
	private boolean screenOn=true;
	
	// Previous position coordinate
    private float mLastX;
    private float mLastY;
    private float mLastZ;

    // Previous detection time
    private long mLastUpdateTime;

	
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			//if screen off ,Shake the phone do not open an application
			if(Intent.ACTION_SCREEN_OFF.equals(action)){
			screenOn=false;
			if (sensorManager!=null) {
            sensorManager.unregisterListener(sensorEventListener);
            }
	   		//if screen on ,Shake the phone do open an application
			}else if(Intent.ACTION_USER_PRESENT.equals(action)){
		    screenOn=true;
			if (sensorManager != null) {
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            }
			//update SharedPreference
			}else if(UPDATESHAREDPREFERENCE.equals(action)){
		    updateAppPackageName();
			}
           
        }

    };

	//Get the latest data of SharedPreferences
    private void updateAppPackageName() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHAKESHAREDPREFERENCE,
                Activity.MODE_PRIVATE);
        mLastSelect = sharedPreferences.getString(SAVESELECTSTATUS, "null");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHAKESHAREDPREFERENCE,
                Activity.MODE_PRIVATE);
        mLastSelect = sharedPreferences.getString(SAVESELECTSTATUS, "null");
    }

	//Open application based on application package name
    public void startApp(String appPackageName) {
        try {
            if (appPackageName!=null) {
                Intent intent = getPackageManager().getLaunchIntentForPackage(
                        appPackageName);
                startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(), R.string.accessibility_shake_open_app_description, Toast.LENGTH_SHORT)
                .show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.accessibility_shake_open_app_description, Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATESHAREDPREFERENCE);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		if (sensorManager!=null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
		Log.i("bro", "onDestroy");
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
        //    float[] values = event.values;
        //    float x = values[0];
        //    float y = values[1];
        //    float z = values[2];
        //    int minValues = 13;
        //    if (Math.abs(x) > minValues || Math.abs(y) > minValues
        //            || Math.abs(z) > minValues) {
        //        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //        if(screenOn){
		//	    startApp(mLastSelect);
		//		}       
        //    }
		
		long currentUpdateTime = System.currentTimeMillis();
       
        long timeInterval = currentUpdateTime - mLastUpdateTime;
       
        if (timeInterval < UPTATEINTERVALTIME) {
            return;
        }
    
        mLastUpdateTime = currentUpdateTime;

        // get X,Y,Z coordinate
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        float deltaZ = z - mLastZ;

        Log.i("XYZ","deltaX="+deltaX+" deltaY="+deltaY+" deltaZ="+deltaZ);
        // Current coordinates become the last coordinate
        mLastX = x;
        mLastY = y;
        mLastZ = z;

        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;
      
        if (speed >= SPEEDTHRESHOLD){
        if(screenOn&&isHome()){
		    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		    startApp(mLastSelect);
            vibrator.vibrate(200);
                    }		
        }
		}
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
	
	//Get desktop application
	private List<String> getHomes() {
        List<String> packageName = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfos) {
            packageName.add(ri.activityInfo.packageName);      
        }
        return packageName;
    }

	//Get the top of the stack application , Comparison with desktop applications
    private boolean isHome() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti=activityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

}
