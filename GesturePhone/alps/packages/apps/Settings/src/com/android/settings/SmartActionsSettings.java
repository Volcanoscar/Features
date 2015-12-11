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

import android.provider.Settings;

import java.io.FileOutputStream; //zhaoxy add 150202  for SFZT-138:not screen off when incall talking with smartcover closed 
public class SmartActionsSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{// implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{

    private SwitchPreference mSmartAction;

    private SwitchPreference mFlipToSilence;
    private SwitchPreference mFlipToSpeaker;	
    private SwitchPreference mFlipToSnoozeAlarm;
    private SwitchPreference mDecreaseRingOnMove;
    private SwitchPreference mQuickLaunchCamera;	
    private SwitchPreference mQuickLaunchPhone;	
    
    public static final String SMART_ACTION = "smart_action";
    public static final String FLIP_TO_SILENCE = "flip_to_silence";
    public static final String FLIP_TO_SPEAKER = "flip_to_speaker";
    public static final String FLIP_TO_SNOOZE_ALARM = "flip_to_snooze_alarm";
    public static final String DECREASE_RING_ON_MOVE = "decrease_ring_on_move";
    public static final String QUICK_LAUNCH_CAMERA = "quick_launch_camera";
    public static final String QUICK_LAUNCH_PHONE = "quick_launch_phone";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("aoran","Action-onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_actions_settings);

		//init screen
		initializeAllPreferences();

		//init preference
		//setPreferenceEnable();
    }

		private void initializeAllPreferences() {

        final PreferenceScreen screen = getPreferenceScreen();
		
        mSmartAction = (SwitchPreference) screen.findPreference(SMART_ACTION);
		if (mSmartAction != null) {
			mSmartAction.setOnPreferenceChangeListener(this);
		}
				
		mFlipToSilence = (SwitchPreference) screen.findPreference(FLIP_TO_SILENCE);
		mFlipToSilence.setOnPreferenceChangeListener(this);
		
		mFlipToSpeaker = (SwitchPreference) screen.findPreference(FLIP_TO_SPEAKER);
		mFlipToSpeaker.setOnPreferenceChangeListener(this);
		
		mFlipToSnoozeAlarm = (SwitchPreference) screen.findPreference(FLIP_TO_SNOOZE_ALARM);
		mFlipToSnoozeAlarm.setOnPreferenceChangeListener(this);	  
		
		mDecreaseRingOnMove = (SwitchPreference) screen.findPreference(DECREASE_RING_ON_MOVE);
		mDecreaseRingOnMove.setOnPreferenceChangeListener(this);
		
		mQuickLaunchCamera = (SwitchPreference) screen.findPreference(QUICK_LAUNCH_CAMERA);
		mQuickLaunchCamera.setOnPreferenceChangeListener(this);
		
		mQuickLaunchPhone = (SwitchPreference) screen.findPreference(QUICK_LAUNCH_PHONE);
		mQuickLaunchPhone.setOnPreferenceChangeListener(this);

	}


		private void setPreferenceEnable() {
			final boolean smartActionEnabled = Settings.System.getInt(
					getContentResolver(),
					Settings.System.DEF_SMART_ACTION, 0) == 1;
			Log.d("aoran","smartActionEnabled="+smartActionEnabled);
			if (smartActionEnabled) {
				mFlipToSilence.setEnabled(true);
				mFlipToSpeaker.setEnabled(true);
				mFlipToSnoozeAlarm.setEnabled(true);
				mDecreaseRingOnMove.setEnabled(true);
				mQuickLaunchCamera.setEnabled(true);
				mQuickLaunchPhone.setEnabled(true);
			} else {
				mFlipToSilence.setEnabled(false);
				mFlipToSpeaker.setEnabled(false);
				mFlipToSnoozeAlarm.setEnabled(false);
				mDecreaseRingOnMove.setEnabled(false);
				mQuickLaunchCamera.setEnabled(false);
				mQuickLaunchPhone.setEnabled(false);
			}
		}
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Refresh UI
        updateToggles();
		//setPreferenceEnable();
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub

