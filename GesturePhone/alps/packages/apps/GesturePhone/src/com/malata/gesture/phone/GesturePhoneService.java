package com.malata.gesture.phone; 
 
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.FloatMath;
import com.malata.gesture.phone.ShellExe;
import java.io.IOException;
import android.view.IWindowManager;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.media.AudioManager;

public class GesturePhoneService extends Service{

    private static final String TAG = "GesturePhoneService";

    public static final String PHONE_RINGER_SILENT_ACTION =
            "android.intent.action.ACTION_PHONE_RINGER_SILENT";
    public static final String PHONE_SPEAKER_ON_ACTION =
            "android.intent.action.ACTION_PHONE_SPEAKER_ON";
    public static final String PHONE_SNOOZE_ALARM_ACTION =
            "android.intent.action.ACTION_PHONE_SNOOZE_ALARM";
    public static final String PHONE_MOVE_ON_ACTION =
            "android.intent.action.ACTION_PHONE_MOVE_ON";
	
    private static final float SETZ_SILENT = -7;
    private static final float SETZ_SPEAKER = -8;
    private static final float UPRIGHT_THRE_MIN = -3;
    private static final float UPRIGHT_THRE_MAX = 3;
    
    private boolean phoneRingingState = false;
    private boolean mUprightState = false;

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer;
    private Sensor mPromixySensor;
    private Sensor mOrientationSensor;
    private AccelerometerSilentListener mAccelerometerSilentListener;
   
    private SensorEventListener mAccelerometerSpeakerListener;

    private AccelerometerMoveOnListener mAccelerometerMoveOnListener;
	    
    private TelephonyManager mTelephonyManager = null;
    private GesturePhoneStateListener mGesturePhoneStateListener = null;
        

    private enum Sensors {
        SENSORS_ALL,
        SENSORS_SILENT,
        SENSORS_SPEAKER,
        SENSORS_MOVE_ON,
        SENSORS_TEST,
    }
   
    // MOVE_OK / MOVE_REVIEW > 50% ?
    private static final int   MOVE_OK = 5;
    private static final int   MOVE_REVIEW = 10;
    private static final float MOVE_THRESTHOLD = (float)0.4; // 2013.6.5, 0.5-->0.4
    private static final int   QUICK_MOVE_REVIEW = 3;
    private static final float QUICK_MOVE_THRESTHOLD = (float)5;    
    private static final float RADIANS_TO_DEGREES = (float) (180 / Math.PI);
    private static final float LONG_FACE_UP_ANGLE = 45;
    private static final float EVER_FACE_UP_ANGLE = 0;
    private static final float LONG_FACE_DOWN_ANGLE = -45;
    private static final float NOW_FACE_DOWN_ANGLE = -45;
    private static final float NOW_FACE_DOWN_CNT = 3;    
    private static final long  FLAT_TIME_MS = 500;      // 0.5 second
    private static final long  MOVE_TIME_MS = 1200;     // 1.2 seconds  
    private static final long  MAX_TIME_MS = 5000;      // 5 seconds  
    private static final long  NANOS_PER_MS = 1000000;  // 1 ms = 1000000 nano second
    
    private static final boolean USE_GRAVITY_SENSOR = false;

    // for more info, pls c "auto\bma222e.c", DRIVER_ATTR(noearly, ...
    private static final String  GSENSOR_NOEARLY = "/sys/devices/platform/gsensor/driver/noearly";
    private static final int     RESULT_FAIL = -1;
   
    // History of observed a1.
    private int                 mMoveHistoryIndex;
    private static final int    MOVE_HISTORY_SIZE = 40;

	private int currentRingToneValue = 0;
	
    private float[]             mA1History     = new float[MOVE_HISTORY_SIZE];
    private int[]               mTxHistory     = new int[MOVE_HISTORY_SIZE];
    private int[]               mTyHistory     = new int[MOVE_HISTORY_SIZE];
    private int[]               mTzHistory     = new int[MOVE_HISTORY_SIZE];
    private long[]              mMoveTimeNanos = new long[MOVE_HISTORY_SIZE];

