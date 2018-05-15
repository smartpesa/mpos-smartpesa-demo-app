package com.smartpesa.smartpesa.util;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MoneyUtilTest {

    @Test
    public void testGetZeroReturnsZeroFormattedString() {
        assertThat(MoneyUtils.getZero(), is(equalTo("0.00")));
    }

    @Test
    public void testStripGroupingSeparator_ReturnsWithoutGrouping() {
        String number = "4,000,999.00";
        String plain = MoneyUtils.stripGroupingSeparator(number);

        assertThat(plain, is(equalTo("4000999.00")));
    }

    @Test
    public void testFormat_ReturnsWithGrouping(){
        double plain = 4000.00;
        String formatted = MoneyUtils.format(plain);

        assertThat(formatted, is(equalTo("4,000.00")));
    }

    @Test
    public void testGetWithoutGroupingSeparator_Returns0() {
        String number = "0.00";
        String plain = MoneyUtils.stripGroupingSeparator(number);

        assertThat(plain, is(equalTo("0.00")));
    }

    @Test
    public void testFormatBigDecimal_ReturnsGrouping(){
        BigDecimal d = new BigDecimal("4000.00");
        String formatted = MoneyUtils.format(d);

        assertThat(formatted, is(equalTo("4,000.00")));
    }

    @Test
    public void testParseBigDecimal_WithoutDecimalReturnsBigDecimal() {
        String number = "10";
        BigDecimal d1 = MoneyUtils.parseBigDecimal(number);
        assertThat(d1, is(equalTo(new BigDecimal("10.00"))));

        String number2 = "100";
        BigDecimal d2 = MoneyUtils.parseBigDecimal(number2);
        assertThat(d2, is(equalTo(new BigDecimal("100.00"))));

        String number3 = "1,000";
        BigDecimal d3 = MoneyUtils.parseBigDecimal(number3);
        assertThat(d3, is(equalTo(new BigDecimal("1000.00"))));

        String number4 = "10000";
        BigDecimal d4 = MoneyUtils.parseBigDecimal(number4);
        assertThat(d4, is(equalTo(new BigDecimal("10000.00"))));
    }

    @Test
    public void testParseBigDecimal_maintainDecimalPrecision() {
        String number = "10.00";
        BigDecimal d1 = MoneyUtils.parseBigDecimal(number);
        assertThat(d1, is(equalTo(new BigDecimal("10.00"))));

        String number2 = "100.123";
        BigDecimal d2 = MoneyUtils.parseBigDecimal(number2);
        assertThat(d2, is(equalTo(new BigDecimal("100.12"))));

        String number3 = "1,000.5";
        BigDecimal d3 = MoneyUtils.parseBigDecimal(number3);
        assertThat(d3, is(equalTo(new BigDecimal("1000.50"))));

        String number4 = "10";
        BigDecimal d4 = MoneyUtils.parseBigDecimal(number4);
        assertThat(d4, is(equalTo(new BigDecimal("10.00"))));

        String number5 = "0.0";
        BigDecimal d5 = MoneyUtils.parseBigDecimal(number5);
        assertThat(d5, is(equalTo(new BigDecimal("0.00"))));
    }

    @Test
    public void testParseBigDecimal_ReturnsNull() {
        String invalidNum = "$1000";
        BigDecimal d = MoneyUtils.parseBigDecimal(invalidNum);
        assertNull(d);

        String invalidNum2 = "abc";
        BigDecimal d2 = MoneyUtils.parseBigDecimal(invalidNum2);
        assertNull(d2);
    }

    @Test
    public void testFormatBigDecimal_ReturnsPrecision() {
        BigDecimal d = new BigDecimal("10.001");
        assertThat(MoneyUtils.format(d), is(equalTo("10.00")));

        BigDecimal d2 = new BigDecimal("10.1");
        assertThat(MoneyUtils.format(d2), is(equalTo("10.10")));
    }

}
