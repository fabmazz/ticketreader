package org.dslul.ticketreader.util;

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

    public static long getMinutesUntilMidnight() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (int)(c.getTimeInMillis()-System.currentTimeMillis()/60000);

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
