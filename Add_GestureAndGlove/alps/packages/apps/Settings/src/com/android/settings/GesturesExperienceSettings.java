/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.mediatek.settings.ext.ISettingsMiscExt;
import android.util.Log;
import com.android.settings.R;
import android.content.ComponentName;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.mediatek.settings.UtilsExt;

import android.preference.SwitchPreference;
//caoqiaofeng add MTSFEFL-14 20150323
public class GesturesExperienceSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{// implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{
    
    private ISettingsMiscExt mExt;    
    private SwitchPreference mGestureMode;
    private MySwitchPreference mGesturesSettingsUp;
    private MySwitchPreference mGesturesSettingsC;
    private MySwitchPreference mGesturesSettingsE;
    private MySwitchPreference mGesturesSettingsM;
    private MySwitchPreference mGesturesSettingsO;	
    private MySwitchPreference mGesturesSettingsW;
    private MySwitchPreference mGesturesSettingsDOWN;
    private MySwitchPreference mGesturesSettingsS;
    private MySwitchPreference mGesturesSettingsV;	
    private MySwitchPreference mGesturesSettingsZ;	
    
    public static final String GESTURES_MODE = "gesture_mode";
    public static final String GESTURES_SETTINGS_UP = "gestures_settings_up";
    public static final String GESTURES_SETTINGS_C = "gestures_settings_c";
    public static final String GESTURES_SETTINGS_E = "gestures_settings_e";
    public static final String GESTURES_SETTINGS_M = "gestures_settings_m";

    public static final String GESTURES_SETTINGS_O = "gestures_settings_o";
    public static final String GESTURES_SETTINGS_W = "gestures_settings_w";
    public static final String GESTURES_SETTINGS_DOWN = "gestures_settings_down";
    public static final String GESTURES_SETTINGS_S = "gestures_settings_s";
    public static final String GESTURES_SETTINGS_V = "gestures_settings_v";
    public static final String GESTURES_SETTINGS_Z = "gestures_settings_z";	

