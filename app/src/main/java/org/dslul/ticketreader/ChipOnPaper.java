package org.dslul.ticketreader;

import android.util.Log;

import org.dslul.ticketreader.util.GttDate;
import org.dslul.ticketreader.util.HelperFunctions;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.dslul.ticketreader.util.GttDate.addMinutesToDate;
import static org.dslul.ticketreader.util.HelperFunctions.getBytesFromPage;


public class ChipOnPaper {

    private Date date;
    private int type;
    private long remainingMins;
    private int remainingRides;

    ChipOnPaper(List<byte[]> dumplist) {

        dumplist = dumplist;

        type = (int)getBytesFromPage(dumplist.get(5), 2, 2);

        long minutes = getBytesFromPage(dumplist.get(10), 0, 3);

        if(type == 9521)
            minutes = getBytesFromPage(dumplist.get(12), 0, 3);

        date = addMinutesToDate(minutes, GttDate.getGttEpoch());

        //calcola minuti rimanenti
        Calendar c = Calendar.getInstance();
        long diff = (c.getTime().getTime() - date.getTime()) / 60000;
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

        //calcola le corse rimanenti
        //TODO: corse in metropolitana (forse bit più significativo pag. 3)
        int tickets;
        if(type == 300) { //extraurbano
            tickets = (int) (~getBytesFromPage(dumplist.get(3), 0, 4));
        } else {
            tickets = (int)(~getBytesFromPage(dumplist.get(3), 2, 2))
                    & 0xFFFF;
        }
        remainingRides = Integer.bitCount(tickets);

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
            case 9521:
                return "Sadem Aeroporto Torino";
            default:
                return "Non riconosciuto";
        }
    }

    public String getDate() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                .format(date);
    }


    public int getRemainingRides() {
        return remainingRides;
    }


    public long getRemainingMinutes() {
        return remainingMins;
    }



}
