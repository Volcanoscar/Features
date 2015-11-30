package com.android.internal.policy.impl;

import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.R;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

/**
 * kongtianhao add this for Shortcut bar 20150820
 * ShortcutDialog adapter 
 */
public class ShortcutAdapter extends BaseAdapter {

	private final static String TAG = "ShortcutAdapter";
	private List<HashMap<String, Object>> appInfos;
	private Context mContext;
	public TextView appinfo_tx, no_running_tx;
	public ImageView appinfo_img, appinfo_remove;
	private Handler mhandler;

	public ShortcutAdapter(Context mContext, List<HashMap<String, Object>> appinfos, Handler handler) {
		super();
		this.appInfos = appinfos;
		this.mContext = mContext;
		this.mhandler = handler;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		Log.i(TAG, "getCount = " + appInfos.size());
		return appInfos == null ? 0 : appInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return appInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.recycler_homelong_item, null);
			holder.appinfo_tx = (TextView) convertView.findViewById(R.id.appinfo_tx_recycler);
			holder.appinfo_img = (ImageView) convertView.findViewById(R.id.appinfo_img_recycler);
			holder.appinfo_remove = (ImageView) convertView.findViewById(R.id.appinfo_remove);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final View convert = convertView;
		HashMap<String, Object> runningAppInfo = new HashMap<String, Object>();
		runningAppInfo = appInfos.get(position);
		Log.i(TAG, "ShortcutAdapter runningAppInfo = " + runningAppInfo);
		final String pkgName = (String) runningAppInfo.get("pkgname");
		final int taskid = (Integer) runningAppInfo.get("taskid");
		final String appName = (String) runningAppInfo.get("appname");
		Bitmap thumbnail = (Bitmap) runningAppInfo.get("thumbnail");
		holder.appinfo_tx.setText(appName);
		holder.appinfo_img.setImageBitmap(thumbnail);
		holder.appinfo_remove.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_shortcutbar_remove));
		holder.appinfo_remove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				removeListItem(convert, position, pkgName);
			}
		});
		holder.appinfo_img.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "ShortcutAdapter img onClick taskid = " + taskid);
				Message msg = new Message();
				msg.what = ShortcutDialog.DISMISS_DIALOG_MOVETOFRONT;
				mhandler.sendMessage(msg);// make the dialog dismiss.
				if (taskid > 0) {
					final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
					am.moveTaskToFront(taskid, ActivityManager.MOVE_TASK_WITH_HOME);
				}
			}
		});
		return convertView;
	}

	private void removeListItem(View rowView, final int position, final String pkgName) {
		final Animation animation = (Animation) AnimationUtils.loadAnimation(rowView.getContext(),
				R.anim.floating_main_view_out);
		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				Log.i(TAG, "ShortcutAdapter removeListItem onAnimationEnd");
				Message msg = new Message();
				msg.what = ShortcutDialog.KILL_PROCESS_BY_PACKAGE;
				msg.obj = pkgName;
				msg.arg1 = position;
				mhandler.sendMessage(msg);// make the item dismiss.
				// animation.cancel(); //onAnimation will be resolved twice.
			}
		});
		rowView.startAnimation(animation);
	}

	static class ViewHolder {
		TextView appinfo_tx;
		ImageView appinfo_img;
		ImageView appinfo_remove;
	}
}