    private ContentResolver cr;

    private SensorEventListener mGsensorTestListner;
    private Vibrator            mVibrator; 


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mGesturePhoneStateListener = new GesturePhoneStateListener();
        mTelephonyManager.listen(mGesturePhoneStateListener,
                                 PhoneStateListener.LISTEN_CALL_STATE);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mPromixySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mAccelerometer = mSensorManager.getDefaultSensor(USE_GRAVITY_SENSOR
                         ? Sensor.TYPE_GRAVITY : Sensor.TYPE_ACCELEROMETER); 
                        
        mAccelerometerSilentListener = new AccelerometerSilentListener();
        mAccelerometerSpeakerListener = new AccelerometerSpeakerListener();
		mAccelerometerMoveOnListener = new AccelerometerMoveOnListener();

        cr = getContentResolver();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        mGsensorTestListner = new GsensorTestListner(); 
    }


    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        
        enableSensors(Sensors.SENSORS_ALL, false);
    }
    

    public void vibrate() {
        if (null != mVibrator && mVibrator.hasVibrator()) {
            mVibrator.vibrate(new long[] { 100, 100 }, -1);
        } else {
            Log.v(TAG, "no vibrator?");
        }
    }


    private void enableSensors(Sensors sensors, boolean bEnable) {
        if (null == mSensorManager) {
            Log.e(TAG, "OMG null p!!"); 
            return;
        }
       
        if (bEnable) {
            switch (sensors) {
                case SENSORS_ALL:
                    break;
                case SENSORS_SILENT:
                    mSensorManager.registerListener(mAccelerometerSilentListener,
                        mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                case SENSORS_SPEAKER:
                    mSensorManager.registerListener(mAccelerometerSpeakerListener,
                        mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    break;
				case SENSORS_MOVE_ON:
					mSensorManager.registerListener(mAccelerometerMoveOnListener,
						mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
					break;
                case SENSORS_TEST:
                    mSensorManager.registerListener(mGsensorTestListner,
                        mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                    
            }
        }
        else {
            switch (sensors) {
                case SENSORS_ALL:
                    mSensorManager.unregisterListener(mAccelerometerSilentListener);
                    mAccelerometerSilentListener.resetZ();
                    mSensorManager.unregisterListener(mAccelerometerSpeakerListener);
                    mSensorManager.unregisterListener(mAccelerometerMoveOnListener);
                    mSensorManager.unregisterListener(mGsensorTestListner);
                    mUprightState = false;
                    break;                    
                case SENSORS_SILENT:
                    mSensorManager.unregisterListener(mAccelerometerSilentListener);
                    mAccelerometerSilentListener.resetZ();
                    break;
                case SENSORS_SPEAKER:
                    mSensorManager.unregisterListener(mAccelerometerSpeakerListener);
                    break;
				case SENSORS_MOVE_ON:
					mSensorManager.unregisterListener(mAccelerometerMoveOnListener);
					mAccelerometerMoveOnListener.resetZ();
					break;
                case SENSORS_TEST:
                    mSensorManager.unregisterListener(mGsensorTestListner);
                    break;
           }
        }
    }


    private void regSettingsObservers(boolean bReg) {
        Log.v(TAG, " in regSettingsObservers, bReg = " + bReg);
        if (bReg) {
            cr.registerContentObserver(
                    Settings.Global.getUriFor(Settings.System.DEF_SMART_ACTION),
                    true, mGestureObserver);
            
            /*cr.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.PROMIXY_DIAL_PHONE_ENABLED),
                    true, mProxiySnapObserver);*/
        }
        else {
            // cr.unregisterContentObserver(mGestureObserver);
            // cr.unregisterContentObserver(mProxiySnapObserver);
        } 
    }


    private ContentObserver mGestureObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.v(TAG, " ENABLE_GESTURE_SETTINGS_ENABLED change");
            //refreshDialOn();
        }
    };


    private ContentObserver mProxiySnapObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.v(TAG, " PROMIXY_DIAL_PHONE_ENABLED change");
            //refreshDialOn();
        }
    };




    private class GesturePhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
		   
            boolean bUpsetSilentOn = 1==Settings.Secure.getInt(cr, 
                                            Settings.System.DEF_SMART_ACTION,
                                            Settings.Secure.DEF_FLIP_TO_SILENCE, 0) ;
            boolean bUpsetSpeakerOn = 1==Settings.Secure.getInt(cr,
                                           Settings.System.DEF_SMART_ACTION,
                                            Settings.Secure.DEF_FLIP_TO_SPEAKER, 0);
            boolean bUpsetSnoozeOn = 1==Settings.Secure.getInt(cr,
                                            Settings.System.DEF_SMART_ACTION,
                                            Settings.Secure.DEF_FLIP_TO_SNOOZE_ALARM, 0);
            boolean bRingOnMoveOn = 1==Settings.Secure.getInt(cr,
                                            Settings.System.DEF_SMART_ACTION,
                                            Settings.Secure.DEF_DECREASE_RING_ON_MOVE, 0);

            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.v(TAG, "CALL_STATE_IDLE");                
                phoneRingingState = false;

				if(bRingOnMoveOn)
					{
					AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					int max = audioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
					int current = audioManager.getStreamVolume( AudioManager.STREAM_RING );
					Log.d("aoran","CALL_STATE_IDLE max :"+ max +"current :"+ current);
					if(currentRingToneValue != 0)
					{
						audioManager.setStreamVolume(AudioManager.STREAM_RING, getCurrentRingToneValue(), 0);
					}
					//saveCurrentRingToneValue(max);
					}
                enableSensors(Sensors.SENSORS_SILENT, false);
                enableSensors(Sensors.SENSORS_MOVE_ON, false);			
                enableSensors(Sensors.SENSORS_SPEAKER, false);


                Log.v(TAG, "testT, CALL_STATE_IDLE--setGsensorNoEarlySuspen d(false)");
                setGsensorNoEarlySuspend(false);                
                break;
                
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.v(TAG, "CALL_STATE_OFFHOOK");
                phoneRingingState = false;

                enableSensors(Sensors.SENSORS_SILENT, false);
                enableSensors(Sensors.SENSORS_MOVE_ON, false);

                if (bUpsetSpeakerOn) {
                    enableSensors(Sensors.SENSORS_SPEAKER, true); 

                    Log.v(TAG, "testT, CALL_STATE_OFFHOOK--setGsensorNoEarlySuspen d(true)");
                    setGsensorNoEarlySuspend(true); 
                }
                                
                break;
                
            case TelephonyManager.CALL_STATE_RINGING:
                Log.v(TAG, "CALL_STATE_RINGING");
                phoneRingingState = true;
				
                if (bUpsetSilentOn) {
                    Log.v(TAG, "aoranslt, CALL_STATE_RINGING");
                    enableSensors(Sensors.SENSORS_SILENT, true);
                }
                if (bRingOnMoveOn) {
                    Log.v(TAG, "aoranmov, CALL_STATE_RINGING");
					
					AudioManager audioManagerInRing = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					//int max = audioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
					int currentInRing = audioManagerInRing.getStreamVolume( AudioManager.STREAM_RING );
					Log.d("Aoran","CALL_STATE_RINGING current :"+ currentInRing);
					saveCurrentRingToneValue(currentInRing);
					//audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
					
                    enableSensors(Sensors.SENSORS_MOVE_ON, true);
                }
				

                Log.v(TAG, "testT, bUpsetSilentOn = " + bUpsetSilentOn
                      //+ ", bProxSpeakerOn = " + bProxSpeakerOn
                      + ", setGsensorNoEarlySuspen d()");
                if (bUpsetSilentOn) {//|| bUpsetSpeakerOn
                    Log.v(TAG, "testT, CALL_STATE_RINGING--setGsensorNoEarlySuspen d(true)");
                    setGsensorNoEarlySuspend(true); //  ++, Gsensor No early suspend
                }
                else {
                    Log.v(TAG, "testT, for safe, CALL_STATE_RINGING--setGsensorNoEarlySuspen d(false)");
                    setGsensorNoEarlySuspend(false); //  ++, Gsensor early suspend
                }
                
                break;
            }
        }
    };
    

	private void saveCurrentRingToneValue(int value ){
		
			currentRingToneValue = value;
	}

	private int getCurrentRingToneValue(){

			return currentRingToneValue;
	}
	
    private void broadcastRingerSilentToPhone() {
        Log.v(TAG, "broadcastRingerSilentToPhone");
        Intent broadcast = new Intent(PHONE_RINGER_SILENT_ACTION);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        sendBroadcast(broadcast);
    }

	
    private void broadcastSpeakerOnToPhone(boolean speakerOn) {
        Log.v(TAG, "boadcasetSpeakerOnToPhone");
        Intent broadcast = new Intent(PHONE_SPEAKER_ON_ACTION);
        broadcast.putExtra("speakerOn", speakerOn);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        sendBroadcast(broadcast);
    }

    private void broadcastSnoozeAlarm() {
        Log.v(TAG, "broadcastSnoozeAlarm");
        Intent broadcast = new Intent(PHONE_SNOOZE_ALARM_ACTION);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        sendBroadcast(broadcast);
    }

    private void broadcastMoveOn() {
        Log.v(TAG, "broadcastVolDownOnMove");
        Intent broadcast = new Intent(PHONE_MOVE_ON_ACTION);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        sendBroadcast(broadcast);
    }
	


    private int nextMoveHistoryIndex(int index) {
        index = (index == 0 ? MOVE_HISTORY_SIZE : index) - 1;
        
        return mMoveTimeNanos[index] != Long.MIN_VALUE ? index : -1;
    }

    public int recordMovePoint(SensorEvent event) {
        /// BEGIN, Movement hitstory
        float       aX = event.values[SensorManager.DATA_X];
        float       aY = event.values[SensorManager.DATA_Y];
        float       aZ = event.values[SensorManager.DATA_Z];
        final long  now = event.timestamp / NANOS_PER_MS;
        final float aXYZ = FloatMath.sqrt(aX * aX + aY * aY + aZ * aZ);
        final float a1 = Math.abs(aXYZ - (float)9.81); // sometimes it doesn't work

        // Calculate the tilt angle.
        // This is the angle between the up vector and the x-y plane (the plane of
        // the screen) in a range of [-90, 90] degrees.
        //   -90 degrees: screen horizontal and facing the ground (overhead)
        //     0 degrees: screen vertical
        //    90 degrees: screen horizontal and facing the sky (on table)
        int tiltZ = 0;
        int tiltY = 0;
        int tiltX = 0;

        if (0 != aXYZ) {
            tiltZ = (int) Math.round(Math.asin(aZ / aXYZ) * RADIANS_TO_DEGREES);
            tiltY = (int) Math.round(Math.asin(aY / aXYZ) * RADIANS_TO_DEGREES);
            tiltX = (int) Math.round(Math.asin(aX / aXYZ) * RADIANS_TO_DEGREES);
        }        

        if (false == isMovePointExist(now, a1, tiltZ)) {
            Log.v(TAG, "testT, recordMovePoint, mMoveHistoryIndex=" + mMoveHistoryIndex + ", now = " + now
                  + ", tAngle = (" + tiltX + ", " + tiltY + ", " + tiltZ + ")"
                  + ", a = (" + aX + ", " + aY + ", " + aZ + ")"
                  + ", aXYZ = " + aXYZ + ", a1 = " + a1
                  );
            addMoveHistoryEntry(now, a1, tiltX, tiltY, tiltZ);
        }
        return tiltZ;
    }

    private boolean isMovePointExist(long now, float a1, int tiltZ) {
        int    iHistory = 0;

        // Log.v(TAG, "isMovePointExis t");
        for (int i = mMoveHistoryIndex; 
            iHistory < MOVE_HISTORY_SIZE && (i = nextMoveHistoryIndex(i)) >= 0;
            iHistory++) {           
            if (a1 == mA1History[iHistory]
                && tiltZ == mTzHistory[iHistory]
                && now == mMoveTimeNanos[iHistory]) {
                Log.v(TAG, "isMovePointExis t, OK! iHistory = " + iHistory
                      + ", mMoveHistoryIndex = " + mMoveHistoryIndex);
                return true;
            }
        }            

        // Log.v(TAG, "isMovePointExist, NO K!");
        return false; 
    }

    private void addMoveHistoryEntry(long now, float a1, int tiltX, int tiltY, int tiltZ) {
        if (mMoveHistoryIndex >= MOVE_HISTORY_SIZE) {
            Log.v(TAG, "OMG, overflow!");
            return;
        }
        
        mA1History[mMoveHistoryIndex] = a1;
        mTxHistory[mMoveHistoryIndex] = tiltX;
        mTyHistory[mMoveHistoryIndex] = tiltY;
        mTzHistory[mMoveHistoryIndex] = tiltZ;
        mMoveTimeNanos[mMoveHistoryIndex] = now;
        mMoveHistoryIndex = (mMoveHistoryIndex + 1) % MOVE_HISTORY_SIZE;
        // mMoveTimeNanos[mMoveHistoryIndex] = Long.MIN_VALUE; DO NOT set to minus
    }

    private boolean isEverFaceUp() {
        int    iHistory = 0;

        Log.v(TAG, "isEverFaceUp mMoveHistoryIndex=" + mMoveHistoryIndex);
        for (int i = mMoveHistoryIndex; 
            iHistory < MOVE_HISTORY_SIZE && (i = nextMoveHistoryIndex(i)) >= 0;
            iHistory++) {           
            if ((mMoveTimeNanos[i] + MAX_TIME_MS) > mMoveTimeNanos[mMoveHistoryIndex] // Don't be too long ago
                && mTzHistory[i] > EVER_FACE_UP_ANGLE ) {
                Log.v(TAG, "isEverFaceUp, OK! tiltAngle[" + i + "] = " + mTzHistory[i]);
                return true;
            }
        }
        
        Log.v(TAG, "isFisEverFaceUpaceUp, NOK!");
        return false;
    }

    private boolean isLongFaceUp(long now) {
        int    iHistory = 0;
        
        Log.v(TAG, "isLongFaceU p");
        for (int i = mMoveHistoryIndex; 
            iHistory < MOVE_HISTORY_SIZE && (i = nextMoveHistoryIndex(i)) >= 0;
            iHistory++) {
            if ((mMoveTimeNanos[i] + MAX_TIME_MS) > mMoveTimeNanos[mMoveHistoryIndex] // Don't be too long ago
                && mTzHistory[i] < LONG_FACE_UP_ANGLE) {
                Log.v(TAG, "isLongFaceU p, NOK! tiltAngle=" + mTzHistory[i]);
                break;
            }
            if (mMoveTimeNanos[i] + FLAT_TIME_MS <= now) { // It's stable long enough?
                // Tilt has remained greater than LONG_FACE_UP_ANGLE for FLAT_TIME_MS.
                return true;
            }
        }
        Log.v(TAG, "isLongFaceU p, NOK!");
        return false;
    }

    private boolean isLongFaceDown(long now) {
        int    iHistory = 0;
        
        Log.v(TAG, "isLongFaceDow n");
        for (int i = mMoveHistoryIndex; 
            iHistory < MOVE_HISTORY_SIZE && (i = nextMoveHistoryIndex(i)) >= 0;
            iHistory++) {
            Log.v(TAG, "isLongFaceDow n, tiltAngle[" + i + "] = " + mTzHistory[i]);
            if ((mMoveTimeNanos[i] + MAX_TIME_MS) > mMoveTimeNanos[mMoveHistoryIndex] // Don't be too long ago
                && mTzHistory[i] > LONG_FACE_DOWN_ANGLE) {
                Log.v(TAG, "isLongFaceDow n, NOK! tiltAngle[" + i + "] = " + mTzHistory[i]);
                return false;
            }
            
            if (mMoveTimeNanos[i] + FLAT_TIME_MS <= now) { // This time may be too short!!!
                // Tilt has remained greater than LONG_FACE_DOWN_ANGLE for FLAT_TIME_MS.
                Log.v(TAG, "isLongFaceDow n, OK! iHistory=" + iHistory
                      + ", interval = " +  (now - mMoveTimeNanos[i]));
                return true;
            }
        }
        
        Log.v(TAG, "isLongFaceDow n, NOK!");
        return false;
    }


    private boolean isRecentEverFaceDown() { // for silient
        int    iHistory = 0;
        int    iNext = 0;
        
        // Log.v(TAG, "isRecentEverFaceDow n mMoveHistoryIndex=" + mMoveHistoryIndex);
        for (int i = mMoveHistoryIndex; 
             iHistory < MOVE_HISTORY_SIZE && (i = nextMoveHistoryIndex(i)) >= 0;
             iHistory++) {
            // Log.v(TAG, "isRecentEverFaceDow n, tiltAngle[" + i + "] = " + mTzHistory[i]);
            iNext = nextMoveHistoryIndex(i);
            
            if ((mMoveTimeNanos[i] + MAX_TIME_MS) > mMoveTimeNanos[mMoveHistoryIndex] // Don't be too long ago
                && iHistory <= NOW_FACE_DOWN_CNT)
            {
                if (mTzHistory[i] < NOW_FACE_DOWN_ANGLE
                    && (iNext >= 0 && mTzHistory[i] < mTzHistory[iNext]) ) // less than last time
                {                 
                    Log.v(TAG, "isRecentEverFaceDow n, tiltAngle[" + i + "] = " + mTzHistory[i] + ", OK! iHistory=" + iHistory);
                    return true;
                }
            }
            else {
                Log.v(TAG, "isRecentEverFaceDow n, NOK! iHistory=" + iHistory);
                return false; 
            }
        }
        
        Log.v(TAG, "isRecentEverFaceDow n, NOK! end iHistory=" + iHistory);
        return false;
    }    


    // Silent G ///////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private class AccelerometerSilentListener implements SensorEventListener{ //  Silent

        private float z = 0;
        private float az = 0;
        private boolean faceUp = false;
        private int   iFaceUpCnt = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {

            az = event.values[SensorManager.DATA_Z];
            float ax = event.values[SensorManager.DATA_X];
            float ay = event.values[SensorManager.DATA_Y];
			Log.d("aoran", "AccelerometerSilent, az= " + az + ", z = " + z + ", ax = " + ax +", ay = " + ay);
            if (phoneRingingState) {
                if (z > 0 && az > 0) {
                	Log.v(TAG, "silent face up");
                    faceUp = true;
                }

                Log.d("tui", "AccelerometerSilent, az= " + az + ", z = " + z);
                if ((z > -16 && z < 0 && az < SETZ_SILENT && az > -16)
                        && (ax > -3.5 && ax < 3.5)
                        && (ay > -1 && ay < 4.1 )
                        && faceUp) {
                    faceUp = false;
                    broadcastRingerSilentToPhone();
                }
                z = az;
            }
        }
        
        public void resetZ() {
            z = 0;
            az = 0;
            faceUp = false;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
        
    }

    // Silent G ///////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private class AccelerometerMoveOnListener implements SensorEventListener{ //  Silent

		
        private boolean firstTime = true;
        private boolean cntFlag = false;
		private float z = 0;
		private float az = 0;
		private float cntX = 0;
		private float cntY = 0;
		private float cntZ = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {

            az = event.values[SensorManager.DATA_Z];
            //float ax = event.values[SensorManager.DATA_X];
            //float ay = event.values[SensorManager.DATA_Y];
            /*if (!cntFlag) {
					cntX = ax;
					cntY = ay;
					cntZ = az;
                    cntFlag = true;
                }
			float absX = Math.abs(cntX-ax);
			float absY = Math.abs(cntY-ay);
			float absZ = Math.abs(cntZ-az);		*/	

                //Log.d("tui", "AccelerometerMoveOnListener, absX= " + absX + ", cntY = " + cntY + ", absZ = "+absZ);
                /*if ((absX > 2 && absY > 2 && absZ > 2) && firstTime) {
                    firstTime = false;
                    broadcastMoveOn();
                }
                z = az;*/
            //  END --, tilt angle will be better
            	Log.d("tui", "AccelerometerMoveOnListener, az= " + az);
				if((az > -7 && az <7) && firstTime) {
					firstTime = false;
					broadcastMoveOn();
				}
			
        }
        
        public void resetZ() {
            az = 0;
            firstTime = true;
			cntFlag = false;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
        
    }

