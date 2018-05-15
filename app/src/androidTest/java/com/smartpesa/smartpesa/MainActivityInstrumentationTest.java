package com.smartpesa.smartpesa;

import com.smartpesa.smartpesa.activity.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import smartpesa.sdk.PesaPod.ServiceManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * JUnit4 Ui Tests for {@link MainActivity} using the {@link AndroidJUnitRunner}.
 * This class uses the JUnit4 syntax for tests.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityInstrumentationTest {
    /**
     * A JUnit {@link Rule @Rule} to launch your activity under test. This is a replacement
     * for {@link ActivityInstrumentationTestCase2}.
     * <p>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link Before @Before} method.
     * <p>
     * {@link ActivityTestRule} will create and launch of the activity for you and also expose
     * the activity under test. To get a reference to the activity you can use
     * the {@link ActivityTestRule#getActivity()} method.
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class, false, false);

    @Before
    public void setUp() {
        /**
         * We need to instantiate merchant module here since {@link MainActivity} needs
         * {@link com.smartpesa.smartpesa.persistence.MerchantModule} to be instantiated
         * or it will crash with NullPointerException.
         */
        ((SmartPesaApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).createMerchantComponent();
        mActivityRule.launchActivity(new Intent());
    }

    @Test
    public void testThatServiceManagerIsNotNull() {
        ServiceManager manager = mActivityRule.getActivity().serviceManager.get();
        assertThat(manager, notNullValue());
    }
}
