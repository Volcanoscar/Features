package com.android.settings.accessibility;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;

import com.android.settings.R;

/*
* this class used to display selection an application interface
*/

public class ShakePreferenceFragment extends PreferenceFragment implements OnItemClickListener{
   
    private static final String UPDATESHAREDPREFERENCE="updateSharedPreference";
	private static final String SAVESELECTSTATUS = "saveSelectStatus";
	private static final String SHAKESHAREDPREFERENCE="shake_spf";
    private ShakeAppAdapeter adapeter;
    private ListView mListView;
    private ArrayList<ShakeAppInfo> mAppList;
    public static String mAppPackageName =null;
	private Activity mActivity;
	
    private String mLastSelect;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		mActivity = getActivity();
		addPreferencesFromResource(R.xml.shake_open_app_select);
	    //get packageName from sharedPreferences
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(SHAKESHAREDPREFERENCE, Activity.MODE_PRIVATE);
		mLastSelect = sharedPreferences.getString(SAVESELECTSTATUS, "null");
        getApp();
        adapeter = new ShakeAppAdapeter(mAppList, getActivity());
    }

	@Override
    public void onStart() {
        super.onStart();
     //   getActivity().startService(new Intent(getActivity(),ShakeService.class));
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater,
             ViewGroup container,  Bundle savedInstanceState) {	 
		View view = inflater.inflate(R.xml.malata_all_app, container, false);
        mListView = (ListView) view.findViewById(R.id.malata_list);
        mListView.setAdapter(adapeter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(this);
        return view;	 
    }
	
	
    @Override
    public void onItemClick(AdapterView<?> listView, View itemLayout, int position, long id) { 
		mAppPackageName = mAppList.get(position).getAppPackage();
		mLastSelect=null;
		//add this Item to sharedPreferences
		SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(SHAKESHAREDPREFERENCE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SAVESELECTSTATUS, mAppPackageName);
        editor.commit();
		//tell the service to update packageName
		Intent intent =new Intent(UPDATESHAREDPREFERENCE); 
        getActivity().sendBroadcast(intent);
        if (itemLayout.getTag() instanceof Holder) {
            Holder holder = (Holder) itemLayout.getTag();
            holder.radioButton.toggle();
            HashMap<String, Boolean> map = adapeter.getStates();
            for (String key : map.keySet()) {
                map.put(key, false);
            }
            map.put(String.valueOf(position), true);
            adapeter.setStates(map);
            adapeter.notifyDataSetChanged();
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
   /*
	*get Application for installation
	*/
	public void getApp() {
        mAppList = new ArrayList<ShakeAppInfo>();
        PackageManager mPM = mActivity.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPM.queryIntentActivities(mainIntent, 0);
		for (ResolveInfo info : apps) {       
            ShakeAppInfo appInfo = new ShakeAppInfo();
            appInfo.setAppPackage(info.activityInfo.applicationInfo.packageName);
            appInfo.setAppName(info.loadLabel(mPM).toString());
            appInfo.setAppIcon(info.loadIcon(mPM));
            mAppList.add(appInfo);
        }
    }
	
	private class Holder {
        ImageView appIcon;
        TextView appName;
        RadioButton radioButton;
    }

    private class ShakeAppInfo {

        private String appName;
        private Drawable appIcon;
        private String packageName;
        private boolean selected;

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public Drawable getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(Drawable appIcon) {
            this.appIcon = appIcon;
        }

        public String getAppPackage() {
            return packageName;
        }

        public void setAppPackage(String packageName) {
            this.packageName = packageName;
        }

    }

    private class ShakeAppAdapeter extends BaseAdapter {

        private ArrayList<ShakeAppInfo> mAppList;
        private Context mContext;
        private LayoutInflater mInflater;
		//save radioButton status 
        private HashMap<String, Boolean> states = new HashMap<String, Boolean>();

        public void setStates(HashMap<String, Boolean> states) {
            this.states = states;
        }

        public HashMap<String, Boolean> getStates() {
            return states;
        }

        public ShakeAppAdapeter(ArrayList<ShakeAppInfo> mAppList, Context context) {
            super();
            this.mAppList = mAppList;
            this.mContext = context;
            mInflater = LayoutInflater.from(mContext);
       }

        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = mInflater.inflate(R.xml.shake_applist_item, null);
                holder.appIcon = (ImageView) convertView
                        .findViewById(R.id.itemImageview);
                holder.appName = (TextView) convertView
                        .findViewById(R.id.itemText);

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            ShakeAppInfo appInfo = mAppList.get(position);
            holder.appIcon.setImageDrawable(appInfo.getAppIcon());
            holder.appName.setText(appInfo.getAppName());
            final RadioButton radio = (RadioButton) convertView
                    .findViewById(R.id.itemradiobt);
            holder.radioButton = radio;
            holder.radioButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
					//save this Item to sharedPreferences
                    ShakePreferenceFragment.mAppPackageName = mAppList.get(
                            position).getAppPackage();
					mLastSelect=null;
					SharedPreferences sharedPreferences = getActivity()
                           .getSharedPreferences(SHAKESHAREDPREFERENCE, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SAVESELECTSTATUS, mAppPackageName);
                    editor.commit();
					//send broadcast to service get lastest 
					Intent intent =new Intent(UPDATESHAREDPREFERENCE); 
                    getActivity().sendBroadcast(intent);
                    for (String key : states.keySet()) {
                        states.put(key, false);
                    }
                    states.put(String.valueOf(position), radio.isChecked());
                    ShakeAppAdapeter.this.notifyDataSetChanged();
                }
            });
            boolean res = false;
            if (states.get(String.valueOf(position)) == null
                    || states.get(String.valueOf(position)) == false) {
				 if (mAppList.get(position).getAppPackage().equals(mLastSelect)) {
                    res = true;
					states.put(String.valueOf(position), true);
                } else {
                    states.put(String.valueOf(position), false);
                    res = false;
                }		   
            } else {
                res = true;
            }
            holder.radioButton.setChecked(res);
            return convertView;
        }

    }
	
}
