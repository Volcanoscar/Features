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
import android.location.LocationManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.database.ContentObserver;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy; 
//caoqiaofeng add MTSFEFL-14 20150323

/**
 * A controller to manage changes of location related states and update the views accordingly.
 */
public class GestureControllerImpl implements GestureController {
    // The name of the placeholder corresponding to the location request status icon.
    // This string corresponds to config_statusBarIcons in core/res/res/values/config.xml.
    public static final String GESTURE_STATUS_ICON_PLACEHOLDER = "gesture";
    public static final int GESTURE_STATUS_ICON_ID = R.drawable.stat_sys_location;

    private static final int[] mHighPowerRequestAppOpArray
        = new int[] {AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION};

    private Context mContext;

    private AppOpsManager mAppOpsManager;
    private StatusBarManager mStatusBarManager;

    private ArrayList<GestureSettingsChangeCallback> mSettingsChangeCallbacks =
            new ArrayList<GestureSettingsChangeCallback>();
      private ContentObserver mGestureChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {   
                gestureSettingsChanged();
        }
    };   
    public GestureControllerImpl(Context context) {
        mContext = context;

        mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mStatusBarManager
                = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);

        mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(Settings.System.GESTURE), 
                        true, mGestureChangeObserver);
    }
    /**
     * Add a callback to listen for changes in location settings.
     */
    public void addSettingsChangedCallback(GestureSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.add(cb);
        cb.onGestureSettingsChanged(isGestureEnabled());
    }

    public void removeSettingsChangedCallback(GestureSettingsChangeCallback cb) {
        mSettingsChangeCallbacks.remove(cb);
    }

    /**
     * Enable or disable location in settings.
     *
     * <p>This will attempt to enable/disable every type of location setting
     * (e.g. high and balanced power).
     *
     * <p>If enabling, a user consent dialog will pop up prompting the user to accept.
     * If the user doesn't accept, network location won't be enabled.
     *
     * @return true if attempt to change setting was successful.
     */
    public boolean setGestureEnabled(boolean enabled) {
        final ContentResolver cr = mContext.getContentResolver();

        boolean flag = Settings.System.putInt(cr, Settings.System.GESTURE, enabled ? 1 : 0);  //20150330

	 if(flag){	
            Intent intent = new Intent(PhoneStatusBarPolicy.TP_GESTURE_SWITCH_BROADCASE);
            intent.putExtra("tp_gesture_switch_value", enabled ? 1 : 0);
            mContext.sendBroadcast(intent);
	 }
	 
        return flag;
    }

    /**
     * Returns true if location isn't disabled in settings.
     */
    public boolean isGestureEnabled() {
        ContentResolver resolver = mContext.getContentResolver();
        int state = Settings.System.getInt(resolver, Settings.System.GESTURE, 0);    
        if (state == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the current user is restricted from using location.
     */
    private boolean isUserGestureRestricted(int userId) {
        return true;
    }

    // Updates the status view based on the current state of gesture requests.
    private void refreshViews() {
        if (isGestureEnabled()) {
            mStatusBarManager.setIcon(GESTURE_STATUS_ICON_PLACEHOLDER, GESTURE_STATUS_ICON_ID, 0,
                    mContext.getString(R.string.quick_settings_gesture_label));
        } else {
            mStatusBarManager.removeIcon(GESTURE_STATUS_ICON_PLACEHOLDER);
        }
    }


    private void gestureSettingsChanged() {
        boolean isEnabled = isGestureEnabled();
        for (GestureSettingsChangeCallback cb : mSettingsChangeCallbacks) {
            cb.onGestureSettingsChanged(isEnabled);
        }
    }

}
