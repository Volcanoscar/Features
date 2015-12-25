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
 * limitations under the License.
 */

package com.android.systemui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
//laiyang add for POPLUSVNMS-67 2015122 start
import android.provider.Settings;
import android.app.ActivityManager;
import android.os.UserHandle;
//laiyang add for POPLUSVNMS-67 end
import android.util.Log;
public class ToggleSlider extends RelativeLayout {
    public interface Listener {
        public void onInit(ToggleSlider v);
        public void onChanged(ToggleSlider v, boolean tracking, boolean checked, int value);
    }

    private Listener mListener;
    private boolean mTracking;

    private CompoundButton mToggle;
    private SeekBar mSlider;
    private TextView mLabel;

    private ToggleSlider mMirror;
    private BrightnessMirrorController mMirrorController;

    //laiyang modify for POPLUSVNMS-67 20151222 start
    public CompoundButton mAutoBrightness;
    private static ToggleSlider mToggleSlider;
    public static void setQsToggleSilder(ToggleSlider toggleSlider) {
        mToggleSlider = toggleSlider;
    }
    //laiyang modify for POPLUSVNMS-67 20151222 end

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.status_bar_toggle_slider, this);

        final Resources res = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ToggleSlider, defStyle, 0);

        mToggle = (CompoundButton) findViewById(R.id.toggle);
        mToggle.setOnCheckedChangeListener(mCheckListener);

        mSlider = (SeekBar) findViewById(R.id.slider);
        mSlider.setOnSeekBarChangeListener(mSeekListener);

        mLabel = (TextView) findViewById(R.id.label);
        mLabel.setText(a.getString(R.styleable.ToggleSlider_text));

        //laiyang add for POPLUSVNMS-67 20151222 start
        mAutoBrightness = (CompoundButton) findViewById(R.id.autoBrightness);
        // get current brightness mode
        int automatic;
        automatic = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                UserHandle.USER_CURRENT);
        if(0 == automatic) {// not auto brightness mode
            mAutoBrightness.setChecked(false);
        } else {// is auto brightness mode
            mAutoBrightness.setChecked(true);
        }
        // set CheckBox changed listener
        mAutoBrightness.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggle, boolean checked) {
                int mode = checked ? 1:0;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE, mode,
                        ActivityManager.getCurrentUser());
            }
        });
        //laiyang add for POPLUSVNMS-67 20151222 end

        a.recycle();
    }

    //laiyang add for POPLUSVNMS-67 20151222 start
    public static void setAutoBrightness(boolean mode) {
        if(mToggleSlider != null && mToggleSlider.mAutoBrightness != null) {
            mToggleSlider.mAutoBrightness.setChecked(mode);
        }
    }
    //laiyang add for POPLUSVNMS-67 20151222 end

    public void hideToggle() {
        mToggle.setVisibility(View.GONE);
        mLabel.setVisibility(View.GONE);        
    }


    public void setMirror(ToggleSlider toggleSlider) {
        Log.i("laiyang", "haha set mirror");
        mMirror = toggleSlider;
        if (mMirror != null) {
            mMirror.setChecked(mToggle.isChecked());
            mMirror.setMax(mSlider.getMax());
            mMirror.setValue(mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController c) {
        mMirrorController = c;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mListener != null) {
            mListener.onInit(this);
        }
    }

    public void setOnChangedListener(Listener l) {
        mListener = l;
    }

    public void setChecked(boolean checked) {
        mToggle.setChecked(checked);
    }

    public boolean isChecked() {
        return mToggle.isChecked();
    }

    public void setMax(int max) {
        mSlider.setMax(max);
        if (mMirror != null) {
            mMirror.setMax(max);
        }
    }

    public void setValue(int value) {
        mSlider.setProgress(value);
        if (mMirror != null) {
            mMirror.setValue(value);
        }
    }

    private final OnCheckedChangeListener mCheckListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton toggle, boolean checked) {
            mSlider.setEnabled(!checked);

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, checked, mSlider.getProgress());
            }

            if (mMirror != null) {
                mMirror.mToggle.setChecked(checked);
            }
        }
    };

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), progress);
            }

            if (mMirror != null) {
                mMirror.setValue(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTracking = true;
            /// laiyang add for POPLUSVNMS-67 20151222 @{
            // update mirror checkbox state
            if(mMirror != null) {
                mMirror.mAutoBrightness.setChecked(mAutoBrightness.isChecked());
            }
            /// @}
            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), mSlider.getProgress());
            }

            mToggle.setChecked(false);

            if (mMirror != null) {
                mMirror.mSlider.setPressed(true);
            }

            if (mMirrorController != null) {
                mMirrorController.showMirror();
                mMirrorController.setLocation((View) getParent());
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTracking = false;

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), mSlider.getProgress());
            }

            if (mMirror != null) {
                mMirror.mSlider.setPressed(false);
            }

            if (mMirrorController != null) {
                mMirrorController.hideMirror();
            }
        }
    };
}

