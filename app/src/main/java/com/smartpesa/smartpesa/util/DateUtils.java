package com.smartpesa.smartpesa.util;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class DateUtils {

    public final static String TIME_FORMAT_HH_MM_AA = "hh:mm aa";
    public final static String TIME_FORMAT_HH_MM_SS = "hh:mm:ss";
    public final static String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";
    public final static String DATE_FORMAT_YYYY_MM_DD_T_HH_MM = "yyyy/MM/dd'T'HH:mm";
    public final static String DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS = "yyyy/MM/dd'T'HH:mm:ss";
    public final static String DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy/MM/dd'T'HH:mm:ss'Z'";
    public final static String DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_SSSZ = "yyyy/MM/dd'T'HH:mm:ss.SSS'Z'";

    private static final Calendar sCalendar = Calendar.getInstance();

    public static String format(Date date) {
        return format(date, DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z);
    }

    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    @Nullable
    public static Date parse(String dateString) {
        return parse(dateString, DATE_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z);
    }

    @Nullable
    public static Date parse(String dateString, String format) {
        Date date = null;
        try {
            date = new SimpleDateFormat(format).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.e("Unable to parse dateString with format: %s and dateString: %s", format, dateString);
        }
        return date;
    }

    public static int getHourFromDate(Date date) {
        sCalendar.setTime(date);
        int hours = sCalendar.get(Calendar.HOUR_OF_DAY);
        return hours;
    }

    public static int getDay(Date date) {
        sCalendar.setTime(date);
        return sCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Date toEndOfDay(Date date) {
        sCalendar.setTime(date);
        sCalendar.set(Calendar.HOUR_OF_DAY, 23);
        sCalendar.set(Calendar.MINUTE, 59);
        sCalendar.set(Calendar.SECOND, 59);
        sCalendar.set(Calendar.MILLISECOND, 999);
        return sCalendar.getTime();
    }

    public static Date toStartOfDay(Date date) {
        sCalendar.setTime(date);
        sCalendar.set(Calendar.HOUR_OF_DAY, 0);
        sCalendar.set(Calendar.MINUTE, 0);
        sCalendar.set(Calendar.SECOND, 0);
        sCalendar.set(Calendar.MILLISECOND, 0);
        return sCalendar.getTime();
    }

    public static Date getFirstDayOfMonth(Date date) {
        sCalendar.setTime(date);
        sCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return toStartOfDay(sCalendar.getTime());
    }

    public static Date getLastDayOfMonth(Date date) {
        sCalendar.setTime(date);
        sCalendar.set(Calendar.DAY_OF_MONTH, sCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return toEndOfDay(sCalendar.getTime());
    }

    public static Date getFirstDayOfWeek(Date date) {
        sCalendar.setTime(date);
        sCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        sCalendar.set(Calendar.DAY_OF_WEEK, sCalendar.getFirstDayOfWeek());
        return toStartOfDay(sCalendar.getTime());
    }

    public static Date getLastDayOfWeek(Date date) {
        sCalendar.setTime(date);
        sCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        sCalendar.set(Calendar.DAY_OF_WEEK, sCalendar.getFirstDayOfWeek() + 6);
        return toEndOfDay(sCalendar.getTime());
    }

    public static Date getLastWeek(Date date){
        sCalendar.setTime(date);
        sCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        return sCalendar.getTime();
    }

    public static Date getNextWeek(Date date){
        sCalendar.setTime(date);
        sCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        return sCalendar.getTime();
    }

    public static Date getLastMonth(Date date){
        sCalendar.setTime(date);
        sCalendar.add(Calendar.MONTH, -1);
        return sCalendar.getTime();
    }

    public static Date getNextMonth(Date date){
        sCalendar.setTime(date);
        sCalendar.add(Calendar.MONTH, 1);
        return sCalendar.getTime();
    }

    public static Date yesterday(Date date){
        sCalendar.setTime(date);
        sCalendar.add(Calendar.DAY_OF_YEAR, -1);
        return sCalendar.getTime();
    }

}
