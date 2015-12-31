package com.malata.superclean.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.support.v4.app.FragmentActivity;
import com.malata.superclean.dialogs.ProgressDialogFragment;

/**
 * Created by xuxiantao on 2015/9/14.
 */
public class BaseActivity extends FragmentActivity {

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected float mDensity;
    protected Context mContext;
    protected String logName;

    private static String mDialogTag = "BaseDialog";

    ProgressDialogFragment mProgressDialogFragment;

    protected ActivityTack tack = ActivityTack.getInstanse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mDensity = metrics.density;
        logName = this.getClass().getSimpleName();
        tack.addActivity(this);

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    protected void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    protected void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(mContext, cls);
        if(bundle != null) {
            intent.putExtras(bundle);
        }

        startActivity(intent);
    }

    protected void startActivity(String action) {
        startActivity(action, null);
    }

    protected void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);

        if(bundle != null) {
            intent.putExtras(bundle);
        }

        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        tack.removeActivity(this);
    }

    public void showDialogLoading() {
        showDialogLoading(null);
    }

    public void showDialogLoading(String msg) {
        if(mProgressDialogFragment == null) {
            mProgressDialogFragment = ProgressDialogFragment.newInstance(0, null);
        }
        if(msg != null) {
            mProgressDialogFragment.setMessage(msg);
        }

        mProgressDialogFragment.show(getFragmentManager(), mDialogTag);
    }

    public void dismissDialogLoading() {
        if(mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
    }
}
