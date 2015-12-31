package com.android.launcher3;

import android.widget.BaseAdapter;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.ListView;
import java.util.ArrayList;
import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.net.Uri;
import android.widget.ListAdapter;

@SuppressLint("NewApi")
public class ChooseLockAppActivity extends Activity {
    public static final String IS_LOCK_APP_ON = "isLockAppON";
    public static final String LOCK_APP = "lock_app";
    private CheckBox ckEncryptionState;
    private ListView lv_apps;
    private List<ResolveInfo> mAllApps;
    private PackageManager mPackageManager;
    private SharedPreferences sp;
    
    public ChooseLockAppActivity() {
        sp = null;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lock_app);
        mPackageManager = getPackageManager();
        sp = getSharedPreferences("lock_app", 0x2);
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        mAllApps = getPackageManager().queryIntentActivities(mainIntent, 0x0);
        ckEncryptionState = (CheckBox)findViewById(R.id.cb_encryption_state);
        lv_apps = (ListView)findViewById(R.id.lv_apps);
        lv_apps.setAdapter(new ChooseLockAppActivity.AppInfoAdapter(this, this, filterAllAppsData()));
        boolean isCheck = sp.getBoolean("isLockAppON", false);
        lv_apps.setVisibility(isCheck ? View.VISIBLE : View.INVISIBLE);
        ckEncryptionState.setText(isCheck ? R.string.tv_encryption_state_on : R.string.tv_encryption_state_off);
        ckEncryptionState.setChecked(isCheck);
        ckEncryptionState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
                lv_apps.setVisibility(paramBoolean ? View.VISIBLE : View.INVISIBLE);
                ckEncryptionState.setText(paramBoolean ? R.string.tv_encryption_state_on : R.string.tv_encryption_state_off);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("isLockAppON", paramBoolean);
                editor.apply();
                boolean isIncludeMMS = sp.getBoolean("com.android.mms.ui.BootActivity", true);
                if(isIncludeMMS) {
                	setMMSNotificationEnable(isIncludeMMS);
                }
                boolean isIncludeDialer = sp.getBoolean("com.android.dialer.DialtactsActivity", true);
                if(isIncludeDialer) {
                	setDialerNotificationEnable(isIncludeDialer);
                }
            }
        });
    }
    
    private List filterAllAppsData() {
        ArrayList<ResolveInfo> mAllApps_Filtered = new ArrayList<ResolveInfo>();
        Log.d("zhangle", "filterAllAppsData mAllApps.size=" + mAllApps.size());
        for(int i = 0x0; i < mAllApps.size(); i = i + 0x1) {
            ResolveInfo resolveInfo = (ResolveInfo)mAllApps.get(i);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            Log.d("zhangle", "filterAllAppsData activityInfo.icon=" + activityInfo.icon + " activityInfo.labelRes=" + activityInfo.labelRes);
            if(activityInfo != null) {
                String s = (String)getPackageManager().getApplicationLabel(activityInfo.applicationInfo);
                Log.d("zhangle", "s=" + s);
                Drawable d = getPackageManager().getApplicationIcon(activityInfo.applicationInfo);
                int icon = activityInfo.icon;
                int lable = activityInfo.labelRes;
                if("com.android.settings.Settings$WifiSettingsActivity".equals(activityInfo.name)) {
                } else if((icon != 0) && (lable != 0)) {
                    mAllApps_Filtered.add(resolveInfo);
                } else if((s != null) && (d != null)) {
                    mAllApps_Filtered.add(resolveInfo);
                }
            }
        }
        return mAllApps_Filtered;
    }
    
    class AppInfoAdapter extends BaseAdapter {
        private List<ResolveInfo> apps;
        private Context mContext;
        
        public AppInfoAdapter(ChooseLockAppActivity p1) {
        }
        
        public AppInfoAdapter(ChooseLockAppActivity p1, Context context, List list) {
            mContext = context;
            apps = list;
        }
        
        public int getCount() {
            return apps.size();
        }
        
        public Object getItem(int position) {
            return Integer.valueOf(position);
        }
        
        public long getItemId(int position) {
            return (long)position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.app_item_info, null);
            ImageView iv = (ImageView)view.findViewById(R.id.app_item_icon);
            TextView tv = (TextView)view.findViewById(R.id.app_item_name);
            CheckBox cb = (CheckBox)view.findViewById(R.id.app_item_state);
            ResolveInfo resolveInfo = (ResolveInfo)apps.get(position);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            ApplicationInfo applicationInfo = activityInfo.applicationInfo;
            int icon_res = activityInfo.icon;
            int text_res = activityInfo.labelRes;
            String activityName = activityInfo.name;
            Log.d("zhangle", "getView icon_res=" + icon_res + " text_res=" + text_res);
            Log.d("zhangle", "getView activityInfo=" + activityInfo.name);
            if((icon_res != 0) && (text_res != 0)) {
                try {
                    Resources res = mPackageManager.getResourcesForApplication(applicationInfo);
                    iv.setBackgroundDrawable(res.getDrawable(icon_res));
                    tv.setText(res.getString(text_res));
                    cb.setChecked(sp.getBoolean(activityName, false));
                } catch(PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } 
            }else {
                iv.setBackgroundDrawable(mPackageManager.getApplicationIcon(applicationInfo));
                tv.setText(mPackageManager.getApplicationLabel(applicationInfo));
                cb.setChecked(sp.getBoolean(activityName, false));
            }           
            cb.setTag(activityName);
            cb.setOnCheckedChangeListener(mCheckBoxListener);
            return view;
        }
    }
    
    private CompoundButton.OnCheckedChangeListener mCheckBoxListener = new CompoundButton.OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        	String activityName = (String)view.getTag();
        	if(null == activityName || activityName.equals(""))
        		return;
        	
            Log.d("zhangle", "onCheckedChanged paramBoolean=" + isChecked);
            Log.d("zhangle", "onCheckedChanged activityName=" + activityName);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(activityName, isChecked);
            editor.apply();
            if((activityName != null) && (activityName.equalsIgnoreCase("com.android.mms.ui.BootActivity"))) {
            	setMMSNotificationEnable(isChecked);
            }
            if((activityName != null) && (activityName.equalsIgnoreCase("com.android.dialer.DialtactsActivity"))) {
            	setDialerNotificationEnable(isChecked);
            }
        }          	
    };
    
    private void setMMSNotificationEnable(boolean paramBoolean) {
        Intent intent = new Intent("mms_is_choosed");
        intent.putExtra("ismmschoosed", paramBoolean);
        sendBroadcast(intent);
        if(paramBoolean) {
            Toast.makeText(this, R.string.mmsischoosed, Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private void setDialerNotificationEnable(boolean paramBoolean) {
        Intent intent = new Intent("dialer_is_choosed");
        intent.putExtra("isdialerschoosed", paramBoolean);
        sendBroadcast(intent);
        if(paramBoolean) {
            Toast.makeText(this, R.string.dialerischoosed, Toast.LENGTH_SHORT).show();
        }
    }
}
