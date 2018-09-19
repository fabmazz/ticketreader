package org.dslul.ticketreader;

import android.util.Log;

import org.dslul.ticketreader.util.GttDate;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static org.dslul.ticketreader.util.HelperFunctions.getBytesFromPage;


public class SmartCard {

    public enum Type {
        BIP,
        PYOU,
        EDISU
    }

    static final Map<Integer, String> subscriptionCodes = new HashMap<Integer, String>() {{
        put(68, "Mensile UNDER 26");
        put(72, "Mensile Studenti Rete Urbana");

        put(101, "Settimanale Formula 1");
        put(102, "Settimanale Formula 2");
        put(103, "Settimanale Formula 3");
        put(104, "Settimanale Formula 4");
        put(105, "Settimanale Formula 5");
        put(106, "Settimanale Formula 6");
        put(107, "Settimanale Formula 7");
        put(108, "Settimanale Intera Area Formula");
        put(109, "Settimanale Personale Rete Urbana");

        put(199, "Mensile Personale Rete Urbana (Formula U)");

        put(201, "Mensile Formula 1");
        put(202, "Mensile Formula 2");
        put(203, "Mensile Formula 3");
        put(204, "Mensile Formula 4");
        put(205, "Mensile Formula 5");
        put(206, "Mensile Formula 6");
        put(207, "Mensile Formula 7");
        put(208, "Mensile Intera Area Formula");

        put(261, "Mensile Studenti Urbano+Suburbano");

        put(290, "Mensile 65+ Urbano Orario Ridotto");
        put(291, "Mensile 65+ Urbano");

        put(307, "Annuale Ivrea Rete Urbana e Dintorni");
        put(308, "Annuale Extraurbano O/D");
        put(310, "Plurimensile Studenti Extraurbano O/D");

        put(721, "Annuale UNDER 26");
        put(722, "Annuale UNDER 26 Fascia A");
        put(723, "Annuale UNDER 26 Fascia B");
        put(724, "Annuale UNDER 26 Fascia C");

        put(730, "Mensile urbano Over 65");
        put(761, "Annuale Over A");
        put(731, "Annuale Over B");
        put(732, "Annuale Over C");
        put(733, "Annuale Over D");

        put(911, "10 Mesi Studenti");
        put(912, "Annuale Studenti");

        put(990, "Junior");

        put(993, "Annuale Formula U");
        put(4001, "Settimanale Formula 4");
        put(4003, "Annuale Formula U a Zone");

    }};

    static final Map<Integer, String> ticketCodes = new HashMap<Integer, String>() {{
        put(712, "Ordinario Urbano");
        put(714, "City 100");
        put(715, "Daily");
        put(716, "Multidaily");
    }};

    private class Contract {
        private int code;
        private boolean isValid;
        private boolean isTicket;
        private boolean isSubscription;
        private Date startDate;
        private Date endDate;

        public Contract(byte[] data) {
            int company = data[0];
            //get contract type
            code = ((data[4] & 0xff) << 8) | data[5] & 0xff;
            //support for GTT S.p.A. tickets only for now
            if(code == 0 || company != 1) {
                isValid = false;
            } else {
                isValid = true;
            }

            if(ticketCodes.containsKey(code)) {
                isTicket = true;
                isSubscription = false;
            } else if(subscriptionCodes.containsKey(code)) {
                isTicket = false;
                isSubscription = true;
            } else {
                isTicket = false;
                isSubscription = false;
            }

            long minutes = ~(data[9] << 16 & 0xff0000 | data[10] << 8 & 0xff00 |
                            data[11] & 0xff) & 0xffffff;
            startDate = GttDate.decode(minutes);

            minutes = ~(data[12] << 16 & 0xff0000 | data[13] << 8 & 0xff00 |
                            data[14] & 0xff) & 0xffffff;
            endDate = GttDate.decode(minutes);


        }

        public int getCode() {
            return code;
        }


        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public boolean isContract() {
            return isValid;
        }

        public boolean isSubscription() {
            if(isSubscription)
                return true;
            else
                return false;
        }

        public boolean isTicket() {
            if(isTicket)
                return true;
            else
                return false;
        }

        public String getTypeName() {
            if(isTicket)
                return ticketCodes.get(code);
            else if(isSubscription)
                return subscriptionCodes.get(code);
            else
                return "Sconosciuto";
        }

    }




    private byte[] efEnvironment;

