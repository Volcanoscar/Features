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
import com.android.systemui.statusbar.policy.LocationController;
import android.content.Intent;
import com.android.systemui.statusbar.policy.GloveController;
import com.android.systemui.statusbar.policy.GloveController.GloveSettingsChangeCallback;
import android.util.Log;

//caoqiaofeng add MTSFEFL-14 20150323
/** Quick settings tile: Glove **/
public class GloveTile extends QSTile<QSTile.BooleanState> {

    private final GloveController mController;
    private final KeyguardMonitor mKeyguard;

    private final Callback mCallback = new Callback();
	 
    public GloveTile(Host host) {
        super(host);
        mController = null;//yutianliang modify for VFOZ-57 old:host.getGloveController()
        mKeyguard = host.getKeyguardMonitor();

    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        Log.i("malata", "cqff setListening listening="+listening);
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
	 Log.i("malata", "cqfff handleClick wasEnabled="+wasEnabled);	
        
        mController.setGloveEnabled(!wasEnabled);
		
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final boolean gloveEnabled =  mController.isGloveEnabled();
         Log.i("malata", "cqff handleUpdateState gloveEnabled="+gloveEnabled);
        // Work around for bug 15916487: don't show location tile on top of lock screen. After the
        // bug is fixed, this should be reverted to only hiding it on secure lock screens:         
        
        state.visible = !mKeyguard.isShowing();
        state.value = gloveEnabled;
        if (gloveEnabled) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_glove_enable);
            state.label = mContext.getString(R.string.quick_settings_gloves_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_glove_off);
            state.label = mContext.getString(R.string.quick_settings_gloves_label);
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_location_off);
        }
    }

    @Override
    protected String composeChangeAnnouncement() {
        Log.i("malata", "cqff composeChangeAnnouncement mState.value="+mState.value);
        if (mState.value) {
            return mContext.getString(R.string.quick_settings_gloves_label);
        } else {
            return mContext.getString(R.string.quick_settings_gloves_label);
        }
    }
    private final class Callback implements GloveSettingsChangeCallback,
            KeyguardMonitor.Callback {
        @Override
        public void onGloveSettingsChanged(boolean enabled) {
            Log.i("malata", "cqff onGloveSettingsChanged enabled=" + enabled);
            refreshState();
        }

        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };

}
