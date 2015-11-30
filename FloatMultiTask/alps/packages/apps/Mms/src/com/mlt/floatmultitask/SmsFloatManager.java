package com.mlt.floatmultitask;

import android.content.Context;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.PixelFormat;
import android.util.Log;

public class SmsFloatManager {

	private static MsgWindowView msgWindow;

	private static WindowManager.LayoutParams mMsgWindowParams;

	private static WindowManager mWindowManager;

	public static MsgWindowView getMsgWindow() {
		return msgWindow;
	}

	 public static void createMsgWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if(null == msgWindow) {
            msgWindow = new MsgWindowView(context);
            mMsgWindowParams = new WindowManager.LayoutParams();
            setLayoutSize(context, mMsgWindowParams);
            mMsgWindowParams.x = (screenWidth - mMsgWindowParams.width) / 2;
            mMsgWindowParams.y = (screenHeight - mMsgWindowParams.height) / 2;
            mMsgWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mMsgWindowParams.format = PixelFormat.RGBA_8888;
            mMsgWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            mMsgWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;// | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            +mMsgWindowParams.windowAnimations = android.R.style.Animation_Toast;
        } else {
            windowManager.removeView(msgWindow);
        }
        msgWindow.setParams(mMsgWindowParams);
        windowManager.addView(msgWindow, mMsgWindowParams);
    }


    private static WindowManager getWindowManager(Context context) {
        if(null == mWindowManager) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

	 public static void removeMsgWindow(Context context) {
        if(null != msgWindow) {
            if(null == mWindowManager) {
                getWindowManager(context);
            }
            mWindowManager.removeView(msgWindow);
            msgWindow = null;
        }
    }

	public static void  setLayoutSize(Context context, WindowManager.LayoutParams params) {
        int i = context.getResources().getDisplayMetrics().densityDpi;
        Log.i("SmsPopView", "densityDpi===>" + i);
        if (i == 240)
        {
            params.width = 820;
            params.height = 620;
            return;
        }
        if (i == 270)
        {
            params.width = 820;
            params.height = 620;
            return;
        }
        if (i == 320)
        {
            params.width = 551;
            params.height = 412;
            return;
        }
        if (i == 480)
        {
            params.width = 820;
            params.height = 620;
            return;
        }
        if (i == 640)
        {
            params.width = 1090;
            params.height = 825;
            return;
        }
        params.width = 820;
        params.height = 620;
    }
}