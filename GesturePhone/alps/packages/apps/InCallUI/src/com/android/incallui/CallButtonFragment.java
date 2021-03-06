/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.telecom.AudioState;
import android.view.ContextThemeWrapper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.common.util.MaterialColorMapUtils.MaterialPalette;

import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.ext.ExtensionManager;
import com.mediatek.incallui.ext.IRCSeCallButtonExt;
import com.mediatek.incallui.recorder.PhoneRecorderUtils;

///aoran add for LFZSF-2 gestures functions,begin.
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.content.ContentResolver;
///aoran add for LFZSF-2 gestures functions,end.
/**
 * Fragment for call control buttons
 */
public class CallButtonFragment
        extends BaseFragment<CallButtonPresenter, CallButtonPresenter.CallButtonUi>
        implements CallButtonPresenter.CallButtonUi, OnMenuItemClickListener, OnDismissListener,
        View.OnClickListener {
    private CompoundButton mAudioButton;
    private ImageButton mChangeToVoiceButton;
    private CompoundButton mMuteButton;
    private CompoundButton mShowDialpadButton;
    private CompoundButton mHoldButton;
    private ImageButton mSwapButton;
    private ImageButton mChangeToVideoButton;
    private CompoundButton mSwitchCameraButton;
    private ImageButton mAddCallButton;
    private ImageButton mMergeButton;
    private CompoundButton mPauseVideoButton;
    private ImageButton mOverflowButton;

    private PopupMenu mAudioModePopup;
    private boolean mAudioModePopupVisible;
    private PopupMenu mOverflowPopup;

    private IRCSeCallButtonExt mRCSeExt;

    private int mPrevAudioMode = 0;

    // Constants for Drawable.setAlpha()
    private static final int HIDDEN = 0;
    private static final int VISIBLE = 255;

    private boolean mIsEnabled;
    private MaterialPalette mCurrentThemeColors;

    private Context mContext;

    ///aoran add for LFZSF-2 gestures functions,begin.
    private ContentResolver cr;
    private boolean mIsReceiverRegisted = false;
    public static final String ACTION_PHONE_SPEAKER_ON = "android.intent.action.ACTION_PHONE_SPEAKER_ON";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_PHONE_SPEAKER_ON)) {
                boolean speakerOn = intent.getBooleanExtra("speakerOn", false);
                Log.d("aoran","Receiver--speakerOn="+speakerOn);
                mAudioButton.setSelected(speakerOn);
            }
        }
    };
    ///aoran add for LFZSF-2 gestures functions,end.

    @Override
    CallButtonPresenter createPresenter() {
        // TODO: find a cleaner way to include audio mode provider than having a singleton instance.
        return new CallButtonPresenter();
    }

    @Override
    CallButtonPresenter.CallButtonUi getUi() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRCSeExt = ExtensionManager.getRCSeCallButtonExt();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.call_button_fragment, container, false);

        mAudioButton = (CompoundButton) parent.findViewById(R.id.audioButton);
        mAudioButton.setOnClickListener(this);
        mChangeToVoiceButton = (ImageButton) parent.findViewById(R.id.changeToVoiceButton);
        mChangeToVoiceButton. setOnClickListener(this);
        mMuteButton = (CompoundButton) parent.findViewById(R.id.muteButton);
        mMuteButton.setOnClickListener(this);
        mShowDialpadButton = (CompoundButton) parent.findViewById(R.id.dialpadButton);
        mShowDialpadButton.setOnClickListener(this);
        mHoldButton = (CompoundButton) parent.findViewById(R.id.holdButton);
        mHoldButton.setOnClickListener(this);
        mSwapButton = (ImageButton) parent.findViewById(R.id.swapButton);
        mSwapButton.setOnClickListener(this);
        mChangeToVideoButton = (ImageButton) parent.findViewById(R.id.changeToVideoButton);
        mChangeToVideoButton.setOnClickListener(this);
        mSwitchCameraButton = (CompoundButton) parent.findViewById(R.id.switchCameraButton);
        mSwitchCameraButton.setOnClickListener(this);
        mAddCallButton = (ImageButton) parent.findViewById(R.id.addButton);
        mAddCallButton.setOnClickListener(this);
        mMergeButton = (ImageButton) parent.findViewById(R.id.mergeButton);
        mMergeButton.setOnClickListener(this);
        mPauseVideoButton = (CompoundButton) parent.findViewById(R.id.pauseVideoButton);
        mPauseVideoButton.setOnClickListener(this);
        mOverflowButton = (ImageButton) parent.findViewById(R.id.overflowButton);
        mOverflowButton.setOnClickListener(this);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set the buttons
        updateAudioButtons(getPresenter().getSupportedAudio());
        mContext = getActivity();

        ///aoran add for LFZSF-2 gestures functions,begin.
        cr = mContext.getContentResolver();
        boolean bUpsetSPeakerOn = 1==Settings.Secure.getInt(cr,
             Settings.System.DEF_SMART_ACTION, Settings.Secure.DEF_FLIP_TO_SPEAKER, 0) ;
        if(bUpsetSPeakerOn){
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PHONE_SPEAKER_ON);
            mContext.registerReceiver(mReceiver, filter);
            mIsReceiverRegisted = true;
            Log.d("aoran", "RegisterReceiver for update SpeakerOn icon");
        }
        ///aoran add for LFZSF-2 gestures functions,end.
    }

    @Override
    public void onResume() {
        if (getPresenter() != null) {
            getPresenter().refreshMuteState();
            /// M: [Video call]Fix ALPS02063462. @{
            getPresenter().refreshCameraButtonState();
            /// @}
        }
        super.onResume();

        updateColors();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(this, "onClick(View " + view + ", id " + id + ")...");

        boolean isClickHandled = true;
        switch(id) {
            case R.id.audioButton:
                onAudioButtonClicked();
                break;
            case R.id.addButton:
                getPresenter().addCallClicked();
                break;
            case R.id.changeToVoiceButton:
                getPresenter().changeToVoiceClicked();
                break;
            case R.id.muteButton: {
                getPresenter().muteClicked(!mMuteButton.isSelected());
                break;
            }
            case R.id.mergeButton:
                getPresenter().mergeClicked();
                mMergeButton.setEnabled(false);
                break;
            case R.id.holdButton: {
                getPresenter().holdClicked(!mHoldButton.isSelected());
                break;
            }
            case R.id.swapButton:
                getPresenter().swapClicked();
                break;
            case R.id.dialpadButton:
                getPresenter().showDialpadClicked(!mShowDialpadButton.isSelected());
                break;
            case R.id.changeToVideoButton:
                getPresenter().changeToVideoClicked();
                break;
            case R.id.switchCameraButton:
                getPresenter().switchCameraClicked(
                        mSwitchCameraButton.isSelected() /* useFrontFacingCamera */);
                break;
            case R.id.pauseVideoButton:
                getPresenter().pauseVideoClicked(
                        !mPauseVideoButton.isSelected() /* pause */);
                break;
            case R.id.overflowButton:
                /// M: For ALPS01961019, Rapid continuous click twice. @{
                mOverflowPopup.dismiss();
                /// @}
                mOverflowPopup.show();
                break;
            default:
                isClickHandled = false;
                Log.wtf(this, "onClick: unexpected");
                break;
        }

        if (isClickHandled) {
            view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }

    @Override
    public void updateColors() {
        MaterialPalette themeColors = InCallPresenter.getInstance().getThemeColors();

        if (mCurrentThemeColors != null && mCurrentThemeColors.equals(themeColors)) {
            return;
        }
        if (themeColors == null) {
            return;
        }

        Resources res = getActivity().getResources();
        View[] compoundButtons = {
                mAudioButton,
                mMuteButton,
                mShowDialpadButton,
                mHoldButton,
                mSwitchCameraButton,
                mPauseVideoButton
        };

        for (View button : compoundButtons) {
            final LayerDrawable layers = (LayerDrawable) button.getBackground();
            final RippleDrawable btnCompoundDrawable = compoundBackgroundDrawable(themeColors);
            layers.setDrawableByLayerId(R.id.compoundBackgroundItem, btnCompoundDrawable);
            /// M: for ALPS01945830 & ALPS01976712. redraw the buttons. @{
            btnCompoundDrawable.setState(layers.getState());
            layers.invalidateSelf();
            /// @}
        }

        ImageButton[] normalButtons = {
            mChangeToVoiceButton,
            mSwapButton,
            mChangeToVideoButton,
            mAddCallButton,
            mMergeButton,
            mOverflowButton
        };

        for (ImageButton button : normalButtons) {
            final LayerDrawable layers = (LayerDrawable) button.getBackground();
            final RippleDrawable btnDrawable = backgroundDrawable(themeColors);
            layers.setDrawableByLayerId(R.id.backgroundItem, btnDrawable);
            /// M: for ALPS01945830 & ALPS01976712. redraw the buttons. @{
            btnDrawable.setState(layers.getState());
            layers.invalidateSelf();
            /// @}
        }

        mCurrentThemeColors = themeColors;
    }

    /**
     * Generate a RippleDrawable which will be the background for a compound button, i.e.
     * a button with pressed and unpressed states. The unpressed state will be the same color
     * as the rest of the call card, the pressed state will be the dark version of that color.
     */
    private RippleDrawable compoundBackgroundDrawable(MaterialPalette palette) {
        Resources res = getResources();
        ColorStateList rippleColor =
                ColorStateList.valueOf(res.getColor(R.color.incall_accent_color));

        StateListDrawable stateListDrawable = new StateListDrawable();
        addSelectedAndFocused(res, stateListDrawable);
        addFocused(res, stateListDrawable);
        addSelected(res, stateListDrawable, palette);
        addUnselected(res, stateListDrawable, palette);

        return new RippleDrawable(rippleColor, stateListDrawable, null);
    }

    /**
     * Generate a RippleDrawable which will be the background of a button to ensure it
     * is the same color as the rest of the call card.
     */
    private RippleDrawable backgroundDrawable(MaterialPalette palette) {
        Resources res = getResources();
        ColorStateList rippleColor =
                ColorStateList.valueOf(res.getColor(R.color.incall_accent_color));

        StateListDrawable stateListDrawable = new StateListDrawable();
        addFocused(res, stateListDrawable);
        addUnselected(res, stateListDrawable, palette);

        return new RippleDrawable(rippleColor, stateListDrawable, null);
    }

    // state_selected and state_focused
    private void addSelectedAndFocused(Resources res, StateListDrawable drawable) {
        int[] selectedAndFocused = {android.R.attr.state_selected, android.R.attr.state_focused};
        Drawable selectedAndFocusedDrawable = res.getDrawable(R.drawable.btn_selected_focused);
        drawable.addState(selectedAndFocused, selectedAndFocusedDrawable);
    }

    // state_focused
    private void addFocused(Resources res, StateListDrawable drawable) {
        int[] focused = {android.R.attr.state_focused};
        Drawable focusedDrawable = res.getDrawable(R.drawable.btn_unselected_focused);
        drawable.addState(focused, focusedDrawable);
    }

    // state_selected
    private void addSelected(Resources res, StateListDrawable drawable, MaterialPalette palette) {
        int[] selected = {android.R.attr.state_selected};
        LayerDrawable selectedDrawable = (LayerDrawable) res.getDrawable(R.drawable.btn_selected);
        ((GradientDrawable) selectedDrawable.getDrawable(0)).setColor(palette.mSecondaryColor);
        drawable.addState(selected, selectedDrawable);
    }

    // default
    private void addUnselected(Resources res, StateListDrawable drawable, MaterialPalette palette) {
        LayerDrawable unselectedDrawable =
                (LayerDrawable) res.getDrawable(R.drawable.btn_unselected);
        ((GradientDrawable) unselectedDrawable.getDrawable(0)).setColor(palette.mPrimaryColor);
        drawable.addState(new int[0], unselectedDrawable);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
        View view = getView();
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }

        mAudioButton.setEnabled(isEnabled);
        mChangeToVoiceButton.setEnabled(isEnabled);
        mMuteButton.setEnabled(isEnabled);
        mShowDialpadButton.setEnabled(isEnabled);
        mHoldButton.setEnabled(isEnabled);
        mSwapButton.setEnabled(isEnabled);
        mChangeToVideoButton.setEnabled(isEnabled);
        mSwitchCameraButton.setEnabled(isEnabled);
        mAddCallButton.setEnabled(isEnabled);
        mMergeButton.setEnabled(isEnabled);
        mPauseVideoButton.setEnabled(isEnabled);
        mOverflowButton.setEnabled(isEnabled);
    }

    @Override
    public void setMute(boolean value) {
        if (mMuteButton.isSelected() != value) {
            mMuteButton.setSelected(value);
        }
    }

    @Override
    public void showAudioButton(boolean show) {
        mAudioButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showChangeToVoiceButton(boolean show) {
        mChangeToVoiceButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void enableMute(boolean enabled) {
        mMuteButton.setEnabled(enabled);
    }

    @Override
    public void showDialpadButton(boolean show) {
        mShowDialpadButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setHold(boolean value) {
        if (mHoldButton.isSelected() != value) {
            mHoldButton.setSelected(value);
        }
    }

    @Override
    public void showHoldButton(boolean show) {
        mHoldButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void enableHold(boolean enabled) {
        mHoldButton.setEnabled(enabled);
    }

    @Override
    public void showSwapButton(boolean show) {
        mSwapButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showChangeToVideoButton(boolean show) {
        mChangeToVideoButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void enableChangeToVideoButton(boolean enable) {
        mChangeToVideoButton.setEnabled(enable);
    }

    @Override
    public void showSwitchCameraButton(boolean show) {
        mSwitchCameraButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSwitchCameraButton(boolean isBackFacingCamera) {
        mSwitchCameraButton.setSelected(isBackFacingCamera);
    }

    @Override
    public void showAddCallButton(boolean show) {
        Log.d(this, "show Add call button: " + show);
        mAddCallButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMergeButton(boolean show) {
        mMergeButton.setVisibility(show ? View.VISIBLE : View.GONE);

        // If the merge button was disabled, re-enable it when hiding it.
        if (!show) {
            mMergeButton.setEnabled(true);
        }
    }

    @Override
    public void showPauseVideoButton(boolean show) {
        mPauseVideoButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPauseVideoButton(boolean isPaused) {
        mPauseVideoButton.setSelected(isPaused);
    }

    @Override
    public void showOverflowButton(boolean show) {
        mOverflowButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void enableAddCall(boolean enabled) {
        mAddCallButton.setEnabled(enabled);
    }

    @Override
    public void configureOverflowMenu(boolean showMergeMenuOption, boolean showAddMenuOption,
            boolean showHoldMenuOption, boolean showSwapMenuOption, boolean showVoiceRecordOption) {
        if (mOverflowPopup == null) {
            final ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(),
                    R.style.InCallPopupMenuStyle);
            mOverflowPopup = new PopupMenu(contextWrapper, mOverflowButton);
            mOverflowPopup.getMenuInflater().inflate(R.menu.incall_overflow_menu,
                    mOverflowPopup.getMenu());
            mOverflowPopup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.overflow_merge_menu_item:
                            getPresenter().mergeClicked();
                            break;
                        case R.id.overflow_add_menu_item:
                            getPresenter().addCallClicked();
                            break;
                        case R.id.overflow_hold_menu_item:
                            getPresenter().holdClicked(true /* checked */);
                            break;
                        case R.id.overflow_resume_menu_item:
                            getPresenter().holdClicked(false /* checked */);
                            break;
                        case R.id.overflow_swap_menu_item:
                            getPresenter().addCallClicked();
                            break;
                        /// M: add other feature, eg: recording, ect and so on. @{
                        case R.id.menu_voice_record:
                            onVoiceRecordClick(item);
                            break;
                        case R.id.menu_ect:
                            onEctMenuSelected(item);
                            break;
                        case R.id.menu_hangup_all:
                            getPresenter().hangupAllClicked();
                            break;
                        case R.id.menu_hangup_holding:
                            getPresenter().hangupAllHoldCallsClicked();
                            break;
                        case R.id.menu_hangup_active_and_answer_waiting:
                            getPresenter().hangupActiveAndAnswerWaitingClicked();
                            break;
                        /// @}
                        default:
                            Log.wtf(this, "onMenuItemClick: unexpected overflow menu click");
                            break;
                    }
                    return true;
                }
            });
            mOverflowPopup.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu popupMenu) {
                    popupMenu.dismiss();
                }
            });
        }

        final Menu menu = mOverflowPopup.getMenu();
        mRCSeExt.configureOverflowMenu((Context)getActivity(), menu);
        menu.findItem(R.id.overflow_merge_menu_item).setVisible(showMergeMenuOption);
        menu.findItem(R.id.overflow_add_menu_item).setVisible(showAddMenuOption);
        menu.findItem(R.id.overflow_hold_menu_item).setVisible(
                showHoldMenuOption && !mHoldButton.isSelected());
        menu.findItem(R.id.overflow_resume_menu_item).setVisible(
                showHoldMenuOption && mHoldButton.isSelected());
        menu.findItem(R.id.overflow_swap_menu_item).setVisible(showSwapMenuOption);

        /// M: Added for mtk feature, eg: recording, ect and so on. @{
        updateOtherMenuItem(showVoiceRecordOption);
        /// @}

        mOverflowButton.setEnabled(menu.hasVisibleItems());
        // M: for ALPS01783112, dismiss mOverflowPopup when call state update.
        mOverflowPopup.dismiss();
    }

    private void onVoiceRecordClick(MenuItem menuItem) {
        Log.d(this, "onVoiceRecordClick");
        String title = menuItem.getTitle().toString();
        if (title == null) {
            return;
        }
        if (!PhoneRecorderUtils.isExternalStorageMounted(mContext)) {
            Toast.makeText(mContext,
                    mContext.getResources().getString(R.string.error_sdcard_access),
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!PhoneRecorderUtils
                .diskSpaceAvailable(PhoneRecorderUtils.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
            InCallPresenter.getInstance().handleStorageFull(true); // true for checking case
            return;
        }

        if (title.equals(getString(R.string.start_record))) {
            Log.d(this, "want to startRecord");
            getPresenter().voiceRecordClicked();
        } else if (title.equals(getString(R.string.stop_record))) {
            getPresenter().stopRecordClicked();
        }
    }

    @Override
    public void setAudio(int mode) {
        updateAudioButtons(getPresenter().getSupportedAudio());
        /// M: For ALPS01825524 @{
        // Telecomm will trigger AudioMode popup refresh when supported Audio has been changed. Here we only update Audio Button.
        // Original Code:
        // refreshAudioModePopup();
        /// @}

        if (mPrevAudioMode != mode) {
            updateAudioButtonContentDescription(mode);
            mPrevAudioMode = mode;
        }
    }

    @Override
    public void setSupportedAudio(int modeMask) {
        updateAudioButtons(modeMask);
        refreshAudioModePopup();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        /// M:  [log optimize] @{
        /** Google log:
        Log.d(this, "- onMenuItemClick: " + item);
        Log.d(this, "  id: " + item.getItemId());
        Log.d(this, "  title: '" + item.getTitle() + "'");
         */
        Log.d(this, "- onMenuItemClick...  title: '" + item.getTitle() + "'");
        /// @}

        // add for plug in. @{
        if (mRCSeExt.handleMenuItemClick(item)) {
            return true;
        }
        // add for plug in. @}

        int mode = AudioState.ROUTE_WIRED_OR_EARPIECE;

        switch (item.getItemId()) {
            case R.id.audio_mode_speaker:
                mode = AudioState.ROUTE_SPEAKER;
                break;
            case R.id.audio_mode_earpiece:
            case R.id.audio_mode_wired_headset:
                // InCallAudioState.ROUTE_EARPIECE means either the handset earpiece,
                // or the wired headset (if connected.)
                mode = AudioState.ROUTE_WIRED_OR_EARPIECE;
                break;
            case R.id.audio_mode_bluetooth:
                mode = AudioState.ROUTE_BLUETOOTH;
                break;
            default:
                Log.e(this, "onMenuItemClick:  unexpected View ID " + item.getItemId()
                        + " (MenuItem = '" + item + "')");
                break;
        }

        getPresenter().setAudioMode(mode);

        return true;
    }

    // PopupMenu.OnDismissListener implementation; see showAudioModePopup().
    // This gets called when the PopupMenu gets dismissed for *any* reason, like
    // the user tapping outside its bounds, or pressing Back, or selecting one
    // of the menu items.
    @Override
    public void onDismiss(PopupMenu menu) {
        Log.d(this, "- onDismiss: " + menu);
        mAudioModePopupVisible = false;
        updateAudioButtons(getPresenter().getSupportedAudio());
    }

    /**
     * Checks for supporting modes.  If bluetooth is supported, it uses the audio
     * pop up menu.  Otherwise, it toggles the speakerphone.
     */
    private void onAudioButtonClicked() {
        Log.d(this, "onAudioButtonClicked: " +
                AudioState.audioRouteToString(getPresenter().getSupportedAudio()));

        if (isSupported(AudioState.ROUTE_BLUETOOTH)) {
            showAudioModePopup();
        } else {
            getPresenter().toggleSpeakerphone();
        }
    }

    /**
     * Refreshes the "Audio mode" popup if it's visible.  This is useful
     * (for example) when a wired headset is plugged or unplugged,
     * since we need to switch back and forth between the "earpiece"
     * and "wired headset" items.
     *
     * This is safe to call even if the popup is already dismissed, or even if
     * you never called showAudioModePopup() in the first place.
     */
    public void refreshAudioModePopup() {
        if (mAudioModePopup != null && mAudioModePopupVisible) {
            // Dismiss the previous one
            mAudioModePopup.dismiss();  // safe even if already dismissed
            // And bring up a fresh PopupMenu
            showAudioModePopup();
        }
    }

    /**
     * Updates the audio button so that the appriopriate visual layers
     * are visible based on the supported audio formats.
     */
    private void updateAudioButtons(int supportedModes) {
        final boolean bluetoothSupported = isSupported(AudioState.ROUTE_BLUETOOTH);
        final boolean speakerSupported = isSupported(AudioState.ROUTE_SPEAKER);

        boolean audioButtonEnabled = false;
        boolean audioButtonChecked = false;
        boolean showMoreIndicator = false;

        boolean showBluetoothIcon = false;
        boolean showSpeakerphoneIcon = false;
        boolean showHandsetIcon = false;

        boolean showToggleIndicator = false;

        if (bluetoothSupported) {
            Log.d(this, "updateAudioButtons - popup menu mode");

            audioButtonEnabled = true;
            audioButtonChecked = true;
            showMoreIndicator = true;

            // Update desired layers:
            if (isAudio(AudioState.ROUTE_BLUETOOTH)) {
                showBluetoothIcon = true;
            } else if (isAudio(AudioState.ROUTE_SPEAKER)) {
                showSpeakerphoneIcon = true;
            } else {
                showHandsetIcon = true;
                // TODO: if a wired headset is plugged in, that takes precedence
                // over the handset earpiece.  If so, maybe we should show some
                // sort of "wired headset" icon here instead of the "handset
                // earpiece" icon.  (Still need an asset for that, though.)
            }

            // The audio button is NOT a toggle in this state, so set selected to false.
            mAudioButton.setSelected(false);
        } else if (speakerSupported) {
            Log.d(this, "updateAudioButtons - speaker toggle mode");

            audioButtonEnabled = true;

            // The audio button *is* a toggle in this state, and indicated the
            // current state of the speakerphone.
            audioButtonChecked = isAudio(AudioState.ROUTE_SPEAKER);
            mAudioButton.setSelected(audioButtonChecked);

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        } else {
            Log.d(this, "updateAudioButtons - disabled...");

            // The audio button is a toggle in this state, but that's mostly
            // irrelevant since it's always disabled and unchecked.
            audioButtonEnabled = false;
            audioButtonChecked = false;
            mAudioButton.setSelected(false);

            // update desired layers:
            showToggleIndicator = true;
            showSpeakerphoneIcon = true;
        }

        // Finally, update it all!

        Log.v(this, "audioButtonEnabled: " + audioButtonEnabled);
        Log.v(this, "audioButtonChecked: " + audioButtonChecked);
        Log.v(this, "showMoreIndicator: " + showMoreIndicator);
        Log.v(this, "showBluetoothIcon: " + showBluetoothIcon);
        Log.v(this, "showSpeakerphoneIcon: " + showSpeakerphoneIcon);
        Log.v(this, "showHandsetIcon: " + showHandsetIcon);

        // Only enable the audio button if the fragment is enabled.
        mAudioButton.setEnabled(audioButtonEnabled && mIsEnabled);
        mAudioButton.setChecked(audioButtonChecked);

        final LayerDrawable layers = (LayerDrawable) mAudioButton.getBackground();
        Log.d(this, "'layers' drawable: " + layers);

        layers.findDrawableByLayerId(R.id.compoundBackgroundItem)
                .setAlpha(showToggleIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.moreIndicatorItem)
                .setAlpha(showMoreIndicator ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.bluetoothItem)
                .setAlpha(showBluetoothIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.handsetItem)
                .setAlpha(showHandsetIcon ? VISIBLE : HIDDEN);

        layers.findDrawableByLayerId(R.id.speakerphoneItem)
                .setAlpha(showSpeakerphoneIcon ? VISIBLE : HIDDEN);

    }

    /**
     * Update the content description of the audio button.
     */
    private void updateAudioButtonContentDescription(int mode) {
        int stringId = 0;

        // If bluetooth is not supported, the audio buttion will toggle, so use the label "speaker".
        // Otherwise, use the label of the currently selected audio mode.
        if (!isSupported(AudioState.ROUTE_BLUETOOTH)) {
            stringId = R.string.audio_mode_speaker;
        } else {
            switch (mode) {
                case AudioState.ROUTE_EARPIECE:
                    stringId = R.string.audio_mode_earpiece;
                    break;
                case AudioState.ROUTE_BLUETOOTH:
                    stringId = R.string.audio_mode_bluetooth;
                    break;
                case AudioState.ROUTE_WIRED_HEADSET:
                    stringId = R.string.audio_mode_wired_headset;
                    break;
                case AudioState.ROUTE_SPEAKER:
                    stringId = R.string.audio_mode_speaker;
                    break;
            }
        }

        if (stringId != 0) {
            mAudioButton.setContentDescription(getResources().getString(stringId));
        }
    }

    private void showAudioModePopup() {
        Log.d(this, "showAudioPopup()...");

        final ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(),
                R.style.InCallPopupMenuStyle);
        mAudioModePopup = new PopupMenu(contextWrapper, mAudioButton /* anchorView */);
        mAudioModePopup.getMenuInflater().inflate(R.menu.incall_audio_mode_menu,
                mAudioModePopup.getMenu());
        mAudioModePopup.setOnMenuItemClickListener(this);
        mAudioModePopup.setOnDismissListener(this);

        final Menu menu = mAudioModePopup.getMenu();

        // TODO: Still need to have the "currently active" audio mode come
        // up pre-selected (or focused?) with a blue highlight.  Still
        // need exact visual design, and possibly framework support for this.
        // See comments below for the exact logic.

        final MenuItem speakerItem = menu.findItem(R.id.audio_mode_speaker);
        speakerItem.setEnabled(isSupported(AudioState.ROUTE_SPEAKER));
        // TODO: Show speakerItem as initially "selected" if
        // speaker is on.

        // We display *either* "earpiece" or "wired headset", never both,
        // depending on whether a wired headset is physically plugged in.
        final MenuItem earpieceItem = menu.findItem(R.id.audio_mode_earpiece);
        final MenuItem wiredHeadsetItem = menu.findItem(R.id.audio_mode_wired_headset);

        final boolean usingHeadset = isSupported(AudioState.ROUTE_WIRED_HEADSET);
        earpieceItem.setVisible(!usingHeadset);
        earpieceItem.setEnabled(!usingHeadset);
        wiredHeadsetItem.setVisible(usingHeadset);
        wiredHeadsetItem.setEnabled(usingHeadset);
        // TODO: Show the above item (either earpieceItem or wiredHeadsetItem)
        // as initially "selected" if speakerOn and
        // bluetoothIndicatorOn are both false.

        final MenuItem bluetoothItem = menu.findItem(R.id.audio_mode_bluetooth);
        bluetoothItem.setEnabled(isSupported(AudioState.ROUTE_BLUETOOTH));
        // TODO: Show bluetoothItem as initially "selected" if
        // bluetoothIndicatorOn is true.

        mAudioModePopup.show();

        // Unfortunately we need to manually keep track of the popup menu's
        // visiblity, since PopupMenu doesn't have an isShowing() method like
        // Dialogs do.
        mAudioModePopupVisible = true;
    }

    private boolean isSupported(int mode) {
        return (mode == (getPresenter().getSupportedAudio() & mode));
    }

    private boolean isAudio(int mode) {
        return (mode == getPresenter().getAudioMode());
    }

    @Override
    public void displayDialpad(boolean value, boolean animate) {
        mShowDialpadButton.setSelected(value);
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            ((InCallActivity) getActivity()).displayDialpad(value, animate);
        }
    }

    @Override
    public boolean isDialpadVisible() {
        if (getActivity() != null && getActivity() instanceof InCallActivity) {
            return ((InCallActivity) getActivity()).isDialpadVisible();
        }
        return false;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    // ---------------------------------------Mediatek-------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Add for plugin.
        mRCSeExt.onViewCreated(InCallPresenter.getInstance().getContext(), view);
    }

    @Override
    public void configRecordingMenuItemTitle() {
        if (mOverflowPopup == null) {
            return;
        }
        final Menu menu = mOverflowPopup.getMenu();
        final MenuItem recordMenu = menu.findItem(R.id.menu_voice_record);
        if (!InCallPresenter.getInstance().isRecording()) {
            recordMenu.setTitle(R.string.start_record);
        } else {
            recordMenu.setTitle(R.string.stop_record);
        }
    }

    private void updateOtherMenuItem(boolean showVoiceRecordOption) {
        final Menu menu = mOverflowPopup.getMenu();
        // Display the voice recording menu correctly
        final MenuItem recordMenu = menu.findItem(R.id.menu_voice_record);
        // Show recordMenu on USER_OWNER
        Log.i(this, "UserHandle.myUserId() : " + UserHandle.myUserId()
                + " |  UserHandle.USER_OWNER : " + UserHandle.USER_OWNER);
        if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
            recordMenu.setVisible(showVoiceRecordOption);
            if (showVoiceRecordOption) {
                configRecordingMenuItemTitle();
            }
            if (InCallUtils.isDMLocked()) {
                recordMenu.setEnabled(false);
            } else {
                recordMenu.setEnabled(true);
            }
        } else {
            recordMenu.setVisible(false);
        }

        // Update the visibility of ECT menu
        setEctMenu(menu);

        // Set hangup all menu, only show when there have more than one active call or
        // background call and has no incoming.
        final MenuItem hangupAllMenu = menu.findItem(R.id.menu_hangup_all);
        final boolean canHangupAllCalls = InCallUtils.canHangupAllCalls();
        hangupAllMenu.setVisible(canHangupAllCalls);

        // Set hangup all menu, only show when there have more than one active call or
        // background call and has no incoming.
        final MenuItem hangupHoldingMenu = menu.findItem(R.id.menu_hangup_holding);
        final boolean canHangupAllHoldCalls = InCallUtils.canHangupAllHoldCalls();
        hangupHoldingMenu.setVisible(canHangupAllHoldCalls);

        // Set hangup active and answer waiting menu, only show when there has
        // one active call and a incoming call which can be answered.
        final MenuItem hangupActiveAndAnswerWaitingMenu = menu
                .findItem(R.id.menu_hangup_active_and_answer_waiting);
        final boolean canHangupActiveAndAnswerWaiting = InCallUtils
                .canHangupActiveAndAnswerWaiting();
        hangupActiveAndAnswerWaitingMenu.setVisible(canHangupActiveAndAnswerWaiting);
    }

    /// M: for ALPS01749269 @{
    // dismiss all pop up menu when a new call incoming
    /// @}
    public void dismissPopupMenu() {
        Log.i(this, "dismissPopupMenu()");
        if (mAudioModePopup != null && mAudioModePopupVisible) {
            mAudioModePopup.dismiss();
            mAudioModePopupVisible = false;
        }

        // Fix ALPS01767216.
        if (mOverflowPopup != null) {
            mOverflowPopup.dismiss();
        }

        ///aoran add for LFZSF-2 gestures functions,begin.
        if(mIsReceiverRegisted){
            mContext.unregisterReceiver(mReceiver);
            mIsReceiverRegisted = false;
            Log.d("aoran", "UnregisterReceiver for update SpeakerOn icon");
        }
        ///aoran add for LFZSF-2 gestures functions,end.
    }

    private void setEctMenu(Menu menu) {
        final MenuItem ectMenu = menu.findItem(R.id.menu_ect);
        if (ectMenu != null) {
            if (getTheCallWithEctCapable() != null) {
                ectMenu.setVisible(true);
            } else {
                ectMenu.setVisible(false);
            }
        }
    }

    private void onEctMenuSelected(MenuItem menu) {
        if (menu != null) {
            final Call call = getTheCallWithEctCapable();
            if (call != null) {
                TelecomAdapter.getInstance().explicitCallTransfer(call.getTelecommCall().getCallId());
            }
        }
    }

    /**
     * M: get the ECT capable call.
     * For ECT, we just check the hold call.
     */
    private Call getTheCallWithEctCapable() {
        final List<Call> calls = CallList.getInstance().getBackgroundCalls();
        for (Call call : calls) {
            if (call != null && call.can(android.telecom.Call.Details.CAPABILITY_ECT)) {
                return call;
            }
        }

        return null;
    }
}