    private Date validationDate;
    private Date creationDate;
    private Type type;
    private List<Contract> tickets = new ArrayList<>();
    private List<Contract> subscriptions = new ArrayList<>();
    private Contract subscription;

    private int ridesLeft = 0;
    private long remainingMins;


    SmartCard(List<byte[]> dumplist) {
        efEnvironment = dumplist.get(1);
        byte[] efContractList = dumplist.get(2);
        byte[] efEventLogs1 = dumplist.get(11);
        byte[] efEventLogs2 = dumplist.get(12);
        byte[] efEventLogs3 = dumplist.get(13);
        byte[] validations = dumplist.get(14);

        byte[] minutes = new byte[3];
        System.arraycopy(efEnvironment, 9, minutes, 0,  3);
        creationDate = GttDate.decode(minutes);

        if(efEnvironment[28] == (byte)0xC0)
            type = Type.BIP;
        else if(efEnvironment[28] == (byte)0xC1)
            type = Type.PYOU;
        else if(efEnvironment[28] == (byte)0xC2)
            type = Type.EDISU;

        for (int i = 0; i < 8; i++) {
            Contract contract = new Contract(dumplist.get(i+3));

            if(contract.isContract()) {
                if(contract.isSubscription()) {
                    subscriptions.add(contract);
                }
                if(contract.isTicket()) {
                    tickets.add(contract);
                }
            }

        }

        //get a valid subscription, if there's any
        Date latestExpireDate = GttDate.getGttEpoch();
        for (Contract sub : subscriptions) {
            if (latestExpireDate.before(sub.getEndDate())) {
                latestExpireDate = sub.getEndDate();
                subscription = sub;
            }
        }


        //actual tickets count
        ridesLeft = countTickets(validations, efContractList);

        //get last validation time
        long mins = getBytesFromPage(efEventLogs1, 20, 3);
        if(mins == 0)
            mins = getBytesFromPage(efEventLogs2, 20, 3);
        if(mins == 0)
            mins = getBytesFromPage(efEventLogs3, 20, 3);
        validationDate = GttDate.addMinutesToDate(mins, GttDate.getGttEpoch());
        Calendar c = Calendar.getInstance();
        long diff = (c.getTime().getTime() - validationDate.getTime()) / 60000;

        int num = (int)(getBytesFromPage(efEventLogs1, 25, 1) >> 4);
        int tickettype = (int)getBytesFromPage(dumplist.get(num+2), 4, 2);

        long maxtime = 90;
        //city 100
        if(tickettype == 714) {
            maxtime = 100;
        }
        //daily
        if(tickettype == 715 || tickettype == 716) {
            maxtime = GttDate.getMinutesUntilMidnight();
        }
        if(diff >= maxtime) {
            remainingMins = 0;
        } else {
            remainingMins = maxtime - diff;
        }

    }

    private int countTickets(byte[] validations, byte[] contractsList) {
        int count = 0;
        for (int i = 2; i < 24; i+=3) {
            int valpos = abs(contractsList[i+1]) >> 4;
            //position in contractslist
            int pos = i/3 + 1;
            //check if it's a subscription
            int sub = abs(contractsList[i]&0xf0) >> 4;
            if(pos > 0 && pos <= 8 && (contractsList[i]&0x0f) != 0 && sub < 0xA) {
                int rides = (abs(validations[valpos*3-3] & 0xff) >> 3);
                count += rides;
            }
        }
        return count;
    }


    public String getTicketName() {
        if(hasTickets())
            return type + " - " + tickets.get(0).getTypeName();
        else
            return type.toString();
    }

    public String getSubscriptionName() {
        if(hasSubscriptions())
            return type + " - " + subscription.getTypeName();
        else
            return type.toString();
    }

    public boolean hasTickets() {
        return ridesLeft != 0;
    }

    public boolean hasSubscriptions() {
        return subscriptions.size() != 0;
    }

    public String getExpireDate() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                .format(subscription.getEndDate());
    }

    public String getValidationDate() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                .format(validationDate);
    }

    public boolean isSubscriptionExpired() {
        Calendar c = Calendar.getInstance();
        return c.getTime().after(subscription.getEndDate());
    }

    private boolean isExpired(Date date) {
        Calendar c = Calendar.getInstance();
        return c.getTime().after(date);
    }

    public int getRemainingRides() {
        return ridesLeft;
    }

    public long getRemainingMinutes() {
        return remainingMins;
    }



}
