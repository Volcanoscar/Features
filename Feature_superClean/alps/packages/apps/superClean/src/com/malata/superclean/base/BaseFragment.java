package com.malata.superclean.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by xuxiantao on 2015/9/14.
 */
public class BaseFragment extends Fragment {

    protected void startActvity(Class<?> cls) {
        startActivity(cls, null);
    }

    protected void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), cls);

        if (bundle != null) {
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

}
