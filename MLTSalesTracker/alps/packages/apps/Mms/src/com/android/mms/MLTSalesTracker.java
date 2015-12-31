package com.android.mms;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.mediatek.telephony.TelephonyManagerEx;
import android.provider.Telephony;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.telephony.TelephonyManager;//aoran
import android.os.SystemClock;


//Add by linpeicai@malatamobile.com for SFZONPCL-71 20140324 begin
public class MLTSalesTracker extends Service {

	private static final String TAG = "MLTSalesTracker";
	private mServiceReceiver mReceiver01;//, mReceiver02;
	//the destination number.
	private static final String strDestAddress = "13751175645";
	private static final long delayMillis = 5*60*1000L;
	SharedPreferences prefs;

	/* ACTION custom constants, as Intent Filter identify constant broadcast */
	private static String SMS_SEND_ACTIOIN = "SMS_SEND_ACTIOIN";
	private static String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";

	public static final String MALATA_NAME = "malata_name";
	public static final String MALATA_SALES_TRACKER = "malata_sales_tracker";

	private static String ALARM_ACTION = "com.malata.broadcast.salestracker.timeout";
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate() = ");
		mReceiver01 = new mServiceReceiver();
		IntentFilter mFilter01 = new IntentFilter();
        mFilter01.addAction(SMS_SEND_ACTIOIN);
	    mFilter01.addAction(SMS_DELIVERED_ACTION); 
	    mFilter01.addAction(ALARM_ACTION); 		
        registerReceiver(mReceiver01, mFilter01);
		
		prefs = getSharedPreferences(MALATA_NAME, Context.MODE_PRIVATE);
		int result = prefs.getInt(MALATA_SALES_TRACKER, 1);
		Log.i(TAG, "[onCreate]result:" + result);

		if (result == 1) {
			//handler.postDelayed(runnable, delayMillis);
			AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;			
			Intent intentToFire = new Intent(ALARM_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
			//alarms.set(alarmType, SystemClock.elapsedRealtime()+delayMillis, pendingIntent);
			alarms.setRepeating(alarmType, SystemClock.elapsedRealtime()+ delayMillis, delayMillis, pendingIntent);
			Log.d(TAG,"---Begin alarms--- SystemClock.elapsedRealtime()="+SystemClock.elapsedRealtime());
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.i(TAG, "onStart() ");
	}

	@Override
	public void onDestroy() {
		/* Unregister custom Receiver */
		unregisterReceiver(mReceiver01);
		super.onDestroy();
		Log.i(TAG, "onDestroy() ");
	}

	   //zhaoxy add for android 4.4 start
	   private boolean isDefaultSms(Context context){
        boolean isDefault=false;
        try {
            int currentapiVersion=android.os.Build.VERSION.SDK_INT;
            if(currentapiVersion>=android.os.Build.VERSION_CODES.KITKAT){
                String defaultSmsApplication = Telephony.Sms.getDefaultSmsPackage(context);
                if (defaultSmsApplication != null && defaultSmsApplication.equals("com.android.mms")) {
                    isDefault=true;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return isDefault;
    }

	 //aoran 20141023 modfiy for MLTSalesTacker,begin
	public boolean isCanUseSim() { 
	    try { 
	        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
	 
	        return TelephonyManager.SIM_STATE_READY == mgr 
	                .getSimState(); 
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	    return false; 
	} 
	 //aoran 20141023 modfiy for MLTSalesTacker,end
	 
	/*
	 * Custom mServiceReceiver rewrite BroadcastReceiver listening SMS status
	 * messages
	 */
	public class mServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(SMS_SEND_ACTIOIN)) {
				try {
					/* android.content.BroadcastReceiver.getResultCode() method */
					// Retrieve the current result code, as set by the previous
					// receiver.
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						/* SMS success */
						Log.i(TAG, "SMS success ");
						AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						Intent intentToFire = new Intent(ALARM_ACTION);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToFire, 0);
						alarms.cancel(pendingIntent);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt(MALATA_SALES_TRACKER, 0);
						editor.commit();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						/* Failed to send text messages */
						Log.i(TAG, "Failed to send text messages ");
						//aoran 20141110 modify delele repeat sending messages.
						//AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						//int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
						
						//Intent intentToFire = new Intent(ALARM_ACTION);
						//PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToFire, 0);
						//alarms.set(alarmType, delayMillis, pendingIntent); 
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						break;
					}
				} catch (Exception e) {
					e.getStackTrace();
				}
			} else if (intent.getAction().equals(SMS_DELIVERED_ACTION)) {
				try {
					/* android.content.BroadcastReceiver.getResultCode() method */
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						/* SMS has been delivered */
						Log.i(TAG, "SMS has been delivered ");
						AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						Intent intentToFire = new Intent(ALARM_ACTION);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToFire, 0);
						alarms.cancel(pendingIntent);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt(MALATA_SALES_TRACKER, 0);
						editor.commit();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						/* SMS is not delivered */
						Log.i(TAG, "SMS is not delivered");
						//aoran 20141110 modify delele repeat sending messages.
						//AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
						//int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
						
						//Intent intentToFire = new Intent(ALARM_ACTION);
						//PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentToFire, 0);
						//alarms.set(alarmType, delayMillis, pendingIntent); 
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						break;
					}
				} catch (Exception e) {
					e.getStackTrace();
				}
			}else if(intent.getAction().equals(ALARM_ACTION)){
							TelephonyManagerEx tmEx = TelephonyManagerEx.getDefault();
			if (tmEx != null) {
				Log.i(TAG, "runnable  handler");
				String imei = tmEx.getDeviceId(0);
				Log.i(TAG, "run() imei  = " + imei);
				String number0 = tmEx.getLine1Number(0);
				String number1 = tmEx.getLine1Number(1);
				Log.i(TAG, "run() number0 = " + number0 + " number1 = "	+ number1);
				String number = null;
				if (!TextUtils.isEmpty(number0)) {
					number = number0;
				} else if (TextUtils.isEmpty(number0) && !TextUtils.isEmpty(number1)) {
					number = number1;
				} else if (!TextUtils.isEmpty(number0) && !TextUtils.isEmpty(number1)) {
					number = number0;
				}
				String body = "TTK" + " " + imei + " " + "K15";
				Log.i(TAG, "run() body  = " + body);
				Log.i(TAG, "isCanUseSim  = " + isCanUseSim());//aoran add
				if ((!TextUtils.isEmpty(number) || isCanUseSim()/*aoran add isCanUseSim*/)&&isDefaultSms(getApplicationContext())) {
					/*
					 * Build custom Action constants Intent (PendingIntent
					 * parameters to use)
					 */
					Intent itSend = new Intent(SMS_SEND_ACTIOIN);
					Intent itDeliver = new Intent(SMS_DELIVERED_ACTION);

					/*
					 * sentIntent parameters acceptable to transmit broadcast
					 * information PendingIntent
					 */
					PendingIntent mSendPI = PendingIntent.getBroadcast(
							getApplicationContext(), 0, itSend, 0);

					/*
					 * deliveryIntent parameters after the service to receive
					 * broadcast information PendingIntent
					 */
					PendingIntent mDeliverPI = PendingIntent.getBroadcast(
							getApplicationContext(), 0, itDeliver, 0);

					Log.i(TAG,"sms sent flag");	//aoran add log	
					SmsManager.getDefault().sendTextMessage(strDestAddress,
							null, body, mSendPI, mDeliverPI);
					Log.e(TAG,"sms sent");		
				}
			}
		}
	}
}
}
//Add by linpeicai@malatamobile.com for SFZONPCL-71 20140324 end
