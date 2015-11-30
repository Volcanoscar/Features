package com.android.settings;


import com.android.settings.R;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

// laiyang add for FloatMultiTask 20150710
public class MultiTaskSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener{

	private SwitchPreference mMultiTaskMode;
	private SwitchPreference mMultiTaskAutoShow;

	public static final String MULTI_TASK_MODE = "multi_task_mode";
	public static final String MULTI_TASK_SMS_AUTO_SHOW = "multi_task_sms_auto_show";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.floating_multi_task_settings);
		final PreferenceScreen screen = getPreferenceScreen();

		mMultiTaskMode = (SwitchPreference) screen.findPreference(MULTI_TASK_MODE);
		mMultiTaskMode.setOnPreferenceChangeListener(this);
		mMultiTaskAutoShow = (SwitchPreference) screen.findPreference(MULTI_TASK_SMS_AUTO_SHOW);
		mMultiTaskAutoShow.setOnPreferenceChangeListener(this);
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		boolean isSwitched = false;
		String multiTaskType = null;

		if(preference == mMultiTaskMode) {
			multiTaskType = MULTI_TASK_MODE;
			if(((SwitchPreference) preference).isChecked()) {
				isSwitched = false;
				Intent intent = new Intent("com.malata.floatmultitask.action.close");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			//	intent.putExtra("value", 1);
				getActivity().sendBroadcast(intent);
			} else {
			Log.d("haha", "into send broadcast");
				isSwitched = true;
				Intent intent = new Intent("com.malata.floatmultitask.action.changestatus");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				intent.putExtra("value", 1);
				getActivity().sendBroadcast(intent);
			}
		} else if(preference == mMultiTaskAutoShow) {
			if(((SwitchPreference) preference).isChecked()) {
				Intent intent = new Intent("com.malata.floatmultitask.action.disableautoshowsms");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				getActivity().sendBroadcast(intent);
				isSwitched = false;
			} else {
				isSwitched = true;
				Intent intent = new Intent("com.malata.floatmultitask.action.enableautoshowsms");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				getActivity().sendBroadcast(intent);
			}
		}
		return true;
	}
}
