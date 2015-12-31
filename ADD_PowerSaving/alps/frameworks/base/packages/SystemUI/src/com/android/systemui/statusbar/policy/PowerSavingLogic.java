package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;

public class PowerSavingLogic {
	private static final String TAG = "PowerSavingLogic";
	private static final String KEY_POWER_SAVING = "power_saving";
	private static final String KEY_POWER_SAVING_BRIGHTNESS = "power_saving_screenlight";
	private static final String KEY_POWER_SAVING_BT = "power_saving_bt";
	private static final String KEY_POWER_SAVING_DATACONN = "power_saving_dataconn";
	private static final String KEY_POWER_SAVING_GPS = "power_saving_gps";	  
	private static final String KEY_POWER_SAVING_WIFI = "power_saving_wifi";
	private static final String KEY_POWER_SAVING_PERCENT = "power_saving_percent";
	  
	private Context mContext;
	private int mBatterPercent;
	private boolean mBPowerSaving;
	private boolean mBWifi;
	private boolean mBBT;
	private boolean mBDataConn;
	private boolean mBGPS;
	private boolean mBrightness;
	
	public void init(){

	readDataBase();
	        
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING),true, mPowerSavingObserver);  
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_BRIGHTNESS),true, mPowerSavingObserver); 
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_BT),true, mPowerSavingObserver); 
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_DATACONN),true, mPowerSavingObserver); 
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_GPS),true, mPowerSavingObserver); 
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_WIFI),true, mPowerSavingObserver); 
        mContext.getContentResolver().registerContentObserver(
        		Settings.System.getUriFor(KEY_POWER_SAVING_PERCENT),true, mPowerSavingObserver);        

        
//        if(mBPowerSaving){
        	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        	mContext.registerReceiver(mBatteryReceiver, filter);
