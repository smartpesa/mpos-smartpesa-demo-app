package com.smartpesa.smartpesa.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DateUtilTest {

    private Date mDate;

    @Before
    public void setUp(){
        mDate = DateUtils.parse("2015-12-10T12:01:43", DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
    }

    @Test
    public void testFirstDayOfTheWeek(){
        Date d = DateUtils.getFirstDayOfWeek(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-07T00:00:00")));
    }

    @Test
    public void testLastDayOfTheWeek(){
        Date d = DateUtils.getLastDayOfWeek(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-13T23:59:59")));
    }

    @Test
    public void testGetLastWeek(){
        Date d = DateUtils.getLastWeek(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-03T12:01:43")));
    }

    @Test
    public void testNextLastWeek(){
        Date d = DateUtils.getNextWeek(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-17T12:01:43")));
    }

    @Test
    public void testGetFirstDayOfMonth(){
        Date d = DateUtils.getFirstDayOfMonth(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-01T00:00:00")));
    }

    @Test
    public void testGetLastDayOfMonth(){
        Date d = DateUtils.getLastDayOfMonth(mDate);
        String format = DateUtils.format(d, DateUtils.DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS);
        assertThat(format, is(equalTo("2015-12-31T23:59:59")));
    }


}