//aoran add end

    // Speaker G ///////////////////////////////////////////////////////////////////////////////////////////////////////
   
    private class AccelerometerSpeakerListener implements SensorEventListener{ // ing
        
        private float   z = 0;
        private float   az = 0;
        private boolean faceUp = false;
        private int     iCnt = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            ///  BEGIN --, tilt angle will be better
            Log.v(TAG, "t, off hook, onGSensorChanged, iCnt = " + iCnt);
            recordMovePoint(event); //  ++, Movement hitstory

            if (isLongFaceDown(event.timestamp / NANOS_PER_MS)) {
                if (iCnt > 0) {
                    iCnt = 0;
                }

                if (iCnt > -2) {
                    Log.v("aoran", "t, broadcastSpeakerOnToSpeaker");
                    broadcastSpeakerOnToPhone(true);
                    iCnt--;
                }
            }
            else if (isLongFaceUp(event.timestamp / NANOS_PER_MS)) {
                if (iCnt < 0) {
                    iCnt = 0;
                }

                if (iCnt < 2) {
                    Log.v("aoran", "t, broadcastSpeakerOnToPhone");
                    broadcastSpeakerOnToPhone(false);
                    iCnt++;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
        
    }


    private class GsensorTestListner implements SensorEventListener{ //  test
    
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.v(TAG, "testT, GsensorTestListner, timestamp = " + event.timestamp/1000000);
            // recordMovePoint(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }        
    }


    private int setGsensorNoEarlySuspend(boolean bNoEarlySuspend) { // from PMU6575.java
        String cmd;

        if (bNoEarlySuspend) {
            cmd = "echo 1 > " + GSENSOR_NOEARLY;
        }
        else {
            cmd = "echo 0 > " + GSENSOR_NOEARLY;
        }

        Log.v(TAG, "testT, setGsensorNoEarlySuspen d, set cmd = " + cmd);
        getInfo(cmd);
        
        cmd = "cat " + GSENSOR_NOEARLY;
        Log.v(TAG, "testT, setGsensorNoEarlySuspen d, get cmd = " + cmd);
        String out = getInfo(cmd);
        Log.v(TAG, "testT, setGsensorNoEarlySuspen d, get out = " + out);
        
        try {
            int iSet = Integer.parseInt(out);
            Log.v(TAG, "testT, setGsensorNoEarlySuspen d, iSet = " + iSet + ", out = " + out);
            return iSet;
        } catch (NumberFormatException e) {
            Log.v(TAG, "testT, setGsensorNoEarlySuspen d, NumberFormatException");
            return RESULT_FAIL;
        }
    }

    private String getInfo(String cmd) { // from PMU6575.java
        String result = null;
        
        try {
            String[] cmdx = { "/system/bin/sh", "-c", cmd }; // file must
            // exist// or
            // wait()
            // return2
            int ret = ShellExe.execCommand(cmdx);
            if (0 == ret) {
                Log.v(TAG, "testT, getInfo, OK ret = " + ret);
                result = ShellExe.getOutput();
            } else {
                // result = "ERROR";
                Log.v(TAG, "testT, getInfo, NOK ret = " + ret);               
                result = ShellExe.getOutput();
            }
        } catch (IOException e) {
            Log.v(TAG, "testT, getInfo, e = " + e.toString());
            result = "RESULT_FAIL";
        }
        
        return result;
    }

}

