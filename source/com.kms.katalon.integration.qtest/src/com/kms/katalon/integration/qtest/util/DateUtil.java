package com.kms.katalon.integration.qtest.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provider a set of utility methods for converting {@link Date}
 *
 */
public class DateUtil {
    private DateUtil() {
        // Disable default constructor.
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static String getSubmittingDateString(String rawDateString) {
        DateFormat qTestDateFormatForGetting = new SimpleDateFormat(DATE_FORMAT);
        DateFormat qTestDateFormatForSubmitting = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        qTestDateFormatForSubmitting.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date rawDate = qTestDateFormatForGetting.parse(rawDateString);
            return qTestDateFormatForSubmitting.format(rawDate);
        } catch (ParseException e) {
            return rawDateString;
        }
    }

    public static Date parseDate(String date) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT).parse(date);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }
}