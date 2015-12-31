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
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.keyguard.R;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.keyguard.KeyguardViewBase;

import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager ;

import static com.android.keyguard.KeyguardHostView.OnDismissAction;


/**
 * A class which manages the bouncer on the lockscreen.
 */
public class MalataHall {

    private final String TAG = "MalataHall" ;
    private final boolean DEBUG = true ;

    private Context mContext;
    private ViewMediatorCallback mCallback;
    private ViewGroup mContainer;
    private StatusBarWindowManager mWindowManager;
    private ViewGroup mRoot;
    private boolean mShowingSoon;
    private Choreographer mChoreographer = Choreographer.getInstance();

    private ViewGroup mNotificationPanel ;

    public MalataHall(Context context, ViewMediatorCallback callback, StatusBarWindowManager windowManager,
            ViewGroup container) {
        mContext = context;
        mCallback = callback;
        mContainer = container;
        mWindowManager = windowManager;
        mNotificationPanel = (ViewGroup) mContainer.findViewById(R.id.notification_panel);
   }
    public void show() {
        if (DEBUG) Log.d(TAG, "show() is called.") ;

        ensureView();
        
        if (mRoot.getVisibility() == View.VISIBLE || mShowingSoon) {

            // show() updates the current security method. This is needed in case we are already
            // showing and the current security method changed.
 
            return;
        }


            mShowingSoon = true;

            // Split up the work over multiple frames.
            mChoreographer.postCallbackDelayed(Choreographer.CALLBACK_ANIMATION, mShowRunnable,
                    null, 48);
    }
    

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mShowRunnable.run() is called.") ;
            mRoot.setVisibility(View.VISIBLE);

            mShowingSoon = false;
        }
    };

    private void cancelShowRunnable() {
        mChoreographer.removeCallbacks(Choreographer.CALLBACK_ANIMATION, mShowRunnable, null);
        mShowingSoon = false;
    }

    public void showWithDismissAction(OnDismissAction r) {
        if (DEBUG) Log.d(TAG, "showWithDismissAction() is called.") ;
        ensureView();
   
        show();
    }

    public void hide(boolean destroyView) {
        if (DEBUG) {
            Log.d(TAG, "hide() is called, destroyView = " + destroyView) ;
        }

        cancelShowRunnable();

        if (destroyView) {
            if (DEBUG) Log.d(TAG, "call removeView()") ;
            removeView();
        } else if (mRoot != null) {
            if (DEBUG) Log.d(TAG, "just set keyguard Invisible.") ;
            mRoot.setVisibility(View.INVISIBLE);
        }

    }


    /**
     * See {@link StatusBarKeyguardViewManager#startPreHideAnimation}.
     */
    public void startPreHideAnimation(Runnable runnable) {
    }


    /**
     * Reset the state of the view.
     */
    public void reset() {
        cancelShowRunnable();
        inflateView();
    }

    public void onScreenTurnedOff() {
    }

    public void onScreenTurnedOn(){
    }

    public long getUserActivityTimeout() {
        return KeyguardViewMediator.AWAKE_INTERVAL_DEFAULT_MS;
    }

    public boolean isShowing() {
        return mShowingSoon || (mRoot != null && mRoot.getVisibility() == View.VISIBLE);
    }

    public void prepare() {
        ensureView();
    }

    private void ensureView() {
        if (mRoot == null) {
            inflateView();
        }
    }

    private void inflateView() {
        if (DEBUG) Log.d(TAG, "inflateView() is called, we force to re-inflate the \"Bouncer\" view.") ;

        removeView();
        mRoot = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.small_window, null);
        mContainer.addView(mRoot, mContainer.getChildCount());
        mRoot.setVisibility(View.INVISIBLE);
        mRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_HOME);
    }

    private void removeView() {
        if (mRoot != null && mRoot.getParent() == mContainer) {

            Log.d(TAG, "removeView() - really remove all views.") ;

            mContainer.removeView(mRoot);
            mRoot = null;
        }
    }

    public boolean onBackPressed() {
        return false;
    }

    /**
     * @return True if and only if the current security method should be shown before showing
     *         the notifications on Keyguard, like SIM PIN/PUK.
     */
    public boolean needsFullscreenBouncer() {
        return true;
    }

    public boolean isSecure() {
        return true;
    }

    public boolean onMenuPressed() {
        return false;
    }

    public boolean interceptMediaKey(KeyEvent event) {
        return false;
    }

    /**
     * @return True if mRoot view container is inflated.
     */
    public boolean isContainerInflated() {
        Log.d(TAG, "isContainerInflated() - ans is " + (mRoot != null)) ;
        return mRoot != null ;
    }
}
