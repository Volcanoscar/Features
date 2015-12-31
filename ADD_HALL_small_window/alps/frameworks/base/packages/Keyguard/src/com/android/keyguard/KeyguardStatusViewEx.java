package com.android.keyguard;

import java.util.Date;
import java.util.Locale;
import java.util.zip.DataFormatException;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Typeface; 

public class KeyguardStatusViewEx extends KeyguardStatusView {
	
	TextView mDateRight;
        private static final Typeface sAndroidClockFont; 
        private static final Typeface sClockopiaFont; 

	static   
        {  
            sAndroidClockFont = Typeface.createFromFile("/system/fonts/AndroidClock.ttf");  
            sClockopiaFont = Typeface.createFromFile("/system/fonts/Clockopia.ttf");  
            
        }  

	public KeyguardStatusViewEx(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public KeyguardStatusViewEx(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public KeyguardStatusViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub		
		mDateRight = (TextView) findViewById(R.id.date_view_right);

		TextView tv = (TextView)findViewById(R.id.clock_text);
		tv.setTypeface(sClockopiaFont, Typeface.BOLD); 

		TextView tv2 = (TextView)findViewById(R.id.date_view_right);
		tv2.setTypeface(sClockopiaFont); 
		
		super.onFinishInflate();
	}

	@Override
	protected void refresh() {	
		super.refresh();        
        
		Date d = new Date();
		
        String dateFormat = DateFormat.format("EEEE", d).toString();
        dateFormat += "\n\n";
        dateFormat += DateFormat.format("MM-dd-yyyy", d).toString();
        mDateRight.setText(dateFormat);    
	}	
}
