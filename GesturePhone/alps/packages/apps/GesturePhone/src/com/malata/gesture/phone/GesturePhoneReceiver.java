package com.malata.gesture.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import com.mediatek.common.featureoption.FeatureOption;

public class GesturePhoneReceiver extends BroadcastReceiver {
    private final static String TAG = "GesturePhoneService";
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.v(TAG, "GesturePhoneReceiver onReceive");
    	if (/*FeatureOption.MALATA_GESTURE_SUPPORT*/true) {
    		context.startService(new Intent(context, GesturePhoneService.class));
    	}
    }
}
