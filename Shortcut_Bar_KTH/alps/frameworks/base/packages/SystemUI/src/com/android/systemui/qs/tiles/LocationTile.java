/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.qs.tiles;

import android.util.Log;
import android.content.Context;
import android.content.Intent;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Toast;

/** Quick settings tile: Location **/
public class LocationTile extends QSTile<QSTile.BooleanState> {

    private final AnimationIcon mEnable =
            new AnimationIcon(R.drawable.ic_signal_location_enable_animation);
    private final AnimationIcon mDisable =
            new AnimationIcon(R.drawable.ic_signal_location_disable_animation);

    private final LocationController mController;
    private final KeyguardMonitor mKeyguard;

    private final Callback mCallback = new Callback();
	
	// kth add for LFZSF-10  shorcut bar test at 20150820 start
	private static final String CONTROLACTION_LOCATION = "com.android.test.controller.location";
	private static final String CONTROLACTION_LOCATION_UPDATE_STATE = "com.android.test.controller.location.updatestate";
	Intent updateIntent = new Intent();
	private static final String TAG_KTH = "ShortcutBar_kth";
	private static int TILE_STATE = 0;
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		Log.i(TAG_KTH, "LocationTile location received = "+ intent.getAction());
			if (intent.getAction().equals(CONTROLACTION_LOCATION)) {		
				click();
				refreshState();				
			}
		}
	};
	// kth add for LFZSF-10  shorcut bar test at 20150820 end

    public LocationTile(Host host) {
        super(host);
		// kth add for  LFZSF-10 shorcut bar test at 20150820 start
		IntentFilter control_filter = new IntentFilter();
		control_filter.addAction(CONTROLACTION_LOCATION);
		mContext.registerReceiver(controlReceiver, control_filter);
		// kth add for  LFZSF-10 shorcut bar test at 20150820 end
        mController = host.getLocationController();
        mKeyguard = host.getKeyguardMonitor();
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addSettingsChangedCallback(mCallback);
            mKeyguard.addCallback(mCallback);
        } else {
            mController.removeSettingsChangedCallback(mCallback);
            mKeyguard.removeCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
        final boolean wasEnabled = (Boolean) mState.value;
        mController.setLocationEnabled(!wasEnabled);
        mEnable.setAllowAnimation(true);
        mDisable.setAllowAnimation(true);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean locationEnabled =  mController.isLocationEnabled();

        // Work around for bug 15916487: don't show location tile on top of lock screen. After the
        // bug is fixed, this should be reverted to only hiding it on secure lock screens:
        // state.visible = !(mKeyguard.isSecure() && mKeyguard.isShowing());
        state.visible = !mKeyguard.isShowing();
        state.value = locationEnabled;
        if (locationEnabled) {
            state.icon = mEnable;
            state.label = mContext.getString(R.string.quick_settings_location_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_on);
        } else {
            state.icon = mDisable;
            state.label = mContext.getString(R.string.quick_settings_location_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_off);
        }
		// kth add for LFZSF-10  shorcut bar test at 20150820 start
		updateIntent.setAction(CONTROLACTION_LOCATION_UPDATE_STATE);
		TILE_STATE = state.value ? 1 : 0;
		updateIntent.putExtra("msg", TILE_STATE);
		mContext.sendBroadcast(updateIntent);
		// kth add for LFZSF-10  shorcut bar test at 20150820 end
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_location_changed_off);
        }
    }

    private final class Callback implements LocationSettingsChangeCallback,
            KeyguardMonitor.Callback {
        @Override
        public void onLocationSettingsChanged(boolean enabled) {
            refreshState();
        }

        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };
}
