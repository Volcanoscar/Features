package com.mlt.floatmultitask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * filename:IndexActivity.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-6
 * author: laiyang
 *
 * The class is to test float feature
 *
 * Modification History
 * -----------------------------------
 *
 * -----------------------------------
 */
public class IndexActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("status", Context.MODE_PRIVATE);
        //if(sp.getBoolean("isOpen", false)) {
            Intent intent = new Intent(IndexActivity.this, FloatMultiTaskService.class);
            stopService(intent);
            startService(intent);
        //}
        finish();
    }

}
