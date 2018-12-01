package org.dslul.ticketreader.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class GttDate {


    public static Date decode(byte[] minutes) {
        return addMinutesToDate(byteArrayToLong(minutes), getGttEpoch());

    }

    public static Date decode(long minutes) {
        return addMinutesToDate(minutes, getGttEpoch());

    }

    public static Date getGttEpoch() {
        String startingDate = "05/01/01 00:00:00";
        SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(startingDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static long getMinutesUntilEndOfService(Date startDate) {
        Calendar curr = Calendar.getInstance();
        Calendar after = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        int dayoff = 0;
        if(start.get(Calendar.DAY_OF_MONTH) == curr.get(Calendar.DAY_OF_MONTH))
            dayoff = 1;
        after.add(Calendar.DAY_OF_MONTH, dayoff);
        after.set(Calendar.HOUR_OF_DAY, 3);
        after.set(Calendar.MINUTE, 0);
        after.set(Calendar.SECOND, 0);
        after.set(Calendar.MILLISECOND, 0);
        return (after.getTimeInMillis() - curr.getTimeInMillis())/60000;

    }

    public static Date addMinutesToDate(long minutes, Date beforeTime) {
        final long ONE_MINUTE_IN_MILLIS = 60000;

        long curTimeInMs = beforeTime.getTime();
        return new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
    }

    private static long byteArrayToLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
           value = (value << 8) + (bytes[i] & 0xff);
        }
        return value;
    }

}
