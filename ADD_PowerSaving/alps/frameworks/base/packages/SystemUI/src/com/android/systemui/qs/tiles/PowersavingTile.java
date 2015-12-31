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

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.PowersavingController;
import com.android.systemui.statusbar.policy.PowersavingController.PowersavingSettingsChangeCallback;

/** Quick settings tile: Powersaving **/
public class PowersavingTile extends QSTile<QSTile.BooleanState> {
	
	  private final AnimationIcon mEnable =
            new AnimationIcon(R.drawable.ic_signal_location_enable_animation);
    private final AnimationIcon mDisable =
            new AnimationIcon(R.drawable.ic_signal_location_disable_animation);

    private final PowersavingController mController;
    private final KeyguardMonitor mKeyguard;
    private final Callback mCallback = new Callback();

    public PowersavingTile(Host host) {
        super(host);
        mController = host.getPowersavingController();
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
        mController.setPowersavingEnabled(!wasEnabled);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean powersavingEnabled =  mController.isPowersavingEnabled();
        state.visible = !mKeyguard.isShowing();
        state.value = powersavingEnabled;
        if (powersavingEnabled) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_power_saving_enable);
            state.label = mContext.getString(R.string.quick_settings_power_saving_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_powersaving_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_power_saving_off);
            state.label = mContext.getString(R.string.quick_settings_power_saving_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_powersaving_off);
        }
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_powersaving_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_powersaving_changed_off);
        }
    }

    private final class Callback implements PowersavingSettingsChangeCallback,
            KeyguardMonitor.Callback {
        @Override
        public void onPowersavingSettingsChanged(boolean enabled) {
            refreshState();
        }

        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };
}