	    //updateToggles();
		return true;
	}

    @Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			  Log.d("aoran", "qiao onPreferenceTreeClick ");	 
			//updateToggles();
			if (mSmartAction== preference) {
				handleSmartActionSwitchClick(
						Settings.System.DEF_SMART_ACTION,
						mSmartAction.isChecked());
				//return true;
			} else if (mFlipToSilence == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_FLIP_TO_SILENCE,
						mFlipToSilence.isChecked());
				//return true;
			} else if (mFlipToSpeaker == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_FLIP_TO_SPEAKER,
						mFlipToSpeaker.isChecked());
				//return true;
			} else if (mFlipToSnoozeAlarm == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_FLIP_TO_SNOOZE_ALARM,
						mFlipToSnoozeAlarm.isChecked());
				//return true;
			} else if (mDecreaseRingOnMove == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_DECREASE_RING_ON_MOVE,
						mDecreaseRingOnMove.isChecked());
				//return true;
			} else if (mQuickLaunchCamera == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_QUICK_LAUNCH_CAMERA,
						mQuickLaunchCamera.isChecked());
				//return true;
			} else if (mQuickLaunchPhone == preference) {
				handleGestureSetttingsPreferenceClick(
						Settings.Secure.DEF_QUICK_LAUNCH_PHONE,
						mQuickLaunchPhone.isChecked());
				//return true;
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
	
		/*
		 * Creates toggles for each available location provider
		 */
		private void updateToggles() {

		final int toggle_smart_action_value = Settings.System.getInt(
						getContentResolver(),Settings.System.DEF_SMART_ACTION, 0);
		mSmartAction.setChecked(toggle_smart_action_value == 1);

		
		final int toggle_flip_to_silence_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_FLIP_TO_SILENCE, 0);
		mFlipToSilence.setChecked(toggle_flip_to_silence_value == 1);
		
		final int toggle_flip_to_speaker_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_FLIP_TO_SPEAKER, 0);
		mFlipToSpeaker.setChecked(toggle_flip_to_speaker_value == 1);
		
		final int toggle_flip_to_snooze_alarm_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_FLIP_TO_SNOOZE_ALARM, 0);
		mFlipToSnoozeAlarm.setChecked(toggle_flip_to_snooze_alarm_value == 1);

		final int toggle_decrease_ring_on_move_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_DECREASE_RING_ON_MOVE, 0);
		mDecreaseRingOnMove.setChecked(toggle_decrease_ring_on_move_value == 1);
		
		final int toggle_quick_launch_camera_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_QUICK_LAUNCH_CAMERA, 0);
		mQuickLaunchCamera.setChecked(toggle_quick_launch_camera_value == 1);
		
		final int toggle_quick_launch_phone_value = Settings.Secure.getInt(
						getContentResolver(),Settings.Secure.DEF_QUICK_LAUNCH_PHONE, 0);
		mQuickLaunchPhone.setChecked(toggle_quick_launch_phone_value == 1);

		Log.d("aoran","toggle_flip_to_silence_value="+toggle_flip_to_silence_value);
		Log.d("aoran","toggle_flip_to_speaker_value="+toggle_flip_to_speaker_value);
		Log.d("aoran","toggle_flip_to_snooze_alarm_value="+toggle_flip_to_snooze_alarm_value);
		Log.d("aoran","toggle_decrease_ring_on_move_value="+toggle_decrease_ring_on_move_value);
		Log.d("aoran","toggle_quick_launch_camera_value="+toggle_quick_launch_camera_value);
		Log.d("aoran","toggle_quick_launch_camera_value="+toggle_quick_launch_phone_value);
		
	}

	private void handleGestureSetttingsPreferenceClick(String settings_key,
			boolean on) {
		Log.v("aoran", "@@@handleGestureSetttingsPreferenceClick," + on);
		Settings.Secure
				.putInt(getContentResolver(), settings_key, (on ? 1 : 0));
		Log.d("aoran","click="+Settings.Secure.getInt(
						getContentResolver(),settings_key, 0));
	}

	private void handleSmartActionSwitchClick(String settings_key,
			boolean on) {
		Log.v("aoran", "@@@handleSmartActionSwitchClick," + on);
		Settings.System
				.putInt(getContentResolver(), settings_key, (on ? 1 : 0));
		Log.d("aoran","click="+Settings.System.getInt(
						getContentResolver(),settings_key, 0));
	}

}
	
