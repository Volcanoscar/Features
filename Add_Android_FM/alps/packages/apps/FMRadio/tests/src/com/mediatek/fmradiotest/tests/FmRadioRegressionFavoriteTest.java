package com.mediatek.fmradiotest.tests;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;
import com.mtk.at.AutoTestAnnotations.BasicFuncTest;
import com.android.fmradio.FmMainActivity;
import com.android.fmradio.FmFavoriteActivity;
import com.android.fmradio.FmRecordActivity;
import com.android.fmradio.FmService;
import com.android.fmradio.FmStation;
import com.android.fmradio.FmUtils;
import com.android.fmradio.R;

public class FmRadioRegressionFavoriteTest extends
ActivityInstrumentationTestCase2<FmFavoriteActivity> {

	// FM state:
    private final static int POWER_UP = 0;
    private final static int POWER_DOWN = 2;
    private final static int CONVERT_RATE = 10;
	private static final String PACKAGE_UNDER_TEST_FMRADIO = "com.android.fmradio";
	private static final String GET_STATIONS = "Getting Stations";
	
	// each counter time is: 10 * 5000 = 50 seconds.
	private static final int TIME_OUT_COUNTER = 10;

	private Context mContext;
	private Solo mSolo;
	private Instrumentation mInstrumentation;
	
	private FmService mService;
	private Activity mActivity;
	public FmRadioRegressionFavoriteTest() {
        super(FmFavoriteActivity.class);
	}
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mSolo = new Solo(getInstrumentation(), getActivity());
        mInstrumentation = getInstrumentation();
        SystemClock.sleep(2000);

        mService = (FmService) TestUtils.getVariableFromFavActivity(getActivity(),
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
    
    public void test_RegressionTestCase007() throws Exception {
    	startScanAndCheck();
    	
    	startScanAndCheck();
    	
    	startScanAndCheck();
    	
    	startScanAndCheck();
    	
    	startScanAndCheck();
    }
    
    private void startScanAndCheck() {
        assertFalse(mService.isScanning());
        
    	int stationBefore = mService.getFrequency();
    	
    	//click more menu in action bar
        mSolo.sendKey(Solo.MENU);
        mInstrumentation.waitForIdleSync();

        // click Refresh menu to start scanning
        mSolo.clickOnText("Refresh");
        SystemClock.sleep(1000);
        assertTrue(mService.isScanning());
        
        assertTrue(findFmViewById(GET_STATIONS).getVisibility() == View.VISIBLE);
        
        // if scan is not completed in 30 seconds,
        // it will not wait scan finish any more.
        int timeCounter = 0;
        do {
        	SystemClock.sleep(5000);
        	timeCounter += 1;
        } while(mService.isScanning() && timeCounter < TIME_OUT_COUNTER);
        
        // make sure scan is already scan completed...
        assertFalse(mService.isScanning());
        
        // make sure the playing station is not change.
        int stationAfter = mService.getFrequency();
        assertTrue(stationBefore == stationAfter);
    }
    
    private View findFmViewById(String viewId) {
    	int id = mActivity.getResources().getIdentifier(viewId, 
    			"id", PACKAGE_UNDER_TEST_FMRADIO);
    	return mSolo.getView(id);
    }
    
    private boolean isViewEnable(String viewId) {
    	return findFmViewById(viewId).isEnabled();
    }
}
