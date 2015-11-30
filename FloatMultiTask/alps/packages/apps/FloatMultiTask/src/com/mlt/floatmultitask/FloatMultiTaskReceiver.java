package com.mlt.floatmultitask;;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * filename:FloatMultiTaskReceiver.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-6
 * author: laiyang
 *
 * The class is to receive broadcast
 *
 * Modification History
 * -----------------------------------
 *
 * -----------------------------------
 */
public class FloatMultiTaskReceiver extends BroadcastReceiver {

    private SharedPreferences sp;

    public FloatMultiTaskReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
       if("com.malata.floatmultitask.action.changestatus".equals(action)) {
            if(1 == intent.getIntExtra("value", 0)) {// show float button
                sp = context.getSharedPreferences("status", Context.MODE_APPEND| Context.MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isOpen", true);
                editor.commit();
                Intent startService = new Intent(context, FloatMultiTaskService.class);
                context.stopService(startService);
                context.startService(startService);
            }
        } else if("com.malata.floatmultitask.action.close".equals(action)) {// close float button
           sp = context.getSharedPreferences("status", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
           SharedPreferences.Editor editor = sp.edit();
           editor.putBoolean("isOpen", false);
           editor.commit();
           // close sms float window
		   Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
           i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
           context.sendBroadcast(i);
           Intent stopService = new Intent(context, FloatMultiTaskService.class);
           context.stopService(stopService);
        } else if("android.intent.action.BOOT_COMPLETED".equals(action)) {// when boot completed,show float button
            sp = context.getSharedPreferences("status", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
            if(sp.getBoolean("isOpen", false)) {
                Intent startService = new Intent(context, FloatMultiTaskService.class);
                context.startService(startService);
            }
        } else if("com.malata.floatmultitask.action.showfloatbutton".equals(action)) {// show float button
            Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
            MultiTaskManager.createFLoatButton(context);
        } else if("com.malata.floatmultitask.action.showmainwindow".equals(action)) {// show main window
            Intent i = new Intent("com.malata.floatmultitask.action.sms.close");
            i.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
            MultiTaskManager.createMainWindow(context);
        } else if("com.malata.floatmultitask.action.cleanbesidesms".equals(action)) {// clear other flaot window beside sms float window
			MultiTaskManager.removeNoteWindow(context);
			MultiTaskManager.removeMusicWindow(context);
			MultiTaskManager.removeVideoWindow(context);
			MultiTaskManager.removeFloatButton(context);
			MultiTaskManager.removeMainWindow(context);
	    } else if("com.malata.floatmultitask.action.enableautoshowsms".equals(action)) {// set auto show sms float window enabled
           Log.i("haha", "get set auto show sms");
			sp = context.getSharedPreferences("status", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isAutoShowSms", true);
			editor.commit();
		} else if("com.malata.floatmultitask.action.disableautoshowsms".equals(action)) {// set auto show sms float window disabled
           Log.i("haha", "get set auto show sms false");
			sp = context.getSharedPreferences("status", Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isAutoShowSms", false);
			editor.commit();
		}
    }
}
