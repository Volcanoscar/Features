package com.malatamobile.smartgesture;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.graphics.drawable.Drawable;

public class GestureHandlerActivity extends Activity {
    int duration;
    int mActionType;
    String packageName;
    String activityName;
    AnimationDrawable mAniDrawable;
    ImageView mAnimImageView;
    PowerManager.WakeLock mWakeLock;
    PowerManager pm;
    static final String TAG = "GestureHandlerActivity"; 
        
    public GestureHandlerActivity() {
        mActionType = -0x1;
        packageName ="";
        activityName = "";
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("malata", "malata GestureHandlerActivity onCreate()");
       //wakeUpScreen();
		
       new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					wakeUpScreen();
				}
			}, (long)(100));   //cqf modify LFZS-73 20150629
			
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);     
        setContentView(R.layout.activity_gesture_handler);
        mAnimImageView = (ImageView)findViewById(R.id.ani_view);  
        Intent intent = getIntent();
        if(intent != null) {
        	
        	Bundle bundle = getIntent().getExtras();
        	mActionType = bundle.getInt(GestureConst.GESTURE_INTENT_EXTRA_NAME);
        	packageName = bundle.getString(GestureConst.GESTURE_PACKAGE_NAME);
        	activityName = bundle.getString(GestureConst.GESTURE_ACTIVITY_NAME);
        	
            mAnimImageView.setImageResource(getAnimationRes(mActionType));            
            mAnimImageView.setBackgroundColor(0xff000000);
            mAniDrawable = (AnimationDrawable)mAnimImageView.getDrawable();
            duration = 0;
            
            if(mAniDrawable != null) {
                for(int i = 0; i < mAniDrawable.getNumberOfFrames(); i++) {
                    duration = (duration + mAniDrawable.getDuration(i));
                }
            }
            
            handleGesture();
            new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					finish();
				}
			}, (long)(duration + 100));
            return;
        }        
    }
    
    protected void onResume() {
        super.onResume();
    }
    
    private void wakeUpScreen() {        
		//zhaoxy modify 150324 for SFZOPTSDTKK-54 start
    	//pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		//mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeUpScreen");
        //mWakeLock.acquire(5000L);
		if (mWakeLock == null) {
			pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeUpScreen");
		}

		try{
			if (mWakeLock.isHeld())
			mWakeLock.release();//always release before acquiring for safety just in case
		}
		catch(Exception e){
				//probably already released
			Log.e(TAG, e.getMessage());
		}
		mWakeLock.acquire(5000L);
		//zhaoxy modify 150324 for SFZOPTSDTKK-54 end
    }
   
    private void handleGesture() {
        switch(mActionType) {
            case GestureConst.GESTURE_SLIDE_UP_SCREEN:
            case GestureConst.GESTURE_SLIDE_E:
            case GestureConst.GESTURE_SLIDE_C:
            case GestureConst.GESTURE_SLIDE_M:
            case GestureConst.GESTURE_SLIDE_O:	   			
            case GestureConst.GESTURE_SLIDE_W:
            case GestureConst.GESTURE_SLIDE_S:
            case GestureConst.GESTURE_SLIDE_V:
            case GestureConst.GESTURE_SLIDE_Z:				
            case GestureConst.GESTURE_SLIDE_DOWN_SCREEN:				
            {
                Log.d("malata", "malata handleGesture mActionType = " + mActionType);
                startSetActivity(mActionType);
                break;
            }
        }
    }
    
    private int getAnimationRes(int actionType) {
        switch(actionType) {
            case GestureConst.GESTURE_SLIDE_UP_SCREEN:
            {
            	return R.drawable.animation_slide_up;
            }        
        	case GestureConst.GESTURE_SLIDE_E:
            {
                return R.drawable.animation_char_e;
            }
            case GestureConst.GESTURE_SLIDE_C:
            {
                return R.drawable.animation_char_c;
            }
            case GestureConst.GESTURE_SLIDE_M:
            {
                return R.drawable.animation_char_m;
            }		
            case GestureConst.GESTURE_SLIDE_O:
            {
                return R.drawable.animation_char_o;
            }			
            case GestureConst.GESTURE_SLIDE_W:
            {
                return R.drawable.animation_char_w;
            }
            case GestureConst.GESTURE_SLIDE_DOWN_SCREEN:
            {
                return R.drawable.animation_slide_down; 
            }
            case GestureConst.GESTURE_SLIDE_S:
            {
                return R.drawable.animation_char_s;
            }
            case GestureConst.GESTURE_SLIDE_V:
            {
                return R.drawable.animation_char_v;
            }
            case GestureConst.GESTURE_SLIDE_Z:
            {
                return R.drawable.animation_char_z;
            }			
			
        }
        return 0x0;
    }
    
    private void startSetActivity(int actionType) {
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);//(0x400000);

    	Log.d("malata", "malata gotoCustomAPP packageName="+packageName +" ; activityName="+activityName); 	
    	final Intent intent = new Intent(); 
	intent.setComponent(new ComponentName(new String(packageName), new String(activityName)));		    	
	intent.setAction(Intent.ACTION_MAIN);	
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	mAniDrawable.start();
	
	if((!(packageName.isEmpty())) && (getPackageManager().resolveActivity(intent, 0x0) != null)){		    	

    		Handler handlerBrowser = new Handler();
    		handlerBrowser.postDelayed(new Runnable() {
		
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub   
	    				startActivity(intent);
	    			}
	    		}, duration);	       
       }else{
	    Log.d("malata", "malata packageName or activityName is null"); 	
       }
    }
    
    protected void onDestroy() {
        super.onDestroy();
		//zhaoxy modify 150324 for SFZOPTSDTKK-54 start
        //mWakelock.release(); 
		try{
			if (mWakeLock.isHeld())
				mWakeLock.release();//always release before acquiring for safety just in case
		}
		catch(Exception e){
				//probably already released
			Log.e(TAG, e.getMessage());
		}
		//zhaoxy modify 150324 for SFZOPTSDTKK-54 end	
    }
        
}
