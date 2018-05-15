package com.smartpesa.smartpesa.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all tests, unit + instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({UnitTestSuite.class, InstrumentTestSuite.class})
public class AndroidTestSuite {}