    public static final String TP_GESTURE_SWITCH_BROADCASE = "tp_gesture_switch_broadcase";	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gestures_experience_settings);
        final PreferenceScreen screen = getPreferenceScreen();
        
        mGestureMode = (SwitchPreference) screen.findPreference(GESTURES_MODE);
	 mGestureMode.setOnPreferenceChangeListener(this);				
        mGesturesSettingsUp = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_UP);
	 mGesturesSettingsUp.setOnPreferenceChangeListener(this);	
        mGesturesSettingsC = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_C);
	 mGesturesSettingsC.setOnPreferenceChangeListener(this);	
        mGesturesSettingsE = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_E);
	 mGesturesSettingsE.setOnPreferenceChangeListener(this);	
        mGesturesSettingsM = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_M);
	 mGesturesSettingsM.setOnPreferenceChangeListener(this);	       

	 mGesturesSettingsO = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_O);
	 mGesturesSettingsO.setOnPreferenceChangeListener(this);	

	 mGesturesSettingsDOWN= (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_DOWN);
	 mGesturesSettingsDOWN.setOnPreferenceChangeListener(this);	

	 mGesturesSettingsW= (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_W);
	 mGesturesSettingsW.setOnPreferenceChangeListener(this);	

	 mGesturesSettingsS = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_S);
	 mGesturesSettingsS.setOnPreferenceChangeListener(this);	

	 mGesturesSettingsV = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_V);
	 mGesturesSettingsV.setOnPreferenceChangeListener(this);	

	 mGesturesSettingsZ = (MySwitchPreference) screen.findPreference(GESTURES_SETTINGS_Z);
	 mGesturesSettingsZ.setOnPreferenceChangeListener(this);	
        
        mExt = UtilsExt.getMiscPlugin(getActivity());
		
    	 mIntentFilter = new IntentFilter();
 	 mIntentFilter.addAction(TP_GESTURE_SWITCH_BROADCASE); 	
	 getActivity().registerReceiver(mIntentReceiver, mIntentFilter);	
	 setHasOptionsMenu(true);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Refresh UI
        updateToggles();	 
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	Log.d("malata", "qiao onPreferenceChange ");    
	 boolean gestureStatus = false;
	 String gesturesType = null;
	 
        if(preference == mGesturesSettingsC){

		gesturesType = GESTURES_SETTINGS_C;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }
		 
        }else if(preference == mGesturesSettingsUp){

		gesturesType = GESTURES_SETTINGS_UP;
	      if(((MySwitchPreference)preference).isChecked()) {
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }		
		  
        }else if(preference == mGesturesSettingsE){

		gesturesType = GESTURES_SETTINGS_E;
	      if(((MySwitchPreference)preference).isChecked()) {
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }		
		  
        }else if(preference == mGesturesSettingsM){       

		gesturesType = GESTURES_SETTINGS_M;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsO){       

		gesturesType = GESTURES_SETTINGS_O;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsW){       

		gesturesType = GESTURES_SETTINGS_W;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsDOWN){       

		gesturesType = GESTURES_SETTINGS_DOWN;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsS){       

		gesturesType = GESTURES_SETTINGS_S;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsV){       

		gesturesType = GESTURES_SETTINGS_V;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGesturesSettingsZ){       

		gesturesType = GESTURES_SETTINGS_Z;
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			gestureStatus = false;
			
		 }else{
			gestureStatus = true;
		 }	
		 
        }else if(preference == mGestureMode){
		
		 if(((SwitchPreference)preference).isChecked()) {
		 	gestureStatus = false;
			Settings.System.putInt(getActivity().getContentResolver(), Settings.System.GESTURE, gestureStatus?1:0); 			
	   		sendSwitchBroadcase(TP_GESTURE_SWITCH_BROADCASE, "tp_gesture_switch_value" , gestureStatus); 						
		 }else{
			createWarningDialog();
		 }	   		
            
	     return true;
	     		
	}

	Log.d("malata", "onPreferenceChange gesturesType="+gesturesType);    
	if(gesturesType!=null){
		SharedPreferences.Editor editor = getActivity().getSharedPreferences( gesturesType, /*MODE_WORLD_WRITEABLE*/2).edit();
		Log.d("malata", "onPreferenceChange gestureStatus="+gestureStatus); 
		editor.putBoolean("type_status", gestureStatus);		
		editor.commit();
	}
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
          Log.d("malata", "qiao onPreferenceTreeClick ");    
	 
        if(preference == mGesturesSettingsC){
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_C);				
		 }		
        }else if(preference == mGesturesSettingsUp){
	      if(((MySwitchPreference)preference).isChecked()) {
			startSelectActivity(GESTURES_SETTINGS_UP);			
		 }		  
        }else if(preference == mGesturesSettingsE){
	      if(((MySwitchPreference)preference).isChecked()) {
			startSelectActivity(GESTURES_SETTINGS_E);
		 }				  
        }else if(preference == mGesturesSettingsM){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_M);
			
		 }		 
        }else if(preference == mGesturesSettingsO){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_O);
			
		 }		 
        }else if(preference == mGesturesSettingsW){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_W);
			
		 }		 
        }else if(preference == mGesturesSettingsDOWN){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_DOWN);
			
		 }		 
        }else if(preference == mGesturesSettingsS){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_S);
			
		 }		 
        }else if(preference == mGesturesSettingsV){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_V);
			
		 }		 
        }else if(preference == mGesturesSettingsZ){       
		 if(((MySwitchPreference)preference).isChecked()) {		 	
			startSelectActivity(GESTURES_SETTINGS_Z);
			
		 }		 
        }
	
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    	}

    private void startSelectActivity(String requestCode) {       
        Bundle bundle = new Bundle();
        bundle.putString("gestures_char", requestCode);
        startFragment(this, GesturesExperienceGrid.class.getCanonicalName(),
                            0, -1 /* Do not request a results */, bundle);
    }

    private void setGesturesSummary(String gestures, MySwitchPreference mPre){
	String strSummary =null;
	int strID = 0;
	
	if(gestures == GESTURES_SETTINGS_C ){				

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_C, /*MODE_WORLD_READABLE*/1);
              String appName = pre.getString("app_name", "");  
		boolean Status = pre.getBoolean("type_status", false);
		mGesturesSettingsC.setChecked(Status);
			  
              if(appName.isEmpty()){
	           mGesturesSettingsC.setChecked(true);
                  saveSelectedAPP(GESTURES_SETTINGS_C, "com.android.dialer", "com.android.dialer.DialtactsActivity", getString(R.string.gestures_settings_defaut_C));
              }
		strID = R.string.gestures_settings_c;

	}else if(gestures == GESTURES_SETTINGS_E ){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_E, /*MODE_WORLD_READABLE*/1);
              String appName = pre.getString("app_name", "");
		boolean Status = pre.getBoolean("type_status", false);
		mGesturesSettingsE.setChecked(Status);
			  
              if(appName.isEmpty()){
		    mGesturesSettingsE.setChecked(true);	  	
                  saveSelectedAPP(GESTURES_SETTINGS_E, "com.android.browser", "com.android.browser.BrowserActivity", getString(R.string.gestures_settings_defaut_E));
              }
			  
		strID = R.string.gestures_settings_e;

	}else if(gestures == GESTURES_SETTINGS_M ){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_M, /*MODE_WORLD_READABLE*/1);
              String appName = pre.getString("app_name", "");
		boolean Status = pre.getBoolean("type_status", false);
		mGesturesSettingsM.setChecked(Status);
			  
              if(appName.isEmpty()){
		    mGesturesSettingsM.setChecked(true);	  	
                  saveSelectedAPP(GESTURES_SETTINGS_M, "com.android.email", "com.android.email.activity.Welcome", getString(R.string.gestures_settings_defaut_M));
              }
			  
		strID = R.string.gestures_settings_m;

	}else if(gestures ==GESTURES_SETTINGS_UP ){
		SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_UP, /*MODE_WORLD_READABLE*/1);
              String appName = pre.getString("app_name", "");  
		boolean Status = pre.getBoolean("type_status", false);
		mGesturesSettingsUp.setChecked(Status);			            

		strID = R.string.gestures_settings_up;

	}
	else if(gestures == GESTURES_SETTINGS_O ){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_O, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsO.setChecked(Status);

		strID = R.string.gestures_settings_o;

	}else if(gestures == GESTURES_SETTINGS_W){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_W, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsW.setChecked(Status);
              
		strID = R.string.gestures_settings_w;

	}else if(gestures == GESTURES_SETTINGS_DOWN){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_DOWN, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsDOWN.setChecked(Status);
              
		strID = R.string.gestures_settings_down;

	}else if(gestures == GESTURES_SETTINGS_S){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_S, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsS.setChecked(Status);
              
		strID = R.string.gestures_settings_s;

	}else if(gestures == GESTURES_SETTINGS_V){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_V, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsV.setChecked(Status);
              
		strID = R.string.gestures_settings_v;

	}else if(gestures == GESTURES_SETTINGS_Z){			

              SharedPreferences pre = getActivity().getSharedPreferences(GESTURES_SETTINGS_Z, /*MODE_WORLD_READABLE*/1);
              boolean Status = pre.getBoolean("type_status", false);
              mGesturesSettingsZ.setChecked(Status);
              
		strID = R.string.gestures_settings_z;

	}
			
	SharedPreferences pre = getActivity().getSharedPreferences(gestures, /*MODE_WORLD_READABLE*/1);
	String appName = pre.getString("app_name", "");
	System.out.println("appName="+appName);

	strSummary = getString(strID);
	if(appName.isEmpty()){	      
	    strSummary = strSummary + getString(R.string.gestures_settings_no_setting);
	}else{
		strSummary = strSummary + getString(R.string.gestures_settings_open) + "[ " + appName +" ]";
	}
		
	mPre.setSummary(strSummary);
    }
	
    public void saveSelectedAPP(String type, String pkg, String cls, String pName){
        SharedPreferences.Editor editor = getActivity().getSharedPreferences( type, /*MODE_WORLD_WRITEABLE*/2).edit();
        editor.putString("package_name", pkg);
        editor.putString("activity_name", cls);
        editor.putString("app_name", pName);
	 editor.putBoolean("type_status", true);		
        editor.commit();
    }

    /*
     * Creates toggles for each available location provider
     */
    private void updateToggles() {
    
	int status = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.GESTURE, -1);
	mGestureMode.setChecked(status>0 ? true : false);
       //gestures C	
	setGesturesSummary(GESTURES_SETTINGS_C, mGesturesSettingsC);

       //gestures E
       setGesturesSummary(GESTURES_SETTINGS_E, mGesturesSettingsE);
	   
       //gestures M	
       setGesturesSummary(GESTURES_SETTINGS_M, mGesturesSettingsM);	

	//gestures UP
        setGesturesSummary(GESTURES_SETTINGS_UP, mGesturesSettingsUp);

      //gestures O	
       setGesturesSummary(GESTURES_SETTINGS_O, mGesturesSettingsO);	

      //gestures W
       setGesturesSummary(GESTURES_SETTINGS_W, mGesturesSettingsW);	

      //gestures DOWN
       setGesturesSummary(GESTURES_SETTINGS_DOWN, mGesturesSettingsDOWN);	

      //gestures S
       setGesturesSummary(GESTURES_SETTINGS_S, mGesturesSettingsS);	

      //gestures V
       setGesturesSummary(GESTURES_SETTINGS_V, mGesturesSettingsV);	

      //gestures Z
       setGesturesSummary(GESTURES_SETTINGS_Z, mGesturesSettingsZ);	
}


    private IntentFilter   mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			 if(action.equals(TP_GESTURE_SWITCH_BROADCASE) && (mGestureMode != null)){
				int status = Settings.System.getInt(context.getContentResolver(), Settings.System.GESTURE, -1);
				mGestureMode.setChecked(status>0 ? true : false);
			}
        }
    };
	
    public void sendSwitchBroadcase(String strIntent, String strIntentVaule,boolean state){

	final Context mContext = getActivity();
        
        if (mContext == null) return;
        
        Intent intent = new Intent(strIntent); 
        intent.putExtra(strIntentVaule, state ? 1 : 0);
        mContext.sendBroadcast(intent);				
    }	

 private void createWarningDialog() {
 	
	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	builder.setMessage(getResources().getString(R.string.zzz_gesture_dialog_warnning)).
		setCancelable(false).
		setTitle(getResources().getString(R.string.zzz_gesture_dialog_title)).
		setPositiveButton(getResources().getString(R.string.zzz_gesture_dialog_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				 Settings.System.putInt(getActivity().getContentResolver(), Settings.System.GESTURE, 1); 			
			        sendSwitchBroadcase(TP_GESTURE_SWITCH_BROADCASE, "tp_gesture_switch_value" , true); 
				 mGestureMode.setChecked(true);				  
		}}).
		setNegativeButton(getResources().getString(R.string.zzz_gesture_dialog_cancel), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mGestureMode.setChecked(false);
			}
		}
	);

	AlertDialog alert = builder.create();
	alert.show();
     
    }	




	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.guesture_menu_main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_gestures_settings_reset: {
				Settings.System.putInt(getActivity().getContentResolver(), Settings.System.GESTURE, 0); 
				mGestureMode.setChecked(false);
				if(resetGesturesValue(GESTURES_SETTINGS_UP)){
					mGesturesSettingsUp.setChecked(true);
				}
				
				if(resetGesturesValue(GESTURES_SETTINGS_E)){
					mGesturesSettingsE.setChecked(true);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_M)){
					mGesturesSettingsM.setChecked(true);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_C)){
					mGesturesSettingsC.setChecked(true);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_O)){
					mGesturesSettingsO.setChecked(false);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_W)){
					mGesturesSettingsW.setChecked(false);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_DOWN)){
					mGesturesSettingsDOWN.setChecked(false);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_S)){
					mGesturesSettingsS.setChecked(false);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_V)){
					mGesturesSettingsV.setChecked(false);
				}

				if(resetGesturesValue(GESTURES_SETTINGS_Z)){
					mGesturesSettingsZ.setChecked(false);
				}
				
				return true;
			}
		}
		return false;
	}

	public boolean resetGesturesValue(String type){
	        SharedPreferences.Editor editor = getActivity().getSharedPreferences( type, /*MODE_WORLD_WRITEABLE*/2).edit();
	        editor.putString("package_name", null);
	        editor.putString("activity_name", null);
	        editor.putString("app_name", null);	

		 if(GESTURES_SETTINGS_UP == type){
			editor.putBoolean("type_status", true);
		 }else{
			editor.putBoolean("type_status", false);
		 }
	         
               sendSwitchBroadcase(TP_GESTURE_SWITCH_BROADCASE, "tp_gesture_switch_value" , false);
		  if(editor.commit()){
		  	updateToggles(); 
			return true;
		  }else{
		 	return false;
		  }
    }
}
	
