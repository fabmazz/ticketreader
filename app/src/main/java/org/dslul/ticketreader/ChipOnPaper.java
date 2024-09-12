package org.dslul.ticketreader;

import android.util.Log;

import org.dslul.ticketreader.util.GttDate;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.dslul.ticketreader.util.GttDate.addMinutesToDate;
import static org.dslul.ticketreader.util.HelperFunctions.getBytesFromPage;


public class ChipOnPaper {

    private Date date;
    private int type;
    private long remainingMins;
    private int remainingRides;

    ChipOnPaper(List<byte[]> dumplist) {

        type = (int)getBytesFromPage(dumplist.get(5), 2, 2);

        long minutes = getBytesFromPage(dumplist.get(10), 0, 3);

        if(type == 9521)
            minutes = getBytesFromPage(dumplist.get(12), 0, 3);

        date = addMinutesToDate(minutes, GttDate.getGttEpoch());

        //calcola minuti rimanenti
        Calendar c = Calendar.getInstance();
        long diff = (c.getTime().getTime() - date.getTime()) / 60000;

        long maxtime = 90;
        //city 100 e multicity
        if(type == 302 || type == 304 ||  type == 375 || type == 650 || type == 651 || type == 658) {
            maxtime = 100;
        }

        //Tour 2 giorni
        if(type == 288) {
            maxtime = 2*24*60;
        }
        //Tour 3 giorni
        if(type == 289) {
            maxtime = 3*24*60;
        }

        //daily
        if(type == 303 || type == 305 || type == 376) {
            remainingMins = GttDate.getMinutesUntilEndOfService(date);
        }
        else if(diff >= maxtime) {
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
            case 375:
                return "City 100 (U + S)";
            case 303:
            case 305:
            case 376:
                return "Daily";
            case 650:
            case 651:
            case 658:
                return "MultiCity";
            case 288:
                return "Tour 2 giorni";
            case 289:
                return "Tour 3 giorni";
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
                return "Non riconosciuto (codice: "+type+")";
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
        if(remainingMins < 0)
            return 0;
        else
            return remainingMins;
    }



}
