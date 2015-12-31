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

package com.android.systemui.statusbar.policy;

public interface PowersavingController {
    boolean isPowersavingEnabled();
    boolean setPowersavingEnabled(boolean enabled);
    void addSettingsChangedCallback(PowersavingSettingsChangeCallback cb);
    void removeSettingsChangedCallback(PowersavingSettingsChangeCallback cb);

    /**
     * A callback for change in powersaving settings (the user has enabled/disabled powersaving).
     */
    public interface PowersavingSettingsChangeCallback {
        /**
         * Called whenever powersaving settings change.
         *
         * @param powersavingEnabled A value of true indicates that at least one type of powersaving
         *                        is enabled in settings.
         */
        void onPowersavingSettingsChanged(boolean powersavingEnabled);
    }
}
