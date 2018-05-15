package com.smartpesa.smartpesa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import android.test.suitebuilder.annotation.SmallTest;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;

/**
 * JUnit4 tests for the calculator's add logic.
 *
 * <p> This test uses a Junit4s Parameterized tests features which uses annotations to pass
 * parameters into a unit test. The way this works is that you have to use the {@link
 * Parameterized}
 * runner to run your tests.
 * </p>
 */
@RunWith(Parameterized.class)
@SmallTest
public class SampleParameterizedUnitTest {
    /**
     * @return {@link Iterable} that contains the values that should be passed to the constructor.
     * In this example we are going to use three parameters: operand one, operand two and the
     * expected result.
     */
    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0, 0, 0},
                {0, -1, -1},
                {2, 2, 4},
                {8, 8, 16},
                {16, 16, 32},
                {32, 0, 32},
                {64, 64, 128}});
    }

    private final double mOperandOne;
    private final double mOperandTwo;
    private final double mExpectedResult;

    /**
     * Constructor that takes in the values specified in
     * {@link SampleParameterizedUnitTest#data()}. The values need to be saved to fields in
     * order
     * to reuse them in your tests.
     */
    public SampleParameterizedUnitTest(double operandOne, double operandTwo,
                                       double expectedResult) {
        mOperandOne = operandOne;
        mOperandTwo = operandTwo;
        mExpectedResult = expectedResult;
    }

    @Test
    public void testAdd_TwoNumbers() {
        double resultAdd = mOperandOne + mOperandTwo;
        assertThat(resultAdd, is(equalTo(mExpectedResult)));
    }
}
