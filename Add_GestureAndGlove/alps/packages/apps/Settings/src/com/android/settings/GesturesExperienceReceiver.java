package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

//caoqiaofeng add MTSFEFL-14 20150323
public class GesturesExperienceReceiver extends BroadcastReceiver{

	private Context mContext;
	static final String TAG = "GesturesExperienceReceiver";	
	static final String[] type = new String[]{
		GesturesExperienceSettings.GESTURES_SETTINGS_UP,
		GesturesExperienceSettings.GESTURES_SETTINGS_C,
		GesturesExperienceSettings.GESTURES_SETTINGS_DOWN,
		GesturesExperienceSettings.GESTURES_SETTINGS_E,
		GesturesExperienceSettings.GESTURES_SETTINGS_M,
		GesturesExperienceSettings.GESTURES_SETTINGS_O,
		GesturesExperienceSettings.GESTURES_SETTINGS_S,
		GesturesExperienceSettings.GESTURES_SETTINGS_V,
		GesturesExperienceSettings.GESTURES_SETTINGS_W,
		GesturesExperienceSettings.GESTURES_SETTINGS_Z
	};
	
 	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;

		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
			
                  String packageName = intent.getData().getSchemeSpecificPart();
		    removedAPK(packageName);
			
               }
	}

       public void removedAPK(String packageName){
	   for(int i=0; i<type.length; i++){
	   	
	   	SharedPreferences pre = mContext.getSharedPreferences(type[i], /*MODE_WORLD_READABLE*/1);
              String tepPackageName = pre.getString("package_name", "");  			 
		
	   	if(tepPackageName.equals(packageName)){
			
			resetGesturesValue(type[i]);			
			
		}
		
	   }
	 }
	   
	public boolean resetGesturesValue(String type){
		
	        SharedPreferences.Editor editor = mContext.getSharedPreferences( type, /*MODE_WORLD_WRITEABLE*/2).edit();
	        editor.putString("package_name", null);
	        editor.putString("activity_name", null);
	        editor.putString("app_name", null);	
		 
		 if(GesturesExperienceSettings.GESTURES_SETTINGS_UP == type){
		 	
			editor.putBoolean("type_status", true);
			
		 }else{
		 
			editor.putBoolean("type_status", false);
			
		 }
	                       
		 if(editor.commit()){
			return true;			
		 }
		  
		 return false;
			
		  
    }
}
