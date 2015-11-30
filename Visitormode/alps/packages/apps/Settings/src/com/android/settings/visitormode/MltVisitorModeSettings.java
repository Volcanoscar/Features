package com.android.settings.visitormode;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.internal.widget.LockPatternUtils;

public class MltVisitorModeSettings extends PreferenceFragment {
	private static final String KEY_VISITOR_MODE = "malata_visitor_mode";
	private static final String KEY_CHANGE_PASSWORD = "malata_change_password";
	
	private static final int REQUEST_CODE_SET_LOCK = 151;
	
	private Activity mActivity;
    private LayoutInflater mInflater;
    
	private CheckBoxPreference mMalataVisitorMode;
	private Preference mChangePassword;
	
	private ListView mListView;
	private MyAdapter mAdapter;
	private AppAsyncLoader mAsyncTask;
	
	private LockPatternUtils mLockPatternUtils;
	private int mQuality = 0;
	
    // comparator for sorting the app list
    public static final Comparator<AppInfo> ALPHA_COMPARATOR = new Comparator<AppInfo>() {
        private final Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppInfo object1, AppInfo object2) {
            return mCollator.compare(object1.mAppName, object2.mAppName);
        }
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActivity = getActivity();
		
		mLockPatternUtils = new LockPatternUtils(mActivity);
		
		addPreferencesFromResource(R.xml.malata_visitor_mode_settings);
		
		mMalataVisitorMode = (CheckBoxPreference) findPreference(KEY_VISITOR_MODE);
		mChangePassword = findPreference(KEY_CHANGE_PASSWORD);
		
