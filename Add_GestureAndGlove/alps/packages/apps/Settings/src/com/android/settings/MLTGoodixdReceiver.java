package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import android.content.SharedPreferences;
import com.android.settings.Goodix;
import android.provider.Settings;

//caoqiaofeng add MTSFEFL-14 20150323
public class MLTGoodixdReceiver extends BroadcastReceiver{

	private Context mContext;

	static final String TAG = "MLTGoodixdReceiver";
	
	static final String LOCK_TAG = "lock_tag";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
               Log.i(TAG, "MLT malataa 1");
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			Intent mIntent = new Intent(mContext, MLTGoodixService.class);
			mContext.startService(mIntent);	

			int mstate = Settings.System.getInt(mContext.getContentResolver(), Settings.System.GESTURE, -1);
			if(mstate>0){				
				Goodix.enableGesture();

				sendSwitchBroadcase(InputMethodAndLanguageSettings.TP_GESTURE_SWITCH_BROADCASE, "tp_gesture_switch_value" , true);
			}

			int status = Settings.System.getInt(context.getContentResolver(), Settings.System.GLOVE, -1);	
			if(status > 0){
				Goodix.enableStylus();
				sendSwitchBroadcase(InputMethodAndLanguageSettings.TP_GLOVE_SWITCH_BROADCASE, "tp_glove_switch_value", true);     
			}else{
				Goodix.disableStylus();
			}		
			
		}else if(intent.getAction().equals(InputMethodAndLanguageSettings.TP_GLOVE_SWITCH_BROADCASE)){  
				int status = Settings.System.getInt(context.getContentResolver(), Settings.System.GLOVE, -1);
				Log.i(TAG, "MLT malata test screen on status= "+status);
				if(status > 0){
					int mEnable = Goodix.enableStylus();
					Log.i("MLT","MLTGoodixdReceiver enable Goodix funtion success enableStylus mEnable = " + mEnable);
				}else{
					int mEnable = Goodix.disableStylus();
					Log.i("MLT","MLTGoodixdReceiver enable Goodix funtion success disableStylus mEnable = " + mEnable);
				}
				
			}else if(intent.getAction().equals(InputMethodAndLanguageSettings.TP_GESTURE_SWITCH_BROADCASE)){
			      Log.i(TAG, "malataa 2");
				int status = Settings.System.getInt(context.getContentResolver(), Settings.System.GESTURE, -1);

				  Log.i(TAG, "MLTGoodixdReceiver test screen on Gesture status= "+status);
				if(status > 0){
					int mEnable = Goodix.enableGesture();
					Log.i("MLT","MLTGoodixdReceiver enable Goodix funtion success enableGesture mEnable = " + mEnable);
				}else{
					int mEnable = Goodix.disableGesture();
					Log.i("MLT","MLTGoodixdReceiver enable Goodix funtion success disableGesture mEnable = " + mEnable);
				}			
			} 
			
	}

      	public void sendSwitchBroadcase(String strIntent, String strIntentVaule,boolean state){
	
		 if (mContext == null) return;
		
		Intent intent = new Intent(strIntent); 
		intent.putExtra(strIntentVaule, state ? 1 : 0);
		mContext.sendBroadcast(intent);				
	}
}
