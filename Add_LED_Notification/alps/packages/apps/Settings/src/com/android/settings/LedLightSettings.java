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
import android.preference.SwitchPreference;

//caoqiaofeng add SFZT-148 20141017
public class LedLightSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{
   
    private static final String LED_LIGHT_SETTIING = "led_light_setting";
    private SwitchPreference mLedLightSettingsLowPowerRemind;
    private SwitchPreference mLedLightSettingsChargingRemind;
    private SwitchPreference mLedLightSettingsNotificationRemind;	
    
    public static final String LED_LIGHT_SETTINGS_LOW_POWER_REMIND = "led_light_settings_low_power_remind";
    public static final String LED_LIGHT_SETTINGS_CHARGING_REMIND = "led_light_settings_charging_remind";
    public static final String LED_LIGHT_SETTINGS_NOTIFICATION_REMIND = "led_light_settings_notification_remind";  
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.led_light_settings);
        final PreferenceScreen screen = getPreferenceScreen();
        			
        mLedLightSettingsLowPowerRemind = (SwitchPreference) screen.findPreference(LED_LIGHT_SETTINGS_LOW_POWER_REMIND);
	 mLedLightSettingsLowPowerRemind.setOnPreferenceChangeListener(this);	
        mLedLightSettingsChargingRemind = (SwitchPreference) screen.findPreference(LED_LIGHT_SETTINGS_CHARGING_REMIND);
	 mLedLightSettingsChargingRemind.setOnPreferenceChangeListener(this);	
        mLedLightSettingsNotificationRemind = (SwitchPreference) screen.findPreference(LED_LIGHT_SETTINGS_NOTIFICATION_REMIND);
	 mLedLightSettingsNotificationRemind.setOnPreferenceChangeListener(this);	        
        
    }
    
    @Override
    public void onResume() {
        super.onResume(); 
	 init();	
    }

    public void init(){
		boolean status ;
		status = (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.LOW_POWER_REMIND_STATUS, -1) > 0) ? true: false;
		mLedLightSettingsLowPowerRemind.setChecked(status);
		
		status = (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.CHARGING_REMIND_STATUS, -1) > 0) ? true: false;
		mLedLightSettingsChargingRemind.setChecked(status) ;

		status = (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.NOTIFICATION_REMIND_STATUS, -1) > 0) ? true: false;
	       mLedLightSettingsNotificationRemind.setChecked(status); 		
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	 boolean Status = false;
	 	
        if(preference == mLedLightSettingsChargingRemind){

		 if(((SwitchPreference)preference).isChecked()) {		 	
			Status = false;
			
		 }else{
			Status = true;
		 }
		 
		Settings.System.putInt(getActivity().getContentResolver(), Settings.System.CHARGING_REMIND_STATUS, Status?1:0); 
		 
        }else if(preference == mLedLightSettingsLowPowerRemind){

	      if(((SwitchPreference)preference).isChecked()) {
			Status = false;
			
		 }else{
			Status = true;
		 }	

		 Settings.System.putInt(getActivity().getContentResolver(), Settings.System.LOW_POWER_REMIND_STATUS, Status?1:0); 
		  
        }else if(preference == mLedLightSettingsNotificationRemind){

	      if(((SwitchPreference)preference).isChecked()) {
			Status = false;
			
		 }else{
			Status = true;
		 }		

		 Settings.System.putInt(getActivity().getContentResolver(), Settings.System.NOTIFICATION_REMIND_STATUS, Status?1:0); 
        }

        return true;
    }
}
	
