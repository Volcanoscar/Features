/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.app.AppOpsManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.database.ContentObserver;

import com.android.systemui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A controller to manage changes of powersaving related states and update the views accordingly.
 */
public class PowersavingControllerImpl implements PowersavingController {
    public static final String POWERSAVING_STATUS_ICON_PLACEHOLDER = "powersaving";
    public static final int POWERSAVING_STATUS_ICON_ID = R.drawable.stat_sys_location;

    private static final int[] mHighPowerRequestAppOpArray
        = new int[] {AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    private Context mContext;

    private AppOpsManager mAppOpsManager;
    private StatusBarManager mStatusBarManager;

    private PowerSavingLogic mPowerSavingLogic;

    private ArrayList<PowersavingSettingsChangeCallback> mSettingsChangeCallbacks =
            new ArrayList<PowersavingSettingsChangeCallback>();            

      private ContentObserver mPowerSavingChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {   
                powersavingSettingsChanged();
        }
    };   
    
    public PowersavingControllerImpl(Context context) {
        mContext = context;

        mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mStatusBarManager
                = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);


//        mContext.getContentResolver().unregisterContentObserver(mPowerSavingChangeObserver); //for power saving shuyong 20141124 
        mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.POWERSAVING), 
                        true, mPowerSavingChangeObserver);
        mPowerSavingLogic = new PowerSavingLogic(mContext);
        mPowerSavingLogic.init();

       // refreshViews();
    }

    /**
     * Add a callback to listen for changes in powersaving settings.
     */
    public void addSettingsChangedCallback(PowersavingSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.add(cb);
        cb.onPowersavingSettingsChanged(isPowersavingEnabled());
    }

    public void removeSettingsChangedCallback(PowersavingSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.remove(cb);
    }

    /**
     * Enable or disable powersaving in settings.
     *
     * <p>This will attempt to enable/disable every type of powersaving setting
     * (e.g. high and balanced power).
     *
     * <p>If enabling, a user consent dialog will pop up prompting the user to accept.
     * If the user doesn't accept, network powersaving won't be enabled.
     *
     * @return true if attempt to change setting was successful.
     */
    public boolean setPowersavingEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();
        return Settings.System.putInt(cr, Settings.System.POWERSAVING, enabled ? 1 : 0);
    }

    /**
     * Returns true if powersaving isn't disabled in settings.
     */
    public boolean isPowersavingEnabled() {
        ContentResolver resolver = mContext.getContentResolver();
        int state = Settings.System.getInt(resolver, Settings.System.POWERSAVING, 0);    
        if (state == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the current user is restricted from using powersaving.
     */
    private boolean isUserPowersavingRestricted(int userId) {
        return true;
//        final UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
//        return um.hasUserRestriction(
//                UserManager.DISALLOW_SHARE_LOCATION,
//                new UserHandle(userId));
    }


    // Updates the status view based on the current state of powersaving requests.
    private void refreshViews() {
        if (isPowersavingEnabled()) {
            mStatusBarManager.setIcon(POWERSAVING_STATUS_ICON_PLACEHOLDER, POWERSAVING_STATUS_ICON_ID, 0,
                    mContext.getString(R.string.accessibility_powersaving_active));
        } else {
            mStatusBarManager.removeIcon(POWERSAVING_STATUS_ICON_PLACEHOLDER);
        }
    }

    private void powersavingSettingsChanged() {
        boolean isEnabled = isPowersavingEnabled();
        for (PowersavingSettingsChangeCallback cb : mSettingsChangeCallbacks) {
            cb.onPowersavingSettingsChanged(isEnabled);
        }
    }
}
