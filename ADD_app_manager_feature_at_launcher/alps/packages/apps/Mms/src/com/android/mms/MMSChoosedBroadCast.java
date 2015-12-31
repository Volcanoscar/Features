package com.android.mms;

import android.content.*;
import android.util.Log;

public class MMSChoosedBroadCast extends BroadcastReceiver
{

    public MMSChoosedBroadCast()
    {
    }

    public void onReceive(Context context, Intent intent)
    {
        String s = intent.getAction();
        Log.d("zhangle", (new StringBuilder()).append("MMSChoosedBroadCast onReceive action=").append(s).toString());
        if(s != null && s.equalsIgnoreCase("mms_is_choosed"))
        {
            int value = 1;
            android.content.SharedPreferences.Editor editor = context.getSharedPreferences("com.android.mms_preferences", 4).edit();
            if(!intent.getBooleanExtra("ismmschoosed", true) )
                value = 0;
            editor.putInt("ismmschoosed", value);
            editor.commit();
        }
    }
}