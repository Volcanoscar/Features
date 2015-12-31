package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.provider.Settings.System;
import android.util.Log;

public class PowerSavingSettings extends SettingsPreferenceFragment
  implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener
{
  private static final String KEY_POWER_SAVING = "power_saving";
  private static final String KEY_POWER_SAVING_BRIGHTNESS = "power_saving_screenlight";
  private static final String KEY_POWER_SAVING_BT = "power_saving_bt";
  private static final String KEY_POWER_SAVING_DATACONN = "power_saving_dataconn";
  private static final String KEY_POWER_SAVING_GPS = "power_saving_gps";
  private static final String KEY_POWER_SAVING_PERCENT = "power_saving_percent";
  private static final String KEY_POWER_SAVING_WIFI = "power_saving_wifi";
  private static final String TAG = "PowerSavingSettings";
  private CheckBoxPreference mCheckBoxBRIGHTNESS;
  private CheckBoxPreference mCheckBoxBT;
  private CheckBoxPreference mCheckBoxDATACONN;
  private CheckBoxPreference mCheckBoxGPS;
  private CheckBoxPreference mCheckBoxWIFI;
  private ListPreference mPowerSavingValuePreference;
  private Preference mPrefPowerSaving;

  private int getPowerSavingValue()
  {
    int percent = System.getInt(getActivity().getContentResolver(), "power_saving_value", 3);
    Log.d("PowerSavingSettings", "getTimoutValue()---currentValue=" + percent);
    return percent;
  }

public void init()
    {
        //select battery percent value
        int percent = getPowerSavingValue();        
        mPowerSavingValuePreference = (ListPreference)findPreference(KEY_POWER_SAVING_PERCENT);
        mPowerSavingValuePreference.setValue(String.valueOf(percent));
        mPowerSavingValuePreference.setOnPreferenceChangeListener(this);
        updatePercentValue(percent);

        //enable or disable power saving
        boolean bEnabled = System.getInt(getContentResolver(), KEY_POWER_SAVING, 0) != 0;
        mPrefPowerSaving = findPreference(KEY_POWER_SAVING);
        mPrefPowerSaving.setOnPreferenceChangeListener(this);        
        SwitchPreference switchpreference = (SwitchPreference)mPrefPowerSaving;
        switchpreference.setChecked(bEnabled);
  
        
        mCheckBoxWIFI = (CheckBoxPreference)findPreference(KEY_POWER_SAVING_WIFI);
        mCheckBoxWIFI.setOnPreferenceChangeListener(this);
        mCheckBoxWIFI.setChecked(System.getInt(getContentResolver(), KEY_POWER_SAVING_WIFI, 0) != 0);
        
        mCheckBoxBT = (CheckBoxPreference)findPreference(KEY_POWER_SAVING_BT);
        mCheckBoxBT.setOnPreferenceChangeListener(this);
        mCheckBoxBT.setChecked(System.getInt(getContentResolver(), KEY_POWER_SAVING_BT, 0) != 0);
        
        
        mCheckBoxDATACONN = (CheckBoxPreference)findPreference(KEY_POWER_SAVING_DATACONN);
        mCheckBoxDATACONN.setOnPreferenceChangeListener(this);
        mCheckBoxDATACONN.setChecked(System.getInt(getContentResolver(), KEY_POWER_SAVING_DATACONN, 0) != 0);
        
        mCheckBoxGPS = (CheckBoxPreference)findPreference(KEY_POWER_SAVING_GPS);
        mCheckBoxGPS.setOnPreferenceChangeListener(this);
        mCheckBoxGPS.setChecked(System.getInt(getContentResolver(), KEY_POWER_SAVING_GPS, 0) != 0);
        
        mCheckBoxBRIGHTNESS = (CheckBoxPreference)findPreference(KEY_POWER_SAVING_BRIGHTNESS);
        mCheckBoxBRIGHTNESS.setOnPreferenceChangeListener(this);    
        mCheckBoxBRIGHTNESS.setChecked(System.getInt(getContentResolver(), KEY_POWER_SAVING_BRIGHTNESS, 0) != 0);

        mCheckBoxWIFI.setEnabled(bEnabled);
        mCheckBoxBT.setEnabled(bEnabled);
        mCheckBoxDATACONN.setEnabled(bEnabled);
        mCheckBoxGPS.setEnabled(bEnabled);
        mCheckBoxBRIGHTNESS.setEnabled(bEnabled);  

    }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.gouwei_power_saving);
    init();
  }
   public boolean onPreferenceChange(Preference preference, Object obj)
    {      
        if(KEY_POWER_SAVING_PERCENT.equals(preference.getKey()))
        {
            int value = Integer.parseInt((String)obj);      
            try
            {
                System.putInt(getContentResolver(), "power_saving_value", value);
                updatePercentValue(value);               
            }
            catch(NumberFormatException numberformatexception)
            {
                Log.e("PowerSavingSettings", "could not persist screen timeout setting", numberformatexception);
            }
        }
        else if(KEY_POWER_SAVING.equals(preference.getKey())) {
            boolean bEnable = !((SwitchPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING, bEnable ? 1 : 0);
            System.putInt(getContentResolver(), "power_saving_flag", bEnable ? 1 : 0);

            mCheckBoxWIFI.setEnabled(bEnable);
            mCheckBoxBT.setEnabled(bEnable);
            mCheckBoxDATACONN.setEnabled(bEnable);
            mCheckBoxGPS.setEnabled(bEnable);
            mCheckBoxBRIGHTNESS.setEnabled(bEnable);            

            if(bEnable){                
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.title_power_saving_open);


                final String str = preference.getContext().getString(R.string.power_saving_message, mPowerSavingValuePreference.getEntry());
                            
                builder.setMessage(str);                
                builder.setPositiveButton(R.string.power_saving_open_yes, null);
                builder.show();
            }
           
        }
        
        //not enable power saving
//        if(System.getInt(getContentResolver(), KEY_POWER_SAVING, 0) != 1)
//            return false;
        
        else if(KEY_POWER_SAVING_WIFI.equals(preference.getKey()))
        {
            boolean bChecked = !((CheckBoxPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING_WIFI, bChecked ? 1 : 0);
        }
        else if(KEY_POWER_SAVING_BT.equals(preference.getKey()))
        {
            boolean bChecked = !((CheckBoxPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING_BT, bChecked ?   1 : 0);
        }
        else if(KEY_POWER_SAVING_DATACONN.equals(preference.getKey()))
        {
            boolean bChecked = !((CheckBoxPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING_DATACONN, bChecked ?    1 : 0);
        }
        else if(KEY_POWER_SAVING_GPS.equals(preference.getKey()))
        {
            boolean bChecked = !((CheckBoxPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING_GPS, bChecked ?    1 : 0);
        }
        else if(KEY_POWER_SAVING_BRIGHTNESS.equals(preference.getKey()))
        {
            boolean bChecked = !((CheckBoxPreference)preference).isChecked();
            System.putInt(getContentResolver(), KEY_POWER_SAVING_BRIGHTNESS, bChecked ?   1 : 0);
        }else{
            return false;
        }
        return true;
           
    }

  public boolean onPreferenceClick(Preference paramPreference)
  {
    return false;
  }

      private void updatePercentValue(int currentValue) {
        ListPreference preference = mPowerSavingValuePreference;
        String summary;
        if (currentValue < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    int value = Integer.parseInt(values[i].toString());
                    if (currentValue == value) {
                        best = i;
                    }
                }
            
                if (entries.length != 0) {                    
                    summary = preference.getContext().getString(
                            R.string.power_saving_value_summary, entries[best]);
                } else {
                    summary = "";
                }    
            }
        }
        Log.e("PowerSavingSettings", "summary=" + summary);
        preference.setSummary(summary);
    }
}