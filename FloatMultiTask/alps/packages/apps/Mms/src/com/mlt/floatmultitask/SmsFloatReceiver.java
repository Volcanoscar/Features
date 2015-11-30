package com.mlt.floatmultitask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.pm.PackageManager.NameNotFoundException;

public class SmsFloatReceiver extends BroadcastReceiver {

    private SharedPreferences sp;

    public SmsFloatReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
       if("com.malata.floatmultitask.action.sms.show".equals(action)) {
		   Log.i("haha", "get sms show");
           Intent startService = new Intent(context, SmsFloatService.class);
           context.stopService(startService);
           context.startService(startService);

        } else if("com.malata.floatmultitask.action.sms.close".equals(action)) {
           Log.i("haha", "get sms close");
           Intent startService = new Intent(context, SmsFloatService.class);
           context.stopService(startService);
//           context.startService(startService);
        } else if("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
		   // if receive message, show float window
		   try {
			  Context multiTaskContext = context.createPackageContext("com.mlt.floatmultitask"
			  	,Context.CONTEXT_IGNORE_SECURITY);

		  	  SharedPreferences sp = multiTaskContext.
			  	getSharedPreferences("status", Context.MODE_WORLD_READABLE);
			  if(sp.getBoolean("isAutoShowSms", false)) {
		 	  	if(SmsFloatManager.getMsgWindow() == null) {
					Intent i = new Intent("com.malata.floatmultitask.action.cleanbesidesms");
					i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					context.sendBroadcast(i);
		   	   		Intent startService = new Intent(context, SmsFloatService.class);
           	    	context.stopService(startService);
           	    	context.startService(startService);
		 	  	}
			  }
		   } catch (NameNotFoundException e) {
			  e.printStackTrace();
		   }

		}
    }
}

