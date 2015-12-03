package com.android.settings.inputmethod;

import com.android.settings.R;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
//caoqiaofeng add MTSFEFL-14 20150323
public class MLTPreferenceCategory extends PreferenceCategory{

	private static final String TAG = "MLTPreferenceCategory";

	public MLTPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.mlt_preference_category);
	}
	
    public MLTPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.mlt_preference_category);

    }
	
	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean onPrepareAddPreference(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            throw new IllegalArgumentException(
                    "Cannot add a " + TAG + " directly to a " + TAG);
        }
		return super.onPrepareAddPreference(preference);
	}

}
