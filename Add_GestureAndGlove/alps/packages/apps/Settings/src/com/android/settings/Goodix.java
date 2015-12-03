package com.android.settings;

import android.util.Log;

//caoqiaofeng add MTSFEFL-14 20150323
public class Goodix {

	static{
		try {
			System.loadLibrary("com_android_settings_jni");
		} catch (UnsatisfiedLinkError e) {
			Log.e("JNI", "Can't find file of .so."+e.toString());
		}
	}
	
	public native static int enableStylus();
	
	public native static int disableStylus();

	public native static int enableGesture();	
	
	public native static int disableGesture();


}
