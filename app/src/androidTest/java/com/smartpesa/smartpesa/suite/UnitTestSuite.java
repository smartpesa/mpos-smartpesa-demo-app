package com.smartpesa.smartpesa.suite;

import com.smartpesa.smartpesa.SampleNormalTest;
import com.smartpesa.smartpesa.SampleParameterizedUnitTest;
import com.smartpesa.smartpesa.util.SmallCalculatorTest;
import com.smartpesa.smartpesa.util.UtilTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SampleNormalTest.class,
                            SampleParameterizedUnitTest.class,
                            UtilTestSuite.class})
public class UnitTestSuite {
}