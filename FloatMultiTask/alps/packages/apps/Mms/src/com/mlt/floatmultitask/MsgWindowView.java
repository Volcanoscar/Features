package com.mlt.floatmultitask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.mms.R;
import java.util.ArrayList;
import java.util.List;
import android.os.Vibrator;
import android.view.inputmethod.InputMethodManager;
import java.lang.reflect.Field;
import android.view.MotionEvent;
import android.app.Service;
//import android.content.SharedPreferences;

/**
 * Created by laiyang on 15-7-8.
 */
public class MsgWindowView extends LinearLayout implements View.OnClickListener,
	Runnable, View.OnTouchListener, View.OnLongClickListener {

    private final static int MSG_GET_MSGS = 1001;
    private static final int MSG_SEND_SMS_SUCCESS = 1002;
    private static final int MSG_SEND_SMS_FAIL = 1003;
    // WindowManager
    private WindowManager.LayoutParams mParams;

    private RelativeLayout mrlMsgWholeView;
    private RelativeLayout mrlTitle;
    private RelativeLayout mrlLayoutTitleAndUnRead;
    private RelativeLayout mrlSmsBottom;
    private RelativeLayout mrlEditModeContent;
    private RelativeLayout mrlGalleryLayout;
//    private RelativeLayout mrlSmsPasswd;
    private TextView mtvTitle;
    private TextView mtvUnreadNum;
    private TextView mtvEmptySms;
    private TextView mtvNewMessage;
    private TextView mtvContact;
    private EditText metEditContent;
//    private EditText metInputPasswd;
    private TextView mtvResendNotify;
//    private TextView mtvPasswdRetry;
    private ImageButton mibBack;
    private ImageButton mibMinimize;
    private ImageButton mibClose;
    private ImageButton mibDelete;
    private ImageButton mibReply;
    private Button mbtnEditOK;
    private Button mbtnEditCancel;
    private Gallery mGallery;
    private List<SmsInfo> mSms;
    private static String smsDatePattern;
    private int unReadNum;
    private Context mContext;
    private SmsGalleryAdapter mSmsAdapter;

	private WindowManager windowManager;

    private static int statusBarHeight;

    private static final String TAG = "haha";
    private float touchX;

    private float touchY;

    private float xInScreen;

    private float yInScreen;

    public MsgWindowView(Context context) {
        super(context);
        mContext = context;
		windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.sms_popup_main, this);
        mrlMsgWholeView = (RelativeLayout) findViewById(R.id.msgWholeView);
        mrlTitle = (RelativeLayout) findViewById(R.id.layout_title);
        mrlLayoutTitleAndUnRead = (RelativeLayout) findViewById(R.id.layout_titleandunread);
        mrlSmsBottom = (RelativeLayout) findViewById(R.id.sms_pop_bottom);
        mrlEditModeContent = (RelativeLayout) findViewById(R.id.edit_model_content_layout);
        mrlGalleryLayout = (RelativeLayout) findViewById(R.id.gallery_layout);
//        mrlSmsPasswd = (RelativeLayout) findViewById(R.id.sms_pop_passwd);
        mtvTitle = (TextView) findViewById(R.id.sms_title);
        mtvUnreadNum = (TextView) findViewById(R.id.unreadnum);
        mtvEmptySms = (TextView) findViewById(R.id.emptyMessage);
        mtvNewMessage = (TextView) findViewById(R.id.sms_new_message);
        mtvContact = (TextView) findViewById(R.id.contact);
        mtvResendNotify = (TextView) findViewById(R.id.resendnotify);
//        mtvPasswdRetry = (TextView) findViewById(R.id.sms_passwd_retry);
        mibBack = (ImageButton) findViewById(R.id.switch_btn);
        mibMinimize = (ImageButton) findViewById(R.id.minimize_btn);
        mibClose = (ImageButton) findViewById(R.id.close_btn);
        mibDelete = (ImageButton) findViewById(R.id.ok_btn);
        mbtnEditOK = (Button) findViewById(R.id.edit_ok_btn);
        mibReply = (ImageButton) findViewById(R.id.btn_cancel);
        mbtnEditCancel = (Button) findViewById(R.id.edit_cancel_btn);
        metEditContent = (EditText) findViewById(R.id.sms_edit_content);
//        metInputPasswd = (EditText) findViewById(R.id.sms_input_passwd);
        mGallery = (Gallery) findViewById(R.id.gallery);
        mibBack.setOnClickListener(this);
        mibMinimize.setOnClickListener(this);
        mibClose.setOnClickListener(this);
        mibReply.setOnClickListener(this);
        mibDelete.setOnClickListener(this);
        mbtnEditCancel.setOnClickListener(this);
        mbtnEditOK.setOnClickListener(this);
		mrlTitle.setOnTouchListener(this);
		mrlTitle.setOnLongClickListener(this);
		/*try {
			Context multiTaskContext = context.createPackageContext
				("com.malatamobile.laiyang.floatmultitask"
				  	,Context.CONTEXT_IGNORE_SECURITY);

			SharedPreferences sp = multiTaskContext.
				  	getSharedPreferences("status", Context.MODE_WORLD_READABLE);
			if(sp.getBoolean("isAutoShowSms", false)) {
			 	Log.i("haha", "isAutoShowSms true");
			} else {
			    Log.i("haha", "isAutoShowSms false");
			}
		} catch(Exception e) {
			Log.i("haha", "read sp error");
			e.printStackTrace();
		}*/
        smsDatePattern = "yyyy" + mContext.getString(R.string.note_time_year) +
            "MM" + mContext.getString(R.string.note_time_month) +
            "dd" + mContext.getString(R.string.note_time_day);
        mSms = new ArrayList<>();
//        new Thread(this).start();
        initSmsData();
        mSmsAdapter = new SmsGalleryAdapter();
        mGallery.setAdapter(mSmsAdapter);
        updateUI();
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_in);
        mrlMsgWholeView.setAnimation(anim);

    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_MSGS:
                    updateUI();
                    break;
                case MSG_SEND_SMS_SUCCESS:
					closeSoftInput(mContext, metEditContent);
                    mtvResendNotify.setVisibility(View.GONE);

					mibClose.setVisibility(View.GONE);
            		mibMinimize.setVisibility(View.VISIBLE);
            		updateControlButtonsVisiblity(View.VISIBLE);
            		updateEditButtonsVisiblity(View.GONE);
            		mrlEditModeContent.setVisibility(View.GONE);
           			mGallery.setVisibility(View.VISIBLE);
            		metEditContent.setText("");
            		mtvContact.setText("");
                    break;
                case MSG_SEND_SMS_FAIL:
                    mtvResendNotify.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    private void updateUI() {
        if(mSms.size() != 0) {
            mtvEmptySms.setVisibility(View.GONE);
            updateUnReadSms();
            mSmsAdapter.notifyDataSetChanged();
            mrlSmsBottom.setVisibility(View.VISIBLE);
            updateEditButtonsVisiblity(View.GONE);
            mibClose.setVisibility(View.GONE);
            mrlGalleryLayout.setVisibility(View.VISIBLE);
        } else {
            mtvEmptySms.setVisibility(View.VISIBLE);
            mibClose.setVisibility(View.GONE);
            mrlSmsBottom.setVisibility(View.GONE);
            mrlGalleryLayout.setVisibility(View.GONE);
        }
    }


    private void updateUnReadSms() {
        unReadNum = SmsContent.getNewSmsCount(mContext);
        if(unReadNum > 0) {
            mtvUnreadNum.setText(unReadNum+"");
            mtvUnreadNum.setVisibility(View.VISIBLE);

        } else {
            mtvUnreadNum.setVisibility(View.GONE);
        }
    }

    private void updateEditButtonsVisiblity(int visible) {
        mbtnEditOK.setVisibility(visible);
        mbtnEditCancel.setVisibility(visible);
    }

    private void updateControlButtonsVisiblity(int visible) {
        mibDelete.setVisibility(visible);
        mibReply.setVisibility(visible);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(mibBack)) {
			closeSoftInput(mContext, metEditContent);
//            Intent intent = new Intent(getContext(), BackTaskService.class);
//            getContext().stopService(intent);
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
               	    Intent intent = new Intent("com.malata.floatmultitask.action.showmainwindow");
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					mContext.sendBroadcast(intent);

                    mrlMsgWholeView.setVisibility(View.GONE);
                    SmsFloatManager.removeMsgWindow(getContext());
                   // SmsFloatManager.createMainWindow(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mrlMsgWholeView.startAnimation(anim);
        } else if(v.equals(mibMinimize)) {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.floating_main_view_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                	Intent intent = new Intent("com.malata.floatmultitask.action.showfloatbutton");
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					mContext.sendBroadcast(intent);

                    mrlMsgWholeView.setVisibility(View.GONE);
                    SmsFloatManager.removeMsgWindow(getContext());
                   // SmsFloatManager.createFLoatButton(getContext());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mrlMsgWholeView.startAnimation(anim);

        } else if(v.equals(mibDelete)) {
           /* Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings");
            intent.setComponent(cn);
            intent.putExtra(":android:show_fragment", "com.android.settings.applications.AppOpsSummary");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);*/
    /*        Intent intent = new Intent();
           intent.setClassName("com.android.settings",
                     "com.android.settings.Settings");
             intent.setAction(Intent.ACTION_MAIN);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
             | Intent.FLAG_ACTIVITY_CLEAR_TASK
               | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                "com.android.settings.applications.AppOpsSummary");
            mContext.startActivity(intent);*/

            if(0 == mSms.size()) {
                return;
            }
            SmsInfo sms = mSms.get(mGallery.getFirstVisiblePosition());
            deleteSms(sms.getId(), sms.getThreadId());
            initSmsData();
            mHandler.sendEmptyMessage(MSG_GET_MSGS);
        } else if(v.equals(mibClose)) {
 	        closeSoftInput(mContext, metEditContent);
            mibClose.setVisibility(View.GONE);
            mibMinimize.setVisibility(View.VISIBLE);
            updateControlButtonsVisiblity(View.VISIBLE);
            updateEditButtonsVisiblity(View.GONE);
            mrlEditModeContent.setVisibility(View.GONE);
            mGallery.setVisibility(View.VISIBLE);
            metEditContent.setText("");
            mtvContact.setText("");
        } else if(v.equals(mibReply)) {
            mibClose.setVisibility(View.VISIBLE);
            mibMinimize.setVisibility(View.GONE);
            updateControlButtonsVisiblity(View.GONE);
            updateEditButtonsVisiblity(View.VISIBLE);
            mGallery.setVisibility(View.GONE);
            mrlEditModeContent.setVisibility(View.VISIBLE);
            mtvContact.setText(mSms.get(mGallery.getFirstVisiblePosition()).getPhoneNumber());

        } else if(v.equals(mbtnEditCancel)) {
        	closeSoftInput(mContext, metEditContent);
            mibClose.setVisibility(View.GONE);
            mibMinimize.setVisibility(View.VISIBLE);
            updateControlButtonsVisiblity(View.VISIBLE);
            updateEditButtonsVisiblity(View.GONE);
            mrlEditModeContent.setVisibility(View.GONE);
            mGallery.setVisibility(View.VISIBLE);
            metEditContent.setText("");
            mtvContact.setText("");
        } else if(v.equals(mbtnEditOK)) {
            String content = metEditContent.getText().toString();
            String number = mtvContact.getText().toString();
            if(null == content || "".equals(content) || null == number || "".equals(number)) {
                mHandler.sendEmptyMessage(MSG_SEND_SMS_FAIL);
                return;
            }
            sendMessage(number, content);
        }
    }

    private void sendMessage(String number, String content) {
        String sent = "sms_sent";
        String delivered = "sms_delivered";
        PendingIntent sentPI = PendingIntent.getActivity(mContext, 0, new Intent(sent), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(mContext, 0, new Intent(delivered), 0);

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        mHandler.sendEmptyMessage(MSG_SEND_SMS_SUCCESS);
                        Log.i("haha", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("haha", "RESULT_ERROR_GENERIC_FAILURE");
                        mHandler.sendEmptyMessage(MSG_SEND_SMS_FAIL);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("haha", "RESULT_ERROR_NO_SERVICE");
                        mHandler.sendEmptyMessage(MSG_SEND_SMS_FAIL);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("haha", "RESULT_ERROR_NULL_PDU");
                        mHandler.sendEmptyMessage(MSG_SEND_SMS_FAIL);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("haha", "RESULT_ERROR_RADIO_OFF");
                        mHandler.sendEmptyMessage(MSG_SEND_SMS_FAIL);
                        break;
                }
            }
        }, new IntentFilter(sent));

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("=====>", "RESULT_CANCELED");
                        break;
                }
            }
        }, new IntentFilter(delivered));

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, content, sentPI, deliveredPI);
    }

    private void deleteSms(int id, int threadId) {
        ContentResolver cr = mContext.getContentResolver();
//        Cursor cursor = cr.query(Uri.parse("content://sms"),new String[]{"thread_id"}, "_id = "+id , null, null);//
//        cursor.moveToFirst();
//        String thread = cursor.getString(cursor.getColumnIndex("thread_id"));
        Uri uri = Uri.parse("content://sms");
        int row = cr.delete(uri,"_id = " + id, null);
        if(row == 1) {
            Log.i("haha", "delete success");
        } else {
            Log.i("haha", "delete faild"+ " "+ row);
        }

    }


    @Override
    public void run() {
        mSms.clear();
        mSms.addAll(SmsContent.getSmsInfo(getContext(), Uri.parse("content://sms/inbox/")));
        mHandler.sendEmptyMessage(MSG_GET_MSGS);
    }

    private void initSmsData() {
        mSms.clear();
        mSms.addAll(SmsContent.getSmsInfo(getContext(), Uri.parse("content://sms/inbox/")));
//        mHandler.sendEmptyMessage(MSG_GET_MSGS);
    }

    private class SmsGalleryAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public SmsGalleryAdapter() {
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return mSms.size();
        }

        @Override
        public Object getItem(int position) {
            return mSms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cache cache = null;
            SmsInfo sms = mSms.get(position);
            if(convertView == null) {
                cache = new Cache();
                convertView = inflater.inflate(R.layout.sms_popup_item, null);
                cache.tvContact = (TextView) convertView.findViewById(R.id.contact);
                cache.tvContent = (EditText) convertView.findViewById(R.id.sms_content);
                cache.tvDate = (TextView) convertView.findViewById(R.id.date_time);
                cache.tvEntrpted = (TextView) convertView.findViewById(R.id.entrpted);
                cache.tvPageNum = (TextView) convertView.findViewById(R.id.page_num);
                convertView.setTag(cache);
            } else {
                cache = (Cache) convertView.getTag();
            }
            cache.tvContact.setText(sms.getPhoneNumber());
            cache.tvDate.setText(TimFormatUtils.getTimeinMills(smsDatePattern, Long.parseLong(sms.getDate())));
            cache.tvContent.setText(sms.getSmsbody());
            cache.tvPageNum.setText((position+1)+"/"+(mSms.size()));
            return convertView;
        }

        private class Cache {
            TextView tvContact;
            TextView tvDate;
            EditText tvContent;
            TextView tvEntrpted;
            TextView tvPageNum;
        }
    }

	@Override
    public boolean onTouch(View v, MotionEvent event) {

        if(isLongClick) {
            touchX = event.getX();
            touchY = event.getY();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "action down");
                touchX = event.getX();
                touchY = event.getY();

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case  MotionEvent.ACTION_MOVE:
                Log.d(TAG, "preX:"+mParams.x);
                Log.d(TAG, "preY:"+mParams.y);
                Log.d(TAG, "nowY:"+event.getRawX());
                Log.d(TAG, "nowY:"+event.getRawY());
                Log.d(TAG, "downX:"+touchX);
                Log.d(TAG, "downY:"+touchY);

                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                mParams.x = (int) (xInScreen - touchX);
                mParams.y = (int) (yInScreen - touchY) ;
                windowManager.updateViewLayout(this, mParams);
                Log.i(TAG, "action_move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "action up");
                isLongClick = true;
                mrlTitle.setOnLongClickListener(this);
//                mrlTitle.setOnTouchListener(null);
                break;
        }
        return false;
    }

    private int getStatusBarHeight(){
        if(statusBarHeight == 0){
            try{
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    private boolean isLongClick = true;

    @Override
    public boolean onLongClick(View v) {
        Vibrator vibrator=(Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0,50}, -1);
        mrlTitle.setOnLongClickListener(null);
        isLongClick = false;
        return false;
    }

	private void closeSoftInput(Context context, EditText et) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromInputMethod(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
