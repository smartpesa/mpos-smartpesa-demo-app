package com.smartpesa.smartpesa;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * JUnit4 unit tests for the calculator logic.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SampleNormalTest {

    @Test
    public void testTrueEqualIsEqualToTrue(){
        boolean t = true;
        assertThat(t, is(equalTo(true)));
    }
}
