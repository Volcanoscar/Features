package com.malata.superclean.base;

import android.os.Bundle;
import android.view.View;

import com.malata.superclean.swipeback.SwipeBackActivityBase;
import com.malata.superclean.swipeback.SwipeBackActivityHelper;
import com.malata.superclean.swipeback.SwipeBackLayout;
import com.malata.superclean.swipeback.Utils;

/**
 * Created by xuxiantao on 2015/9/14.
 */
public class BaseSwipeBackActivity extends BaseActivity implements SwipeBackActivityBase {

    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View view = super.findViewById(id);
        if (view == null && mHelper != null) {
            return mHelper.findViewById(id);
        }
        return view;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
