package org.dslul.ticketreader;

import android.util.Log;

import org.dslul.ticketreader.util.GttDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ChipOnPaper {

    private String pages;
    private String date;
    private int type;
    private long remainingMins;

    public ChipOnPaper(byte[] content) {
        //if(data == null)
        //TODO: convert this class to use bytes internally instead of strings
        String contentstr = "";
        byte[] page = new byte[4];
        for (int i = 0; i < content.length; i += 4) {
            System.arraycopy(content, i, page, 0,  4);
            contentstr = contentstr + ByteArrayToHexString(page) + System.getProperty("line.separator");
        }
        this.pages = contentstr;
        this.date = this.pages.substring(90, 96);
        //this.type = (int)getBytesFromPage(5, 0, 1);
        this.type = (int)getBytesFromPage(5, 2, 2);
    }

    public String getTypeName() {
        //http://www.gtt.to.it/cms/biglietti-abbonamenti/biglietti/biglietti-carnet
        switch (type) {
            case 302:
            case 304:
                return "City 100";
            case 303:
            case 305:
                return "Daily";
            case 704:
                return "Tour";
            case 301:
                return "Multicorsa extraurbano";
            case 702:
            case 706:
                return "Carnet 5 corse";
            case 701:
            case 705:
                return "Carnet 15 corse";
            case 300:
                return "Extraurbano";
            default:
                return "Non riconosciuto";
        }
    }

    public String getDate() {
        Date finalDate = addMinutesToDate(Long.parseLong(this.date, 16), GttDate.getGttEpoch());

        //calcola minuti rimanenti
        Calendar c = Calendar.getInstance();
        long diff = (c.getTime().getTime() - finalDate.getTime()) / 60000;
        long maxtime = 90;
        //city 100
        if(type == 302 || type == 304) {
            maxtime = 100;
        }
        //daily
        if(type == 303 || type == 305) {
            maxtime = GttDate.getMinutesUntilMidnight();
        }
        //Tour TODO: make a distinction between the two types
        if(type == 704) {
            maxtime = 2*24*60;
        }
        if(diff >= maxtime) {
            remainingMins = 0;
        } else {
            remainingMins = maxtime - diff;
        }


        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                .format(finalDate);
    }


    //TODO: corse in metropolitana (forse bit più significativo pag. 3)
    public int getRemainingRides() {
        int tickets;
            if(type == 300) { //extraurbano
                tickets = (int) (~getBytesFromPage(3, 0, 4));
            } else {
                    tickets = (int)(~getBytesFromPage(3, 2, 2))
                                            & 0xFFFF;
            }
        return Integer.bitCount(tickets);
    }


    public long getRemainingMinutes() {
        return remainingMins;
    }



    private long getBytesFromPage(int page, int offset, int bytesnum) {
        return Long.parseLong(
                pages.substring(9 * page + offset * 2, 9 * page + offset * 2 + bytesnum * 2), 16);
    }



    private static Date addMinutesToDate(long minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }


    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String ByteArrayToHexString(byte[] inarray) {
            int i, j, in;
            String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
            String out= "";
            for(j = 0 ; j < inarray.length ; ++j) {
                in = (int) inarray[j] & 0xff;
                i = (in >> 4) & 0x0f;
                out += hex[i];
                i = in & 0x0f;
                out += hex[i];
            }
            return out;
    }

}
