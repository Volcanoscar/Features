package com.malata.superclean;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.malata.superclean.base.ActivityTack;
import com.malata.superclean.base.BaseActivity;
import com.malata.superclean.model.SDCardInfo;
import com.malata.superclean.service.CleanerService;
import com.malata.superclean.service.CoreService;
import com.malata.superclean.utils.AppUtil;
import com.malata.superclean.utils.SharedPreferencesUtils;
import com.malata.superclean.utils.StorageUtil;
import com.malata.superclean.utils.SystemBarTintManager;
import com.malata.superclean.utils.T;
import com.malata.superclean.utils.UIElementsHelper;
import com.malata.superclean.views.circleprogress.ArcProgress;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.view.View;

import android.util.Log;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
   
    ArcProgress arcStore;

    
    ArcProgress arcProgress;

    
    TextView capacity;

    Context mContext;

    private Timer timer;
    private Timer timer2;

    public static final long TWO_SECOND = 2 * 1000;
    long preTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate start");

        arcStore = (ArcProgress) findViewById(R.id.arc_store);
        arcProgress =  (ArcProgress) findViewById(R.id.arc_process);
        capacity = (TextView) findViewById(R.id.capacity);

        RelativeLayout speedUpCardView = (RelativeLayout)findViewById(R.id.card1);
        speedUpCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
               speedUp();
            }
        });

        RelativeLayout cleanCardView = (RelativeLayout)findViewById(R.id.card2);
        cleanCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
               rubbishClean();
            }
        });

        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, CleanerService.class));

        mContext = getApplicationContext();

        if (!SharedPreferencesUtils.isShortCut(mContext)) {
            createShortCut();
        }

        Log.i(TAG, "onCreate end");
        //actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);

        //applyKitKatTranslucency();
    }

    private void createShortCut() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "一键加速");
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.short_cut_icon));
        Intent i = new Intent();
        i.setAction("com.malata.shortcut");
        i.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
        sendBroadcast(intent);
        SharedPreferencesUtils.setIsShortCut(mContext, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume start");

        fillData();

        Log.i(TAG, "onResume end");
    }

    private void applyKitKatTranslucency() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager mTintManager = new SystemBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setNavigationBarTintEnabled(true);
            mTintManager.setTintDrawable(UIElementsHelper.getGeneralActionBarBackground(this));

            getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(this));
        }
    }

    private void setTranslucentStatus(boolean on) {
        Window window = getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if(on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }

        window.setAttributes(winParams);
    }

    private void fillData() {
        timer = null;
        timer2 = null;
        timer = new Timer();
        timer2 = new Timer();

        long l = AppUtil.getAvailMemory(mContext);
        long y = AppUtil.getTotalMemory(mContext);
        final double x = (((y - l) / (double) y) * 100);

        arcProgress.setProgress(0);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (arcProgress.getProgress() >= (int) x) {
                            timer.cancel();
                        } else {
                            arcProgress.setProgress(arcProgress.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);

        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(mContext);

        long nAvailaBlock;
        long TotalBlocks;
        if(mSDCardInfo != null) {
            nAvailaBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            nAvailaBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        final double percentStore = (((TotalBlocks - nAvailaBlock) / (double) TotalBlocks) * 100);

        capacity.setText(StorageUtil.convertStorage(TotalBlocks - nAvailaBlock) + "/" + StorageUtil.convertStorage(TotalBlocks));
        arcStore.setProgress(0);

        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(arcStore.getProgress() >= (int) percentStore) {
                            timer2.cancel();
                        } else {
                            arcStore.setProgress(arcStore.getProgress() + 1);
                        }
                    }
                });
            }
        }, 50, 20);
    }

    void speedUp() {
        startActivity(MemoryCleanActivity.class);
    }

    void rubbishClean() {
        startActivity(RubbishCleanActivity.class);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        timer.cancel();
        timer2.cancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = new Date().getTime();

            if((currentTime - preTime) > TWO_SECOND) {
                T.showShort(mContext, "再按一次退出应用程序");

                preTime = currentTime;

                return true;
            } else {
                ActivityTack.getInstanse().exit(mContext);
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
