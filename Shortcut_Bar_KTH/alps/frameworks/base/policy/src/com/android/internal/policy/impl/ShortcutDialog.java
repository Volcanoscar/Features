package com.android.internal.policy.impl;

import android.os.Handler;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.Message;
import android.app.Service;
import android.os.Vibrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.internal.R;
import com.android.internal.policy.impl.brightness.*;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import android.provider.Settings;
import android.content.ContentResolver;
import android.app.ActivityManager.TaskThumbnail;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.PhoneConstants;

import android.telephony.TelephonyManager;
import android.bluetooth.BluetoothAdapter;
import android.net.wifi.WifiManager;

/**
 * Swipe up to access shortcuts from the bottom of the screen
 * kth add this for LFZSF-10 Shortcut bar test at 20150820
 */
public class ShortcutDialog extends Dialog implements View.OnClickListener, View.OnLongClickListener {

    private static final String CONTROLACTION_WIFI = "com.android.test.controller.wifi";
    private static final String CONTROLACTION_WIFI_DETAIL = "com.android.test.controller.wifi.detail";
    private static final String CONTROLACTION_WIFI_UPDATE_STATE = "com.android.test.controller.wifi.updatestate";

    private static final String CONTROLACTION_AIRPLANE = "com.android.test.controller.airplane";
    private static final String CONTROLACTION_AIRPLANE_UPDATE_STATE = "com.android.test.controller.airplane.updatestate";

    private static final String CONTROLACTION_DATA = "com.android.test.controller.data";
    private static final String CONTROLACTION_DATA_DETAIL = "com.android.test.controller.data.detail";
    //private static final String CONTROLACTION_DATA_UPDATE_STATE = "com.android.test.controller.data.updatestate";

    private static final String CONTROLACTION_FLASHLIGHT = "com.android.test.controller.flashlight";
    private static final String CONTROLACTION_FLASHLIGHT_UPDATE_STATE = "com.android.test.controller.flashlight.updatestate";

    private static final String CONTROLACTION_LOCATION = "com.android.test.controller.location";
    private static final String CONTROLACTION_LOCATION_UPDATE_STATE = "com.android.test.controller.location.updatestate";

    private static final String CONTROLACTION_BLUETOOTH = "com.android.test.controller.bluetooth";
    private static final String CONTROLACTION_BLUETOOTH_UPDATE_STATE = "com.android.test.controller.bluetooth.updatestate";

    private static final String CONTROLACTION_CAST = "com.android.test.controller.cast";
    private static final String CONTROLACTION_CAST_UPDATE_STATE = "com.android.test.controller.cast.updatestate";

    private static final String CONTROLACTION_INVERSION = "com.android.test.controller.inversion";
    private static final String CONTROLACTION_INVERSION_UPDATE_STATE = "com.android.test.controller.inversion.updatestate";

    private static final String CONTROLACTION_HOTSPOT = "com.android.test.controller.hotspot";
    private static final String CONTROLACTION_HOTSPOT_UPDATE_STATE = "com.android.test.controller.hotspot.updatestate";

    private static final String CONTROLACTION_ROTATION = "com.android.test.controller.rotation";
    private static final String CONTROLACTION_ROTATION_UPDATE_STATE = "com.android.test.controller.rotation.updatestate";

    private static final String CONTROLACTION_AUDIOPROFILE = "com.android.test.controller.audioprofile";
    private static final String CONTROLACTION_AUDIOPROFILE_UPDATE_STATE = "com.android.test.controller.audioprofile.updatestate";

    private static final int STATE_OFF = 0;
    private static final int STATE_ON = 1;
    private static final int STATE_WIFI_NO_NET = 2;
    private static final int STATE_WIFI_CONNECTED = 3;
    private static final int STATE_DATA_DISABLE = 6;

    private static final int STATE_ROTATION_PROTRAIT = 0;
    private static final int STATE_ROTATION_UNLOCK = 1;

    public static final int DISMISS_DIALOG_MOVETOFRONT = 1;
    public static final int KILL_PROCESS_BY_PACKAGE = 2;

    private final String TAG = "ShortcutDialog";

    GridView runningapp_gridV;
    ImageView ivTurbokey, ivWifi, ivCellular, ivAirplane, ivFlashlight, ivBT,
            ivLocation, ivCast, ivInversion, ivHotspot, ivRotation, ivMeeting;
    TextView tvNoRunningApp;
    Intent mintent = new Intent();
    Handler mHandler = new Handler();
    IntentFilter controlFilter = new IntentFilter();

