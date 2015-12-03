package com.malatamobile.smartgesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
import android.sax.StartElementListener;
import android.util.Log;

public class GestureReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Context contextSetting = null;
        
	    Log.d("malata", "GestureReceiver--onReceive()");
	    
	    if("android.intent.guest_broadcase".equals(action)) {
	    	
	    	int mstate = Settings.System.getInt(context.getContentResolver(), "gesture", -1);	    	
	    	
	    	int actionType = intent.getIntExtra("guest_key_code", -0x1);
	    	
		    try {
		    	 contextSetting = context.createPackageContext("com.android.settings", 0x2);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    String sGesturesKey= null;
		    if(actionType == GestureConst.GESTURE_SLIDE_C ){				
				sGesturesKey = "gestures_settings_c";				
			}else if(actionType == GestureConst.GESTURE_SLIDE_E ){			
				sGesturesKey = "gestures_settings_e";				
			}else if(actionType == GestureConst.GESTURE_SLIDE_M ){			
				sGesturesKey = "gestures_settings_m";				
			}else if(actionType == GestureConst.GESTURE_SLIDE_UP_SCREEN ){
				sGesturesKey = "gestures_settings_up";
			}

			else if(actionType == GestureConst.GESTURE_SLIDE_O ){			
				sGesturesKey = "gestures_settings_o";				
			}
			else if(actionType == GestureConst.GESTURE_SLIDE_W ){			
				sGesturesKey = "gestures_settings_w";				
			}
			else if(actionType == GestureConst.GESTURE_SLIDE_DOWN_SCREEN){			
				sGesturesKey = "gestures_settings_down";				
			}
			else if(actionType == GestureConst.GESTURE_SLIDE_S ){			
				sGesturesKey = "gestures_settings_s";				
			}
			else if(actionType == GestureConst.GESTURE_SLIDE_V ){			
				sGesturesKey = "gestures_settings_v";				
			}
			else if(actionType == GestureConst.GESTURE_SLIDE_Z ){			
				sGesturesKey = "gestures_settings_z";				
			}
			
		    SharedPreferences pre = contextSetting.getSharedPreferences(sGesturesKey, 4);
			String packageName = pre.getString("package_name", "");
			String activityName = pre.getString("activity_name", "");
			boolean gestrueStatus = pre.getBoolean("type_status", false);
			
	    	if((mstate>0) && gestrueStatus && !(packageName.isEmpty()) && !(activityName.isEmpty())){  
		        Intent mIntent = new Intent(context, GestureHandlerActivity.class);
		        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 		        
		        Bundle bundle = new Bundle();
		        bundle.putInt(GestureConst.GESTURE_INTENT_EXTRA_NAME, actionType);
		        bundle.putString(GestureConst.GESTURE_PACKAGE_NAME, packageName);
		        bundle.putString(GestureConst.GESTURE_ACTIVITY_NAME, activityName);
		        mIntent.putExtras(bundle);
		        context.startActivity(mIntent);
		        
		        return;
	    	}
	    }
    }
}
