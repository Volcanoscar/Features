package com.mediatek.dataprotection.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.internal.widget.LockPatternUtils;

public class DataProtectionLockPatternUtils {

    private static final String TAG = "DataProtectionLockPatternUtils";
    private Context mContext = null;

    public DataProtectionLockPatternUtils(Context context) {
        mContext = context;
    }

    public boolean saveLockPattern(String pattern) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        if (pattern == null) {
            editor.clear();
            editor.commit();
            return true;
        }
        /*liuhao for POPLUSVNMS-114 20151214 begin*/
        //final byte[] hash = LockPatternUtils.patternToHash(LockPatternUtils
        //        .stringToPattern(pattern));
        editor.putString("password", pattern/*new String(hash)*/);
        /*liuhao for POPLUSVNMS-114 20151214 end*/
        // editor.putString(arg0, arg1)
        boolean result = editor.commit();
        Log.d(TAG, "saveLockPattern...result " + result);

        return result;
    }

    public boolean isPatternSet() {
        Log.d(TAG, "isPatternSet...");
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        String pattern = prefs.getString("password", null);

        return pattern != null;
    }

    public boolean checkPattern(String pattern) {
        Log.d(TAG, "checkPattern...");
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        String oldPattern = prefs.getString("password", null);
        /*liuhao for POPLUSVNMS-114 20151214 begin*/
        //final byte[] hash = LockPatternUtils.patternToHash(LockPatternUtils
        //        .stringToPattern(pattern));
        //String newPattern = new String(hash);
        return oldPattern.equals(pattern/*newPattern*/);
        /*liuhao for POPLUSVNMS-114 20151214 end*/
    }
}
