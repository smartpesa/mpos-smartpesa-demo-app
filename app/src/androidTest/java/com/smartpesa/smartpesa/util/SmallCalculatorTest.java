package com.smartpesa.smartpesa.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SmallCalculatorTest {
    private SmallCalculator mCalculator;

    @Before
    public void setUp() {
        mCalculator = new SmallCalculator();
    }

    @Test
    public void testFirstOperationReturnCurrentValue() {
        BigDecimal result = mCalculator.add(new BigDecimal("0.00"));
        assertThat(result, is(equalTo(new BigDecimal("0.00"))));
    }

    @Test
    public void testAdditionForTheFirstTimeReturnsCorrectValue() {
        mCalculator.add(new BigDecimal("1.00"));
        BigDecimal result = mCalculator.add(new BigDecimal("2.50"));
        assertThat(result, is(equalTo(new BigDecimal("3.50"))));
    }

    @Test
    public void testAdditionFollowedBySubtractionReturnsCorrectValue() {
        mCalculator.add(new BigDecimal("1.00"));
        mCalculator.add(new BigDecimal("4.00"));
        BigDecimal result = mCalculator.sub(new BigDecimal("2.00"));
        assertThat(result, is(equalTo(new BigDecimal("7.00"))));
    }

    @Test
    public void testMultipleAdditionReturnsCorrectValue() {
        mCalculator.add(new BigDecimal("1.00"));
        mCalculator.add(new BigDecimal("4.00"));
        mCalculator.add(new BigDecimal("7.20"));
        mCalculator.add(new BigDecimal("10.00"));
        BigDecimal result = mCalculator.add(new BigDecimal("2.00"));
        assertThat(result, is(equalTo(new BigDecimal("24.20"))));
    }

    @Test
    public void testMultipleSubtractionReturnsCorrectValue() {
        mCalculator.sub(new BigDecimal("1000.00"));
        mCalculator.sub(new BigDecimal("4.00"));
        mCalculator.sub(new BigDecimal("10.00"));
        mCalculator.sub(new BigDecimal("10.00"));
        BigDecimal result = mCalculator.sub(new BigDecimal("2.00"));
        assertThat(result, is(equalTo(new BigDecimal("974.00"))));
    }

    @Test
    public void testEqualPerformsLatestOperation() {
        mCalculator.add(new BigDecimal("100.00"));
        BigDecimal result = mCalculator.equal(new BigDecimal("10.00"));

        assertThat(result, is(equalTo(new BigDecimal("110.00"))));

        mCalculator.sub(result);
        BigDecimal result2 = mCalculator.sub(new BigDecimal("20.00"));

        assertThat(result2, is(equalTo(new BigDecimal("90.00"))));

        BigDecimal result3 = mCalculator.equal(new BigDecimal("10.00"));

        assertThat(result3, is(equalTo(new BigDecimal("80.00"))));
    }

    @Test
    public void testClearWillClearCalculation() {
        mCalculator.add(new BigDecimal("10.00"));
        mCalculator.add(new BigDecimal("4.00"));
        mCalculator.add(new BigDecimal("10.00"));
        BigDecimal result = mCalculator.add(new BigDecimal("2.00"));
        assertThat(result, is(equalTo(new BigDecimal("26.00"))));

        mCalculator.clear();
        BigDecimal result2 = mCalculator.equal(new BigDecimal("0.00"));
        assertThat(result2, is(equalTo(new BigDecimal("0.00"))));

        mCalculator.clear();
        mCalculator.add(new BigDecimal("15.00"));
        BigDecimal result3 = mCalculator.equal(new BigDecimal("20.00"));
        assertThat(result3, is(equalTo(new BigDecimal("35.00"))));
    }

    @Test
    public void testIsInOperationReturnsCorrectValue() {

        assertThat(mCalculator.isPerformingOperation(), is(equalTo(false)));

        mCalculator.equal(new BigDecimal("0.00"));
        assertThat(mCalculator.isPerformingOperation(), is(equalTo(false)));

        mCalculator.add(new BigDecimal("1.00"));
        assertThat(mCalculator.isPerformingOperation(), is(equalTo(true)));

        mCalculator.add(new BigDecimal("2.00"));
        mCalculator.sub(new BigDecimal("3.00"));
        assertThat(mCalculator.isPerformingOperation(), is(equalTo(true)));

    }

    @Test
    public void testCalculator_StartsWith0() {
        mCalculator.sub(new BigDecimal("0.00"));
        mCalculator.sub(new BigDecimal("0.00"));
        mCalculator.sub(new BigDecimal("0.00"));
        mCalculator.sub(new BigDecimal("0.00"));
        BigDecimal result = mCalculator.sub(new BigDecimal("50.00"));
        assertThat(result, is(equalTo(new BigDecimal("50.00"))));
    }

    @Test
    public void testCalculator_NonNegative() {
        mCalculator.add(new BigDecimal("10.00"));
        mCalculator.sub(new BigDecimal("20.00"));
        BigDecimal result = mCalculator.equal(new BigDecimal("500.00"));
        assertThat(result, is(equalTo(BigDecimal.ZERO)));
    }

}
