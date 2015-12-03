/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.R;
import com.mediatek.xlog.Xlog;
import java.util.ArrayList;

public class BatteryController extends BroadcastReceiver {
    private static final String TAG = "BatteryController";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final ArrayList<BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final PowerManager mPowerManager;

    private int mLevel;
    private boolean mPluggedIn;
    private boolean mCharging;
    private boolean mCharged;
    private boolean mPowerSave;

    public BatteryController(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        /// M: Support "battery percentage". caoqiaofeng add MTSFEFL-111 20150424
        mShouldShowBatteryPercentage = (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.BATTERY_PERCENTAGE, 0) != 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
        /// M: Support "battery percentage". caoqiaofeng add MTSFEFL-111 20150424
        filter.addAction(ACTION_BATTERY_PERCENTAGE_SWITCH);
        context.registerReceiver(this, filter);

        updatePowerSave();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("BatteryController state:");
        pw.print("  mLevel="); pw.println(mLevel);
        pw.print("  mPluggedIn="); pw.println(mPluggedIn);
        pw.print("  mCharging="); pw.println(mCharging);
        pw.print("  mCharged="); pw.println(mCharged);
        pw.print("  mPowerSave="); pw.println(mPowerSave);
    }

    public void addStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
        cb.onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
    }

    public void removeStateChangedCallback(BatteryStateChangeCallback cb) {
        mChangeCallbacks.remove(cb);
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            mLevel = (int)(100f
                    * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
            mPluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

            final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            mCharged = status == BatteryManager.BATTERY_STATUS_FULL;
            mCharging = mCharged || status == BatteryManager.BATTERY_STATUS_CHARGING;

            //caoqiaofeng add MTSFEFL-111 20150424 start
            /// M: Support "battery percentage". @{ 
            mBatteryPercentage = getBatteryPercentage(intent);
            Xlog.d(TAG,"mBatteryPercentage is " + mBatteryPercentage + " mShouldShowBatteryPercentage is "
                    + mShouldShowBatteryPercentage + " mLabelViews.size() " + mLabelViews.size());
            refreshBatteryPercentage();
            /// M: Support "battery percentage". @}
            //caoqiaofeng add MTSFEFL-111 20150424 end            
            fireBatteryLevelChanged();
        } else if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)) {
            updatePowerSave();
        } else if (action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING)) {
            setPowerSave(intent.getBooleanExtra(PowerManager.EXTRA_POWER_SAVE_MODE, false));
        }
        //caoqiaofeng add MTSFEFL-111 20150424 start		
        /// M: Support "battery percentage". @{ 
        else if (action.equals(ACTION_BATTERY_PERCENTAGE_SWITCH)) {
            mShouldShowBatteryPercentage = (intent.getIntExtra("state",0) == 1);
            Xlog.d(TAG, " OnReceive from mediatek.intent.ACTION_BATTERY_PERCENTAGE_SWITCH  mShouldShowBatteryPercentage" +
                    " is " + mShouldShowBatteryPercentage);
            refreshBatteryPercentage();
        } else if (Intent.ACTION_USER_SWITCHED.equals(action)) {
            mShouldShowBatteryPercentage = (Settings.Secure.getIntForUser(context
                    .getContentResolver(), Settings.Secure.BATTERY_PERCENTAGE, 0, ActivityManager.getCurrentUser()) != 0);
            Xlog.d(TAG, "ACTION_USER_SWITCHED mShouldShowBatteryPercentage is "
                    + mShouldShowBatteryPercentage + " ActivityManager.getCurrentUser() is " + ActivityManager.getCurrentUser());
            refreshBatteryPercentage();
        }
        /// @}
       //caoqiaofeng add MTSFEFL-111 20150424 end        
    }

    //caoqiaofeng add MTSFEFL-111 20150424 start
    /// M: Support "Battery Percentage Switch" 
    private static final String ACTION_BATTERY_PERCENTAGE_SWITCH = "mediatek.intent.action.BATTERY_PERCENTAGE_SWITCH";
    /// M: Support "battery percentage". @{
    private boolean mShouldShowBatteryPercentage = false;
    private String mBatteryPercentage = "100%";
    /// @}

    private Context mContext;
    private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();

    public void addLabelView(TextView v) {
        mLabelViews.add(v);
    }

    /// M: Support "battery percentage". @{ 
    private  String getBatteryPercentage(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra("level", 0);
        int scale = batteryChangedIntent.getIntExtra("scale", 100);
        return String.valueOf(level * 100 / scale) + "%";
    }
    /// @}
    /// M: Support "battery percentage". @{
    private void refreshBatteryPercentage() {

        Log.i(TAG, "refreshBatteryPercentage mLabelViews.size() ="+ mLabelViews.size() );
		
        if (mLabelViews.size() > 0) {
            TextView v = mLabelViews.get(0);
			Log.i(TAG, "refreshBatteryPercentage v ="+ v );
            if (v != null) {
		  Log.i(TAG, "refreshBatteryPercentage mShouldShowBatteryPercentage="+mShouldShowBatteryPercentage);   		
                if (mShouldShowBatteryPercentage) {
                    v.setText(mBatteryPercentage);
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.GONE);
                }
            }
        }
    }
    /// @}
    //caoqiaofeng add MTSFEFL-111 20150424 end
    public boolean isPowerSave() {
        return mPowerSave;
    }

    private void updatePowerSave() {
        setPowerSave(mPowerManager.isPowerSaveMode());
    }

    private void setPowerSave(boolean powerSave) {
        if (powerSave == mPowerSave) return;
        mPowerSave = powerSave;
        if (DEBUG) Log.d(TAG, "Power save is " + (mPowerSave ? "on" : "off"));
        firePowerSaveChanged();
    }

    private void fireBatteryLevelChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
        }
    }

    private void firePowerSaveChanged() {
        final int N = mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            mChangeCallbacks.get(i).onPowerSaveChanged();
        }
    }

    public interface BatteryStateChangeCallback {
        void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging);
        void onPowerSaveChanged();
    }
}
