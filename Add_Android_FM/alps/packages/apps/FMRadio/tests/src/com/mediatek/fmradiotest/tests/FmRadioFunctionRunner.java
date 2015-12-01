package com.mediatek.fmradiotest.tests;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class FmRadioFunctionRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(FMRadioBasicTest.class);
        return suite;
    }

}