		boolean visitormode = getVisitorMode();
		if (mMalataVisitorMode != null) {
			mMalataVisitorMode.setChecked(visitormode);
		}
		if (mChangePassword != null) {
			mChangePassword.setEnabled(visitormode);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    mInflater = inflater;
	    View view = inflater.inflate(R.layout.malata_all_app, container, false);
        mListView = (ListView) view.findViewById(R.id.malata_list);
        mAdapter = new MyAdapter();
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ListView list = (ListView) parent;
                AppInfo app = (AppInfo) mAdapter.getItem(position);
                
                CheckBox checkBox = (CheckBox) v.findViewById(R.id.malata_status);
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    app.mAppHide = false;
                    mActivity.getContentResolver().delete(
                            Uri.parse("content://com.android.visitor"),
                            "package_name = ? AND activity_name = ?",
                            new String[] { app.mPkgName, app.mAtyName });
                } else {
                    checkBox.setChecked(true);
                    app.mAppHide = true;
                    ContentValues values = new ContentValues();
                    values.put("package_name", app.mPkgName);
                    values.put("activity_name", app.mAtyName);
                    mActivity.getContentResolver().insert(
                            Uri.parse("content://com.android.visitor"), values);
                }
                mAdapter.setHideItem(position, app);
            }
        });
        mListView.setAdapter(mAdapter);
	    return view;
	}
	
	@Override
	public void onResume() {
	    mQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality();
	    if (mQuality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
	        if (!mLockPatternUtils.isLockPatternEnabled() || !mLockPatternUtils.savedPatternExists()) {
	            mQuality = 0;
	        }
	    }
        
        int quality = Settings.Global.getInt(mActivity.getContentResolver(),
				"malata_visitor_mode", 0);
        
		if (quality != 0) {
	        if (mQuality != quality) {
	        	setVisitorMode(false);
	        }
			if (Settings.Global.getInt(mActivity.getContentResolver(),
					"malata_visitor_mode_in", 0) == 1) {
				mActivity.finish();
			}
		}
		super.onResume();
		
		startLoader();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CODE_SET_LOCK && resultCode == Activity.RESULT_OK) {
		    if (mQuality == 0) {
		        mQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality();
		    }
			setVisitorMode(true);
		} else {
			setVisitorMode(false);
		}
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mMalataVisitorMode) {
			if (mMalataVisitorMode.isChecked()) {
		        if (mQuality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
		        	Intent intent = new Intent(mActivity, MltVisitorModeLockPattern.class);
		        	intent.putExtra("pattern_mode", 1);
		        	intent.putExtra("confirm_credentials", false);
		        	startActivityForResult(intent, REQUEST_CODE_SET_LOCK);
		        } else if (mQuality == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC 
		        		|| mQuality == DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC 
		        		|| mQuality == DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC) {
		        	Intent intent = new Intent(mActivity, MltVisitorModeLockPassword.class);
		        	intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, mQuality);
		        	intent.putExtra("password_mode", 1);
		        	intent.putExtra("confirm_credentials", false);
		        	startActivityForResult(intent, REQUEST_CODE_SET_LOCK);
				} else {
				    createLockChooseDialog();
				}
			} else {
				setVisitorMode(false);
			}
		} else if (preference == mChangePassword) {
			if (getVisitorMode()) {
		        if (mQuality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
		        	Intent intent = new Intent(mActivity, MltVisitorModeLockPattern.class);
		        	intent.putExtra("pattern_mode", 1);
		        	intent.putExtra("confirm_credentials", false);
		        	startActivity(intent);
		        } else if (mQuality == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC 
		        		|| mQuality == DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC 
		        		|| mQuality == DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC) {
		        	Intent intent = new Intent(mActivity, MltVisitorModeLockPassword.class);
		        	intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, mQuality);
		        	intent.putExtra("password_mode", 1);
		        	intent.putExtra("confirm_credentials", false);
		        	startActivity(intent);
				}
			}
		}
		return false;
	}
	
    private void createLockChooseDialog() {
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int value) {
                        dialog.dismiss();
                        mMalataVisitorMode.setChecked(false);
                    }
                }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int value) {
                        Intent intent = new Intent(mActivity, MltVisitorModeLockGeneric.class);
                        intent.putExtra("confirm_credentials", false);
                        startActivityForResult(intent, REQUEST_CODE_SET_LOCK);
                        dialog.dismiss();
                    }
                }).setMessage(R.string.malata_set_password_hint)
                .setTitle(R.string.malata_set_password_title).create();
        dialog.show();
    }
	
	private void setVisitorMode(boolean isVisitorMode) {
		Settings.Global.putInt(mActivity.getContentResolver(), "malata_visitor_mode", isVisitorMode ? mQuality : 0);
	
		mMalataVisitorMode.setChecked(isVisitorMode);
		mChangePassword.setEnabled(isVisitorMode);
	}
	
	private boolean getVisitorMode() {
		return Settings.Global.getInt(mActivity.getContentResolver(), "malata_visitor_mode", 0) != 0;
	}
	
    private void startLoader() {
        mAsyncTask = (AppAsyncLoader)new AppAsyncLoader().execute();
    }
    
	private class AppAsyncLoader extends AsyncTask<Void, Integer, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            PackageManager mPM = mActivity.getPackageManager();
            
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            
            List<ResolveInfo> apps = mPM.queryIntentActivities(mainIntent, 0);
            if (apps == null) {
                return null;
            }

            List<AppInfo> appList = new ArrayList<AppInfo>();
            List<HideAppInfo> hideAppList = getHideAppInfoForDB();
            
            for (ResolveInfo info : apps) {
                if (isCancelled()) {
                    return null;
                }

                AppInfo appInfo = new AppInfo();
                appInfo.mPkgName = info.activityInfo.applicationInfo.packageName;
                appInfo.mAtyName = info.activityInfo.name;
                appInfo.mAppName = info.loadLabel(mPM).toString();
                appInfo.mAppIcon = info.loadIcon(mPM);
                appInfo.mAppHide = checkHideApp(appInfo, hideAppList);
                appList.add(appInfo);
            }

            Collections.sort(appList, ALPHA_COMPARATOR);
            return appList;
        }
	    
        @Override
        protected void onPostExecute(List<AppInfo> result) {
            mAdapter.setPackageList(result);
        }
        
        private List<HideAppInfo> getHideAppInfoForDB() {
            Cursor cursor = mActivity.getContentResolver().query(
                    Uri.parse("content://com.android.visitor"),
                    new String[] { "package_name", "activity_name" }, null, null, null);
            
            List<HideAppInfo> info = new ArrayList<HideAppInfo>();
            
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            HideAppInfo hide = new HideAppInfo();
                            hide.mPkgName = cursor.getString(0);
                            hide.mAtyName = cursor.getString(1);
                            info.add(hide);
                        } while (cursor.moveToNext());
                    }
                } finally {
                    cursor.close();
                }
            }
            return info;
        }
        
        private boolean checkHideApp(AppInfo app, List<HideAppInfo> hideAppList) {
            if (hideAppList == null || hideAppList.size() == 0) {
                return false;
            }
            for (HideAppInfo hide : hideAppList) {
                if (hide.mPkgName.equals(app.mPkgName) && hide.mAtyName.equals(app.mAtyName)) {
                    hideAppList.remove(hide);
                    return true;
                }
            }
            return false;
        }
	}
	
	private class MyAdapter extends BaseAdapter {
	    private List<AppInfo> mPkgList = new ArrayList<AppInfo>();
	    
	    public void setPackageList(List<AppInfo> list) {
	        mPkgList = list;
	        notifyDataSetChanged();
	    }
	    
	    public void setHideItem(int position, AppInfo info) {
	        if (mPkgList != null && info != null) {
	            mPkgList.set(position, info);
	        }
        }
	    
        @Override
        public int getCount() {
            if (mPkgList != null) {
                return mPkgList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mPkgList != null) {
                return mPkgList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder appViewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.malata_app_item, parent, false);
                
                appViewHolder = new AppViewHolder();
                appViewHolder.mAppIcon = (ImageView) convertView.findViewById(R.id.malata_app_icon);
                appViewHolder.mPkgLabel = (TextView) convertView.findViewById(R.id.malata_app_name);
                appViewHolder.mStaus = (CheckBox) convertView.findViewById(R.id.malata_status);
                convertView.setTag(appViewHolder);
            } else {
                appViewHolder = (AppViewHolder) convertView.getTag();
            }
            
            AppInfo appInfo = mPkgList.get(position);
            appViewHolder.mAppIcon.setImageDrawable(appInfo.mAppIcon);
            appViewHolder.mPkgLabel.setText(appInfo.mAppName);
            appViewHolder.mStaus.setChecked(appInfo.mAppHide);
            return convertView;
        }
	    
	}
	
    private class AppViewHolder {
        public ImageView mAppIcon;
        public TextView mPkgLabel;
        public CheckBox mStaus;
    }
    
    private class AppInfo {
        private String mPkgName;
        private String mAtyName;
        private String mAppName;
        private Drawable mAppIcon;
        private boolean mAppHide;
    }
    
    private class HideAppInfo {
        private String mPkgName;
        private String mAtyName;
    }
}
