package com.zxx1.light;

import com.zxx1.light.R;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class LightActivity extends Activity {
    private Button backBtn = null;
    private Button btn = null;
    private Camera camera = null;
    private Parameters parameters = null;
    public static boolean kaiguan = true;

    private int back = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        backBtn = (Button) findViewById(R.id.btn_light);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new Mybutton());
    }

    class Mybutton implements OnClickListener {
        @Override
        public void onClick(View v) {
           if (kaiguan) {
               try {
                   camera = Camera.open();
               } catch(Exception e) {
                   Toast.makeText(LightActivity.this, "Please close Camera or TorchLight in other space!", Toast.LENGTH_SHORT).show();
                   return ;
               }
               parameters = camera.getParameters();
               parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
               camera.setParameters(parameters);
               camera.startPreview();
               backBtn.setBackgroundResource(R.drawable.shou_on);
               kaiguan = false;
           } else {
               backBtn.setBackgroundResource(R.drawable.shou_off);
               parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
               camera.setParameters(parameters);
               camera.stopPreview();
               kaiguan = true;
               camera.release();
           }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back++;
            switch (back) {
            case 1:
                Toast.makeText(LightActivity.this,
                    getString(R.string.again_exit), Toast.LENGTH_SHORT)
                    .show();
                break;
            case 2:
                back = 0;
                Myback();
                break;
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
       }
    }

    public void Myback() { 
        if (kaiguan) {
            LightActivity.this.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (!kaiguan) {
            camera.release();
            LightActivity.this.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            kaiguan = true;
        }
    }
}