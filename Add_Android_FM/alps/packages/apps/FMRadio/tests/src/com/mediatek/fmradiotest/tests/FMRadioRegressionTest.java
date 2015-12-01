package com.mediatek.fmradiotest.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.jayway.android.robotium.solo.Solo;
import com.mtk.at.AutoTestAnnotations.BasicFuncTest;
import com.android.fmradio.FmMainActivity;
import com.android.fmradio.FmFavoriteActivity;
import com.android.fmradio.FmRecordActivity;
import com.android.fmradio.FmService;
import com.android.fmradio.FmStation;
import com.android.fmradio.FmUtils;
import com.android.fmradio.R;

public class FMRadioRegressionTest extends
ActivityInstrumentationTestCase2<FmMainActivity>{

	private static final String TAG = "FMRadioRegressionTest";
	
	// FM state:
    private final static int POWER_UP = 0;
    private final static int POWER_DOWN = 2;
    private final static int CONVERT_RATE = 10;

	private static final String FIELD_CURRENT_STATION = "mCurrentStation";
    
    private final String PACKAGE_UNDER_TEST_FMRADIO = "com.android.fmradio";
    private static final String PREV_STATION = "button_prevstation";
    private static final String DECREASE_STATION = "button_decrease";
    private static final String ADD_FAVORITE_STATION = "button_add_to_favorite";
    private static final String NEXT_STATION = "button_nextstation";
    private static final String INCREASE_STATION = "button_increase";

    
	private Context mContext;
	private Instrumentation mInstrumentation;
	private Solo mSolo;
    private FmService mService;
    private Activity mActivity;
    
    private ImageButton mBtnPlayAndStop;
    private int mCurrentStation = 0;

    
	public FMRadioRegressionTest() {
        super(FmMainActivity.class);
	}
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mSolo = new Solo(getInstrumentation(), getActivity());
        mInstrumentation = getInstrumentation();
        SystemClock.sleep(2000);

        mService = (FmService) TestUtils.getVariableFromActivity(getActivity(),
                "mService");
        mActivity = getActivity();
        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_UP);
    }

    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }
    
    public void test_regressionTestCase003() throws Exception {
    	// FM is in playing state when it is launched.
        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_UP);
        mBtnPlayAndStop = (ImageButton) findPlayButton();

        clickAndCheckPlayButton();
        clickAndCheckPlayButton();
        clickAndCheckPlayButton();
        clickAndCheckPlayButton();
        clickAndCheckPlayButton();
    }

    private View findPlayButton() {
    	int play_button_id = mActivity.getResources().getIdentifier("play_button",
    			"id", PACKAGE_UNDER_TEST_FMRADIO);
    	return mSolo.getView(play_button_id);
    }
    
    private View findScanMenu() {
    	int scan_id = mActivity.getResources().getIdentifier("fm_station_list", 
    			"id", PACKAGE_UNDER_TEST_FMRADIO);
    	return mSolo.getView(scan_id);
    }

    private View findFmViewById(String viewId) {
    	int id = mActivity.getResources().getIdentifier(viewId, 
    			"id", PACKAGE_UNDER_TEST_FMRADIO);
    	return mSolo.getView(id);
    }

    private void clickAndCheckPlayButton() {
    	
    	//in playing state, check button state
    	assertTrue(isViewEnable(PREV_STATION));
    	assertTrue(isViewEnable(DECREASE_STATION));
    	assertTrue(isViewEnable(ADD_FAVORITE_STATION));
    	assertTrue(isViewEnable(INCREASE_STATION));
    	assertTrue(isViewEnable(NEXT_STATION));
    	
        mSolo.clickOnView(findPlayButton());
        mInstrumentation.waitForIdleSync();
        SystemClock.sleep(2000);
        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_DOWN);
    	//in playing state, check button state
    	assertFalse(isViewEnable(PREV_STATION));
    	assertFalse(isViewEnable(DECREASE_STATION));
    	assertFalse(isViewEnable(ADD_FAVORITE_STATION));
    	assertFalse(isViewEnable(INCREASE_STATION));
    	assertFalse(isViewEnable(NEXT_STATION));

        mSolo.clickOnView(mBtnPlayAndStop);
        mInstrumentation.waitForIdleSync();
        SystemClock.sleep(2000);
        fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_UP);
    }
    
    private int getIntFromActivity(String fieldName) {
        return (int) TestUtils.getVariableFromActivity(mActivity, fieldName);
    }

    private boolean isViewEnable(String viewId) {
    	return findFmViewById(viewId).isEnabled();
    }
}