    Context mContext;
    ShortcutAdapter adapter;
    BrightnessController mBrightnessController;
    ActivityManager am;
    TelephonyManager mTelephonyManager;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    WifiManager mWifiManager;

    List<HashMap<String, Object>> appInfos;

    private Handler switchHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(TAG, "ShortcutDialog switchHandler " + msg.what);
            switch (msg.what) {
                case DISMISS_DIALOG_MOVETOFRONT:
                    // move task to front
                    dismiss();
                    break;
                case KILL_PROCESS_BY_PACKAGE:
                    int position = msg.arg1;
                    killProcessByPkgname((String) msg.obj, msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };

    // refresh the custom controller icons when origin tile refreshed.
    private BroadcastReceiver updateStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.i(TAG, "updateStateReceiver received " + intent.getAction());
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int msg = intent.getIntExtra("msg", 0);
                    String action = intent.getAction();
                    if (action.equals(CONTROLACTION_WIFI_UPDATE_STATE)) {
                        refreshWLAN(msg);
                    } else if (action.equals(CONTROLACTION_AIRPLANE_UPDATE_STATE)) {
                        refreshAirplan(msg);
                    } else if (action.equals(CONTROLACTION_FLASHLIGHT_UPDATE_STATE)) {
                        refreshFlashlight(msg);
                    } else if (action.equals(CONTROLACTION_BLUETOOTH_UPDATE_STATE)) {
                        initBT(msg);
                    } else if (action.equals(CONTROLACTION_LOCATION_UPDATE_STATE)) {
                        refreshLocation(msg);
                    } else if (action.equals(CONTROLACTION_AUDIOPROFILE_UPDATE_STATE)) {
                        refreshAudioProfile(msg);
                    } else if (action.equals(CONTROLACTION_ROTATION_UPDATE_STATE)) {
                        refreshRotation(msg);
                    } else if (action.equals(
                            TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                        PhoneConstants.DataState state = getMobileDataState(intent);
                        refreshDataConnect(state);
                    } else if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)
                            || action.equals(mBluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                        refreshBT();
                    }
                }
            });
        }
    };

    public ShortcutDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTelephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        controlFilter.addAction(CONTROLACTION_WIFI_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_AIRPLANE_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_FLASHLIGHT_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_LOCATION_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_BLUETOOTH_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_CAST_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_AUDIOPROFILE_UPDATE_STATE);
        controlFilter.addAction(CONTROLACTION_ROTATION_UPDATE_STATE);
        controlFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        controlFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        controlFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        appInfos = new ArrayList<HashMap<String, Object>>();
        initDialogView();
        initView();
    }

    private void initDialogView() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(R.color.white);
        window.setType(WindowManager.LayoutParams.TYPE_DISPLAY_OVERLAY);
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        setContentView(R.layout.shortcut_bar_dialog_test);
        window.setWindowAnimations(R.style.ShortcutDialogAnim);
        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = window.getWindowManager().getDefaultDisplay().getWidth();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        refreshAllState();
        Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(150);
        setBriListening(true);
        mContext.registerReceiver(updateStateReceiver, controlFilter);
        initRunningApp();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        setBriListening(false);
        mContext.unregisterReceiver(updateStateReceiver);
    }

    private void initView() {
        runningapp_gridV = (GridView) findViewById(R.id.recentapp_container_recyclerview);
        tvNoRunningApp = (TextView) findViewById(R.id.tv_no_runningapp);
        ivTurbokey = (ImageView) findViewById(R.id.iv_turbokey);
        ivWifi = (ImageView) findViewById(R.id.iv_wifi);
        ivCellular = (ImageView) findViewById(R.id.iv_cellular);
        ivAirplane = (ImageView) findViewById(R.id.iv_airplane);
        ivFlashlight = (ImageView) findViewById(R.id.iv_flashlight);
        ivBT = (ImageView) findViewById(R.id.iv_bluetooth);
        ivLocation = (ImageView) findViewById(R.id.iv_location);
        ivCast = (ImageView) findViewById(R.id.iv_cast);
        ivInversion = (ImageView) findViewById(R.id.iv_inversion);
        ivHotspot = (ImageView) findViewById(R.id.iv_hotspot);
        ivRotation = (ImageView) findViewById(R.id.iv_rotation);
        ivMeeting = (ImageView) findViewById(R.id.iv_meeting);
        mBrightnessController = new BrightnessController(getContext(), (ImageView) findViewById(R.id.brightness_icon),
                (ToggleSlider) findViewById(R.id.brightness_slider));
        ivTurbokey.setOnClickListener(this);
        ivWifi.setOnClickListener(this);
        ivCellular.setOnClickListener(this);
        ivAirplane.setOnClickListener(this);
        ivFlashlight.setOnClickListener(this);
        ivBT.setOnClickListener(this);
        ivLocation.setOnClickListener(this);
        ivCast.setOnClickListener(this);
        ivInversion.setOnClickListener(this);
        ivHotspot.setOnClickListener(this);
        ivRotation.setOnClickListener(this);
        ivMeeting.setOnClickListener(this);
        ivCellular.setOnLongClickListener(this);
        ivWifi.setOnLongClickListener(this);
    }

    /**
     * BrightnessController Callbacks Listening
     *
     * @param listening
     */
    public void setBriListening(boolean listening) {
        if (listening) {
            mBrightnessController.registerCallbacks();
        } else {
            mBrightnessController.unregisterCallbacks();
        }
    }

    private void initRunningApp() {
        am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        RunningAppManager runningAppManager = new RunningAppManager(mContext);
        appInfos = runningAppManager.getRunningApp();
        if (appInfos.size() == 0) {
            // there is no running application
            tvNoRunningApp.setVisibility(View.VISIBLE);
            tvNoRunningApp.setText(R.string.no_runningapp_str);
        } else {
            tvNoRunningApp.setVisibility(View.GONE);
            //gridview width (dip)= 107 * appInfos.size() + 10 * (appInfos.size -1) + 20
            FrameLayout.LayoutParams paramsRunning = new FrameLayout.LayoutParams(
                    Utils.dip2px(mContext, 117 * appInfos.size()+10),
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            runningapp_gridV.setLayoutParams(paramsRunning);
            runningapp_gridV.setColumnWidth(Utils.dip2px(mContext, 107));
            runningapp_gridV.setHorizontalSpacing(Utils.dip2px(mContext, 10));
            runningapp_gridV.setStretchMode(GridView.NO_STRETCH);
            runningapp_gridV.setNumColumns(appInfos.size());
            Log.i(TAG, "runningTasks = " + appInfos);
            adapter = new ShortcutAdapter(mContext, appInfos, switchHandler);
            runningapp_gridV.setAdapter(adapter);
        }
    }

    /**
     * kill process by package name
     *
     * @param kill_pkgName selected item's packege name
     * @param item         position of the origin data list
     */
    private void killProcessByPkgname(final String kill_pkgName, int position) {
        Log.i(TAG, "kill_pkgName =" + kill_pkgName + " position =" + position);
        try {
            //execute the task
            am.killBackgroundProcesses(kill_pkgName);// Process was killed  but it will restart at once
            am.forceStopPackage(kill_pkgName);// it won't restart
        } catch (Exception e) {
            e.printStackTrace();
        }
        appInfos.remove(position);
        Log.i(TAG, "killProcessByPkgname appInfos.tostring = " + appInfos.toString());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            if (appInfos.size() == 0) {
                tvNoRunningApp.setVisibility(View.VISIBLE);
                tvNoRunningApp.setText(R.string.no_runningapp_str);
            }
        }
    }

    /**
     * kill all process in backstage
     */
    private void mTurboKey() {
        Log.i(TAG, "mTurboKey_runningtasks" + appInfos);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -300);
        translateAnimation.setDuration(700);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(700);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        ivTurbokey.startAnimation(animationSet);
        ivTurbokey.setImageResource(R.drawable.ic_qs_turbokey_rocket_white);
        int size = appInfos.size();
        for (int i = 0; i < size; i++) {
            Log.i(TAG, "mTurboKey_runningtasks for appInfos" + appInfos);
            HashMap<String, Object> appinfo = new HashMap<String, Object>();
            appinfo = appInfos.get(0);
            String kill_pkgName = (String) appinfo.get("pkgname");
            Log.i(TAG, "kill" + kill_pkgName);
            if (kill_pkgName == null || kill_pkgName.equals("")) {
            } else {
                killProcessByPkgname(kill_pkgName, 0);
            }
        }
        appInfos.clear();
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.iv_cellular:
                mintent.setAction(CONTROLACTION_DATA_DETAIL);
                mContext.sendBroadcast(mintent);
                dismiss();
                break;
            case R.id.iv_wifi:
                mintent.setAction(CONTROLACTION_WIFI_DETAIL);
                mContext.sendBroadcast(mintent);
                dismiss();
                break;
            default:
                break;
        }
        return true;
    }

    private void setBluetoothStatus() {
        switch (mBluetoothAdapter.getState()) {
            case BluetoothAdapter.STATE_ON:
            case BluetoothAdapter.STATE_TURNING_ON:
                mBluetoothAdapter.disable();
                break;
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
                mBluetoothAdapter.enable();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.i(TAG, "click view " + v.getId());
        if (Utils.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_turbokey:
                mTurboKey();
                break;
            case R.id.iv_wifi:
                mintent.setAction(CONTROLACTION_WIFI);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_cellular:
                mintent.setAction(CONTROLACTION_DATA);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_airplane:
                mintent.setAction(CONTROLACTION_AIRPLANE);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_flashlight:
                mintent.setAction(CONTROLACTION_FLASHLIGHT);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_location:
                mintent.setAction(CONTROLACTION_LOCATION);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_bluetooth:
                mintent.setAction(CONTROLACTION_BLUETOOTH);
                setBluetoothStatus();
                break;
            case R.id.iv_cast:
                mintent.setAction(CONTROLACTION_CAST);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_inversion:
                mintent.setAction(CONTROLACTION_INVERSION);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_hotspot:
                mintent.setAction(CONTROLACTION_HOTSPOT);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_rotation:
                mintent.setAction(CONTROLACTION_ROTATION);
                mContext.sendBroadcast(mintent);
                break;
            case R.id.iv_meeting:
                mintent.setAction(CONTROLACTION_AUDIOPROFILE);
                mContext.sendBroadcast(mintent);
                break;
            default:
                break;
        }
    }

    /**
     * refresh all controller icon states when shortcut bar is swiped up.
     */
    private void refreshAllState() {
        ContentResolver resolver = mContext.getContentResolver();
        int mode_location = Settings.Secure.getIntForUser(resolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
        int modeBT = Settings.Global.getInt(resolver, Settings.Global.BLUETOOTH_ON, 0);
        int modeWIFI = Settings.Global.getInt(resolver, Settings.Global.WIFI_ON, 0);
        int modeAirplan = Settings.Global.getInt(resolver, Settings.Global.AIRPLANE_MODE_ON, 0);
        int modeRotation = Settings.System.getIntForUser(resolver,
                Settings.System.ACCELEROMETER_ROTATION, 0, ActivityManager.getCurrentUser());
        int mode_audioprofile_now = Settings.Global.getInt(resolver, Settings.Global.AUDIOPROFILE_NOW_STATE, 0);
        boolean dataEnable = mTelephonyManager.getDataEnabled();

        if (mode_audioprofile_now == 2) {
            refreshAudioProfile(1);
        } else {
            refreshAudioProfile(0);
        }
        refreshWLAN(modeWIFI);
        // refreshDataConnect(msg);
        refreshDataConnect(dataEnable);
        refreshAirplan(modeAirplan);
        // refreshFlashlight(msg);
        initBT(modeBT);
        refreshLocation(mode_location);
        refreshRotation(modeRotation);
    }

    private void refreshWLAN(int msg) {
        if (msg == STATE_ON) {
            refreshImgResource(ivWifi, R.drawable.ic_qs_wifi_0);
        } else if (msg == STATE_OFF) {
            refreshImgResource(ivWifi, R.drawable.ic_qs_wifi_disabled);
        } else if (msg == STATE_WIFI_NO_NET) {
            refreshImgResource(ivWifi, R.drawable.ic_qs_wifi_no_network);
        } else if (msg == STATE_WIFI_CONNECTED) {
            refreshImgResource(ivWifi, R.drawable.ic_qs_wifi_full_4);
        }
    }
	
	/**
     * refresh data icon states when datastate changed. the state was read from PhoneConstants.DataState.
     */
    private void refreshDataConnect(PhoneConstants.DataState state) {
        if (state == PhoneConstants.DataState.CONNECTED) {
            refreshImgResource(ivCellular, R.drawable.ic_qs_mobile_enable);
        } else if (state == PhoneConstants.DataState.DISCONNECTED) {
            refreshImgResource(ivCellular, R.drawable.ic_qs_mobile_off);
        } else if (state == PhoneConstants.DataState.CONNECTING) {
            refreshImgResource(ivCellular, R.drawable.ic_qs_mobile_disable);
        }
    }

	/**
     * refresh data icon states when shortcut bar is swiped up. the state was read from setting.
     */
    private void refreshDataConnect(boolean state) {
        if (state) {
            refreshImgResource(ivCellular, R.drawable.ic_qs_mobile_enable);
        } else {
            refreshImgResource(ivCellular, R.drawable.ic_qs_mobile_off);
        }
    }

    private void refreshAirplan(int msg) {
        if (msg == STATE_ON) {
            refreshImgResource(ivAirplane, R.drawable.ic_qs_airplane_on);
        } else if (msg == STATE_OFF) {
            refreshImgResource(ivAirplane, R.drawable.ic_qs_airplane_off);
        }
    }

    private void refreshFlashlight(int msg) {
        if (msg == STATE_ON) {
            refreshImgResource(ivFlashlight, R.drawable.ic_qs_flashlight_on);
        } else if (msg == STATE_OFF) {
            refreshImgResource(ivFlashlight, R.drawable.ic_qs_flashlight_off);
        }
    }

    private void initBT(int msg) {
        if (msg == STATE_ON) {
            refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_on);
            // refreshState();
        } else if (msg == STATE_OFF) {
            refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_off);
        }
    }

    private void refreshBT() {
        Log.i(TAG, "mBluetoothAdapter state " + mBluetoothAdapter.getState());
        switch (mBluetoothAdapter.getState()) {
            case BluetoothAdapter.STATE_ON:
                refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_on);
                ivBT.setAlpha(255);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_on);
                ivBT.setAlpha(180);
                break;
            case BluetoothAdapter.STATE_OFF:
                refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_off);
                ivBT.setAlpha(255);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                refreshImgResource(ivBT, R.drawable.ic_qs_bluetooth_off);
                ivBT.setAlpha(255);
                break;
        }
    }

    private void refreshLocation(int msg) {
        if (msg == STATE_OFF) {
            refreshImgResource(ivLocation, R.drawable.ic_qs_location_off);
        } else if (msg == STATE_ON) {
            refreshImgResource(ivLocation, R.drawable.ic_qs_location_on);
        } else {
            refreshImgResource(ivLocation, R.drawable.ic_qs_location_on);
        }
    }

    private void refreshAudioProfile(int msg) {
        if (msg == STATE_ON) {
            refreshImgResource(ivMeeting, R.drawable.ic_qs_meeting_profile_enable);
        } else if (msg == STATE_OFF) {
            refreshImgResource(ivMeeting, R.drawable.ic_qs_meeting_profile_off);
        }
    }

    private void refreshRotation(int msg) {
        if (msg == STATE_ROTATION_PROTRAIT) {
            refreshImgResource(ivRotation, R.drawable.ic_qs_rotation_portrait);
        } else if (msg == 2) {
            refreshImgResource(ivRotation, R.drawable.ic_qs_rotation_landscape);
        } else if (msg == STATE_ROTATION_UNLOCK) {
            refreshImgResource(ivRotation, R.drawable.ic_qs_rotation_unlocked);
        }
    }

    public void refreshImgResource(ImageView imgView, int resId) {
        imgView.setImageResource(resId);
    }

    private PhoneConstants.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        } else {
            return PhoneConstants.DataState.DISCONNECTED;
        }
    }

    public static class Utils {
        private static long lastClickTime;

        /**
         * broadcast produce delay
         * if quick double click, it will block
         */
        public static boolean isFastDoubleClick() {
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            if (0 < timeD && timeD < 200) {
                return true;
            }
            lastClickTime = time;
            return false;
        }

        public static int dip2px(Context context, float dipValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            Log.i("ShortcutDialog", "density=" + scale);
            return (int) (dipValue * scale + 0.5f);
        }

        public static int px2dip(Context context, float pxValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return scale == 0 ? 0 : (int) (pxValue / scale + 0.5f);
        }
    }
}