//        }
	}
	public void deInit(){
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		mContext.getContentResolver().unregisterContentObserver(mPowerSavingObserver);
		
//		if(mBPowerSaving){
			mContext.unregisterReceiver(mBatteryReceiver);
//		}
	}
	
	public PowerSavingLogic(Context context) {
		super();
		mContext = context;		
	}
	private void disableWifi(){
		Log.d(TAG, "disable wifi");	
        final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {        	
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... args) {
                    wifiManager.setWifiEnabled(false);
                    return null;
                }
            }.execute();            
        }
	}
	private void disableBt(){
		Log.d(TAG, "disable BT");
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {         
	        new AsyncTask<Void, Void, Void>() {
	            @Override
	            protected Void doInBackground(Void... args) {
                    bluetoothAdapter.disable();
	                return null;
	            }
	        }.execute();
        }
	}
	private void disableGps(){
		Log.d(TAG, "disable gps");
        new AsyncTask<Void, Void, Integer>() { 
	        @Override
	        protected Integer doInBackground(Void... args) {
	        	if(isLocationEnabled()){
	        		setLocationEnabled(false);
	        	}
	        	return null;
	        }
	        /**
	         * Enable or disable location in settings.
	         *
	         * <p>This will attempt to enable/disable every type of location setting
	         * (e.g. high and balanced power).
	         *
	         * <p>If enabling, a user consent dialog will pop up prompting the user to accept.
	         * If the user doesn't accept, network location won't be enabled.
	         *
	         * @return true if attempt to change setting was successful.
	         */
	        public boolean setLocationEnabled(boolean enabled) {
	            int currentUserId = ActivityManager.getCurrentUser();
	            if (isUserLocationRestricted(currentUserId)) {
	                return false;
	            }
	            final ContentResolver cr = mContext.getContentResolver();
	            // When enabling location, a user consent dialog will pop up, and the
	            // setting won't be fully enabled until the user accepts the agreement.
	            int mode = enabled
	                    ? Settings.Secure.LOCATION_MODE_HIGH_ACCURACY : Settings.Secure.LOCATION_MODE_OFF;
	            // QuickSettings always runs as the owner, so specifically set the settings
	            // for the current foreground user.
	            return Settings.Secure
	                    .putIntForUser(cr, Settings.Secure.LOCATION_MODE, mode, currentUserId);
	        }

	        /**
	         * Returns true if location isn't disabled in settings.
	         */
            public boolean isLocationEnabled() {
                ContentResolver resolver = mContext.getContentResolver();
                // QuickSettings always runs as the owner, so specifically retrieve the settings
                // for the current foreground user.
                int mode = Settings.Secure.getIntForUser(resolver, Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
                return mode != Settings.Secure.LOCATION_MODE_OFF;
            }

            /**
             * Returns true if the current user is restricted from using location.
             */
            private boolean isUserLocationRestricted(int userId) {
                final UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
                return um.hasUserRestriction(
                        UserManager.DISALLOW_SHARE_LOCATION,
                        new UserHandle(userId));
            }
            
        }.execute();
	}
	private void disableDataConn(){
		Log.d(TAG, "disable dataconnection");
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... args) {
//                ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                TelephonyManager tm = TelephonyManager.from(mContext);
                if(tm.getDataEnabled()){
                	tm.setDataEnabled(false);
                    //tm.setDataEnabled(enabled);
                    // cm.isNetworkSupported(TYPE_MOBILE)
                    //&& tm.getSimState() == SIM_STATE_READY;
                }
                return null;
            }
        }.execute();
	}
	//change to auto mode
	private void disableBrightness(){
		Log.d(TAG, "disable brightness");
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... args) {
                        int mode = Settings.System.getIntForUser(mContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                                ActivityManager.getCurrentUser());
                        if(mode != Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
                            Settings.System.putIntForUser(mContext.getContentResolver(), 
                            Settings.System.SCREEN_BRIGHTNESS_MODE, 
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC, 
                            ActivityManager.getCurrentUser());
                        }
                        return null;
                    }
                }.execute();
	}
    private void update(){
    	if(!mBPowerSaving) return;
    	
    	if(mBWifi) disableWifi();
    	if(mBBT) disableBt();
    	if(mBGPS) disableGps();
    	if(mBDataConn) disableDataConn();
    	if(mBrightness) disableBrightness();    	
    } 

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {			
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {				
				int status = intent.getIntExtra("status", 0);
				if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING){
					int percent = intent.getIntExtra("level",  0) * 100 /intent.getIntExtra("scale", 100);
					if(mBPowerSaving && percent <= mBatterPercent){
						update();
					}
				}	
			}			
		}
	};
    private ContentObserver mPowerSavingObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {  
        	readDataBase();        	
        }
    };    
    private void readDataBase(){
        //set default values
        if(-1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING, -1)){
               	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING, 0);            
                Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_WIFI, 0);
        	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_BT, 0);
        	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_GPS, 0);
        	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_DATACONN, 0);
        	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_BRIGHTNESS, 0);
        	Settings.System.putInt(mContext.getContentResolver(), KEY_POWER_SAVING_PERCENT, 3);
        }
        
    	mBPowerSaving = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING, 0);
    	mBWifi = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_WIFI, 0);
    	mBBT = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_BT, 0);
    	mBGPS = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_GPS, 0);
    	mBDataConn = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_DATACONN, 0);
    	mBrightness = 1 == Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_BRIGHTNESS, 0);
    	int value = Settings.System.getInt(mContext.getContentResolver(), KEY_POWER_SAVING_PERCENT, 0);
    	
    	switch(value){
    	case 1:
    		mBatterPercent = 10;
    		break;
    	case 2:
    		mBatterPercent = 20;
    		break;
    	default:
    	case 3:
    		mBatterPercent = 30;
    		break;
    	case 4:
    		mBatterPercent = 40;
    		break;
    	case 5:
    		mBatterPercent = 50;
    		break;    	
    	}

    	Log.d(TAG, "mBPowerSaving="+mBPowerSaving+", mBWifi="+mBWifi+", mBBT="+mBBT+", mBGPS="+mBGPS+
    	"\nmBDataConn="+mBDataConn+", mBrightness=" + mBrightness + " mBatterPercent="+mBatterPercent);
    }
}
