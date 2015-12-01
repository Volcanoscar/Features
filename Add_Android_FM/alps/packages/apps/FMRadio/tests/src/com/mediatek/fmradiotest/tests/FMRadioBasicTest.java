/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.fmradiotest.tests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioSystem;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;

import com.jayway.android.robotium.solo.Solo;
import com.mtk.at.AutoTestAnnotations.BasicFuncTest;
import com.android.fmradio.FmMainActivity;
import com.android.fmradio.FmFavoriteActivity;
import com.android.fmradio.FmRecordActivity;
import com.android.fmradio.FmService;
import com.android.fmradio.FmStation;
import com.android.fmradio.FmUtils;

public class FMRadioBasicTest extends
        ActivityInstrumentationTestCase2<FmMainActivity> {
    private final static String TAG = "FMRadioBasicTest";
    private final static int POWER_UP = 0;
    private final static int POWER_DOWN = 2;
    private final static int CONVERT_RATE = 10;
    private final static String FIELD_CURRENT_STATION = "mCurrentStation";
    // Request code
    private static final int REQUEST_CODE_FAVORITE = 1;

    public static final int REQUEST_CODE_RECORDING = 2;
    // FM Recorder state not recording and not playing
    public static final int STATE_IDLE = 5;
    // FM Recorder state recording
    public static final int STATE_RECORDING = 6;
    // FM Recorder state playing
    public static final int STATE_PLAYBACK = 7;
    // FM Recorder state invalid, need to check
    public static final int STATE_INVALID = -1;

    // Set audio policy for FM
    // should check AUDIO_POLICY_FORCE_FOR_MEDIA in audio_policy.h
    private static final int FOR_PROPRIETARY = 1;

    private Activity mActivity = null;
    private Solo mSolo = null;
    private Context mContext;

    private FmService mService;

    /**
     * constructor
     */
    public FMRadioBasicTest() {
        super(FmMainActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mSolo = new Solo(getInstrumentation(), getActivity());
        SystemClock.sleep(2000);

        mService = (FmService) TestUtils.getVariableFromActivity(getActivity(),
                "mService");
        mActivity = getActivity();
        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_UP);
        Log.d(TAG, " mService: " + mService);
    }

    public void tearDown() throws Exception {
        getActivity().finish();
        super.tearDown();
        SystemClock.sleep(2000);
    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test01_PowerUp() throws Exception {

        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_UP);

    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test02_SeekStation() throws Exception {

        int firstStation = getIntFromActivity(FIELD_CURRENT_STATION);
        mService.seekStationAsync(((float) firstStation / CONVERT_RATE) , true);
        SystemClock.sleep(2000);

        int secStation = getIntFromActivity(FIELD_CURRENT_STATION);
        mService.seekStationAsync(((float) secStation / CONVERT_RATE), true);
        SystemClock.sleep(2000);

        int thirdStation = getIntFromActivity(FIELD_CURRENT_STATION);
        assertTrue(secStation > firstStation);
        assertTrue(thirdStation > secStation);
    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test03_FavoriteStation() throws Exception {

        //assure one station is exist...
        int curStation = getIntFromActivity(FIELD_CURRENT_STATION);
        mService.seekStationAsync(((float) curStation / CONVERT_RATE), true);
        SystemClock.sleep(2000);

        int currentStation = getIntFromActivity(FIELD_CURRENT_STATION);
        FmStation.insertStationToDb(mActivity, currentStation, "stationTest");
        boolean isFavoriteStation = FmStation.isFavoriteStation(mActivity, currentStation);
        assertFalse(isFavoriteStation);

        FmStation.addToFavorite(mActivity, currentStation);
        SystemClock.sleep(2000);
        isFavoriteStation = FmStation.isFavoriteStation(mActivity, currentStation);
        assertTrue(isFavoriteStation);

        // restore state
        FmStation.removeFromFavorite(mActivity, currentStation);
        SystemClock.sleep(2000);
        isFavoriteStation = FmStation.isFavoriteStation(mActivity, currentStation);
        assertFalse(isFavoriteStation);
    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test04_TuneStation() throws Exception {

        int firstStation = getIntFromActivity(FIELD_CURRENT_STATION);
        int tuneStation = FmUtils.computeIncreaseStation(firstStation);
        for (int i = 0; i < 10; i++) {
            mService.tuneStationAsync(FmUtils.computeFrequency(tuneStation));
            tuneStation += 0.2;
            SystemClock.sleep(1000);
        }

        int secStation = getIntFromActivity(FIELD_CURRENT_STATION);
        assertTrue(secStation > firstStation);

    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test05_ScanStation() throws Exception {

        int stationNumBefore = FmStation.getStationCount(mActivity);
        // will auto scan stations
        enterStationList();
        SystemClock.sleep(10000);
        int stationNumAfter = FmStation.getStationCount(mActivity);
        if (stationNumBefore == 0) {
            assertTrue(stationNumAfter > stationNumBefore);
        } else {
            assertTrue(stationNumAfter > 0);
        }

        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test06_StartRecording() throws Exception {

        int recorderState = mService.getRecorderState();
        assertTrue(recorderState == STATE_INVALID);
        startRecording();
        SystemClock.sleep(5000);

        recorderState = mService.getRecorderState();
        assertTrue(recorderState == STATE_RECORDING);

        mService.stopRecordingAsync();
        SystemClock.sleep(3000);

        recorderState = mService.getRecorderState();
        assertTrue(recorderState == STATE_IDLE);
        mSolo.sendKey(KeyEvent.KEYCODE_BACK);
    }

    /***
     * @throws Throwable
     */
    @BasicFuncTest
    public void test07_SpeakerSwitch() throws Exception {

        //assure one station is exist...
        int curStation = getIntFromActivity(FIELD_CURRENT_STATION);
        mService.seekStationAsync(((float) curStation / CONVERT_RATE), true);
        SystemClock.sleep(2000);

        int systemForceUse = AudioSystem.getForceUse(FOR_PROPRIETARY);
        assertTrue(systemForceUse == 0);

        mService.setSpeakerPhoneOn(true);
        SystemClock.sleep(1000);
        systemForceUse = AudioSystem.getForceUse(FOR_PROPRIETARY);

        mService.setSpeakerPhoneOn(false);
        SystemClock.sleep(1000);
        systemForceUse = AudioSystem.getForceUse(FOR_PROPRIETARY);
        assertTrue(systemForceUse == 0);
    }

    public void test08_Powerdown() throws Exception {

        mService.powerDownAsync();

        // sleep 2 seconds, because power down is async
        // operation
        SystemClock.sleep(2000);
        int fmState = mService.getPowerStatus();
        assertTrue(fmState == POWER_DOWN);
    }

    private int getIntFromActivity(String fieldName) {
        return (int) TestUtils.getVariableFromActivity(mActivity, fieldName);
    }

    private float getFloatFromActivity(String fieldName) {
        return (float) TestUtils.getVariableFromActivity(mActivity, fieldName);
    }

    private void enterStationList() {
        Intent intent = new Intent();
        intent.setClass(mActivity, FmFavoriteActivity.class);
        mActivity.startActivityForResult(intent, REQUEST_CODE_FAVORITE);
    }

    private void startRecording() {
        Intent recordIntent = new Intent(mActivity, FmRecordActivity.class);
        int currentStation = getIntFromActivity(FIELD_CURRENT_STATION);
        recordIntent.putExtra(FmStation.CURRENT_STATION, currentStation);
        mActivity.startActivityForResult(recordIntent, REQUEST_CODE_RECORDING);
    }
}
