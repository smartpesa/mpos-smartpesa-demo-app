package com.smartpesa.smartpesa.suite;

import com.smartpesa.smartpesa.MainActivityInstrumentationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all Junit3 and Junit4 Instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({MainActivityInstrumentationTest.class})
public class InstrumentTestSuite {
}
