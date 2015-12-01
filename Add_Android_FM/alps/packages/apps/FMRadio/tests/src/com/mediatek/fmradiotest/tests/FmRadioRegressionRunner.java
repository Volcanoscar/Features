package com.mediatek.fmradiotest.tests;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

public class FmRadioRegressionRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(FMRadioRegressionTest.class);
        suite.addTestSuite(FmRadioRegressionFavoriteTest.class);
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
    	return FmRadioRegressionRunner.class.getClassLoader();
    }
}
