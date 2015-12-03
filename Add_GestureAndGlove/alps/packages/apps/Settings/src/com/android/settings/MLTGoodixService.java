package com.android.settings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings;
//caoqiaofeng add MTSFEFL-14 20150323
public class MLTGoodixService extends Service{

	private final static String TAG = "MLTGoodixService";
	private IntentFilter   mIntentFilter;
	public static final String MLT_NAME = "malata_group";
	public static final String MLT_SETTING = "malata_setting";
	public static final String MLT_STATUS = "malata_status";
	public static int ENABLE_STYLUS = 1;
	public static int DISABLE_STYLUS = 0;
	private boolean mEnableFlag = true;
	private boolean mDisableFlag = false;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "MLTGoodixService onCreate()");
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
	 mIntentFilter.addAction("android.intent.action.ACTION_SHUTDOWN"); 
        registerReceiver(mIntentReceiver, mIntentFilter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		Context mContext;

		@Override
		public void onReceive(Context context, Intent intent) {
			mContext = context;

			String action = intent.getAction();
	
			int mResult = Settings.System.getInt(mContext.getContentResolver(), Settings.System.GLOVE, -1); 
		
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra("status", 0);
				int plugged = intent.getIntExtra("plugged", 0);

				if ((status == BatteryManager.BATTERY_STATUS_CHARGING)|| (plugged != 0)) {

					if (((mResult == ENABLE_STYLUS) && mEnableFlag) /*|| (mRestart == 1)*/) {  
						int mDisable = Goodix.disableStylus();
						if (mDisable > 0) {
							Toast.makeText(mContext, R.string.close_glove_mode, Toast.LENGTH_SHORT).show();
							Settings.System.putInt(mContext.getContentResolver(), Settings.System.GLOVE, 0);
							mEnableFlag = false;
							mDisableFlag = true; 

							 if (context == null) {
							 	return;
							 }else{
								Intent intentDisable = new Intent(/*TP_GLOVE_SWITCH_BROADCASE*/"tp_glove_switch_broadcase"); 
								intentDisable.putExtra("tp_glove_switch_value", 0);
								context.sendBroadcast(intentDisable);
							 }							
						}
					}
				} else if ((status == BatteryManager.BATTERY_STATUS_NOT_CHARGING)|| (plugged == 0)) {
					if (mDisableFlag) {
						int mEnable = Goodix.enableStylus();
						if (mEnable > 0) {
							Toast.makeText(mContext,R.string.reopen_glove_mode,Toast.LENGTH_SHORT).show();
							Settings.System.putInt(mContext.getContentResolver(), Settings.System.GLOVE, 1); 
							mEnableFlag = true;
							mDisableFlag = false; 

							 if (context == null) {
							 	return;
							 }else{
								Intent intentEnable = new Intent(/*TP_GLOVE_SWITCH_BROADCASE*/"tp_glove_switch_broadcase"); 
								intentEnable.putExtra("tp_glove_switch_value",1);
								context.sendBroadcast(intentEnable);
							 }							
						}
					} 
				}
			}
			else if(action.equals("android.intent.action.ACTION_SHUTDOWN")){
				if(!mEnableFlag){
					Settings.System.putInt(mContext.getContentResolver(), Settings.System.GLOVE, 1);
				}
			}
		}
	};
 
}
