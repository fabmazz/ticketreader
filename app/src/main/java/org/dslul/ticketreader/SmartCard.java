package org.dslul.ticketreader;

import android.content.Context;
import android.util.Log;

import org.dslul.ticketreader.util.GttDate;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        put(4003, "Annuale Formula U a Zone");

    }};

    static final Map<Integer, String> ticketCodes = new HashMap<Integer, String>() {{
        put(712, "Ordinario Urbano");
        put(714, "City 100");
        put(715, "Daily");
        put(716, "Multidaily");
    }};

    Context context;

    private class Item {
        private int code;
        private boolean isValid;
        private boolean isTicket;
        private boolean isSubscription;
        private Date startDate;
        private Date endDate;

        public Item(byte[] data) {
            //get item type
            code = ((data[6] & 0xff) << 8) | data[7] & 0xff;
            if(code == 0) {
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

            long minutes = ~(data[11] << 16 & 0xff0000 | data[12] << 8 & 0xff00 |
                            data[13] & 0xff) & 0xffffff;
            startDate = GttDate.decode(minutes);

            minutes = ~(data[14] << 16 & 0xff0000 | data[15] << 8 & 0xff00 |
                            data[16] & 0xff) & 0xffffff;
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

        public boolean isValid() {
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




    private byte[] cardinfo = new byte[31];
    private byte[] itemsdata = new byte[31*7];

    private Date creationDate;
    private Type type;
    private List<Item> items = new ArrayList<>();
    private boolean isSubscription = false;
    private Item lastItem;

    private String subscriptionName;
    private int ridesLeft = 0;


    public SmartCard(byte[] content, Context context) {
        this.context = context;

        System.arraycopy(content, 0, cardinfo, 0,  31);
        System.arraycopy(content, 31*2+2, itemsdata, 0,  31*7);

        byte[] minutes = new byte[3];
        System.arraycopy(cardinfo, 11, minutes, 0,  3);
        creationDate = GttDate.decode(minutes);

        if(cardinfo[30] == (byte)0xC0)
            type = Type.BIP;
        else if(cardinfo[30] == (byte)0xC1)
            type = Type.PYOU;
        else if(cardinfo[30] == (byte)0xC2)
            type = Type.EDISU;

        Date lastExpireDate = GttDate.getGttEpoch();
        for (int i = 0; i < 7; i++) {
            byte[] itemdata = new byte[31];
            System.arraycopy(itemsdata, 31*i, itemdata, 0,  31);
            Item item = new Item(itemdata);

            if(item.isValid()) {
                if(lastExpireDate.before(item.getEndDate())) {
                    lastExpireDate = item.getEndDate();
                    lastItem = item;
                    if(item.isSubscription()) {
                        isSubscription = true;
                        subscriptionName = item.getTypeName();
                        if(!isExpired(item.getEndDate()))
                            break;
                    }
                    if(item.isTicket()) {
                        ridesLeft += 1;
                        if(isExpired(lastItem.getEndDate()))
                            isSubscription = false;
                        //TODO: count tickets in daily 7 carnets
                    }

                }
                //isSubscription = item.isSubscription();

                items.add(item);
            }
        }



    }


    public String getName() {
        return type + " - " + lastItem.getTypeName();
    }

    public String getSubscriptionName() {
        return type + " - " + subscriptionName;
    }

    public String getDate() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                .format(lastItem.getEndDate());
    }

    public boolean isExpired() {
        Calendar c = Calendar.getInstance();
        return c.getTime().after(lastItem.getEndDate());
    }

    private boolean isExpired(Date date) {
        Calendar c = Calendar.getInstance();
        return c.getTime().after(date);
    }

    public int getRemainingRides() {
        return ridesLeft;
    }


    public boolean isSubscription() {
        return isSubscription;
    }


}
