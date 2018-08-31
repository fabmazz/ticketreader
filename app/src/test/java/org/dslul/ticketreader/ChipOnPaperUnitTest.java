package org.dslul.ticketreader;

import android.test.AndroidTestCase;
import android.test.ApplicationTestCase;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.dslul.ticketreader.util.HelperFunctions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.dslul.ticketreader.util.HelperFunctions.hexStringToByteArray;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ChipOnPaperUnitTest {

    List<byte[]> ticket2 = new ArrayList<>();
    List<byte[]> ticket3 = new ArrayList<>();
    List<byte[]> ticket4 = new ArrayList<>();
    List<byte[]> ticket5 = new ArrayList<>();
    List<byte[]> ticket6 = new ArrayList<>();

    @Test
    public void ChipOnPaper_isCorrect() throws Exception {
        List<byte[]> ticket1 = new ArrayList<>();
        ticket1.add(hexStringToByteArray("057D6292"));
        ticket1.add(hexStringToByteArray("AD2954E9"));
        ticket1.add(hexStringToByteArray("3915F203"));
        ticket1.add(hexStringToByteArray("07FFFFF0"));
        ticket1.add(hexStringToByteArray("01040000"));
        ticket1.add(hexStringToByteArray("020102BE"));
        ticket1.add(hexStringToByteArray("68970000"));
        ticket1.add(hexStringToByteArray("00AE10A7"));
        ticket1.add(hexStringToByteArray("0200645C"));
        ticket1.add(hexStringToByteArray("397D91B4"));
        ticket1.add(hexStringToByteArray("68A4F900"));
        ticket1.add(hexStringToByteArray("04F80000"));
        ticket1.add(hexStringToByteArray("68A4F900"));
        ticket1.add(hexStringToByteArray("00050004"));
        ticket1.add(hexStringToByteArray("F8AE1079"));
        ticket1.add(hexStringToByteArray("9E1291E4"));
        ChipOnPaper chip = new ChipOnPaper(ticket1);
        assertEquals(4, chip.getRemainingRides());
        assertEquals(0, chip.getRemainingMinutes());

    }

    @Test
    public void Smartcard_Count_Ticket_isCorrect() throws Exception {
        List<byte[]> list = new ArrayList<>();
        list.add(hexStringToByteArray("6F208970ABA0B980986C9A09078F098087E0A980DF0101010A0101090E109019011022354345676010019000"));
        list.add(hexStringToByteArray("050110129845479323849432659823874899264578987A09A9692348799000"));

        list.add(hexStringToByteArray("05012160014020014030014040014050012110012170012180000000009000"));

        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));
        list.add(hexStringToByteArray("00000000000000000000000000000000000000000000000000000000009000"));

        list.add(hexStringToByteArray("0501000000216D7F0D0004F800002A00000000006D7F0D00007000BEE39000"));
        list.add(hexStringToByteArray("0501000000216D00050004F800000400000000006CFFF00000400007779000"));
        list.add(hexStringToByteArray("0501000000216CFFF00004F80000D800000000006CFFF000004000316A9000"));

        list.add(hexStringToByteArray("60000234D95D6F5840000000000060004000004000000000234321E56A821000019000"));
        SmartCard smartcard = new SmartCard(list);


        assertEquals(3, smartcard.getRemainingRides());

        list.set(2, hexStringToByteArray("05012160014020014030014040014050012110012170012180000000009000"));
        list.set(11, hexStringToByteArray("0501000000216D959E0004F800002A00000000006D959E00001000674E9000"));
        list.set(12, hexStringToByteArray("0501000000216D7F0D0004F800002A00000000006D7F0D00007000BEE39000"));
        list.set(13, hexStringToByteArray("0501000000216D00050004F800000400000000006CFFF00000400007779000"));
        SmartCard s2 = new SmartCard(list);

        assertEquals(2, s2.getRemainingRides());


        list.set(2, hexStringToByteArray("05012060014020014030014040014050012110012170012180000000009000"));
        list.set(11, hexStringToByteArray("0501000000216D9B2C0004F800002A00000000006D9B2C000060008D809000"));
        list.set(12, hexStringToByteArray("0501000000216D959E0004F800002A00000000006D959E00001000674E9000"));
        list.set(13, hexStringToByteArray("0501000000216D7F0D0004F800002A00000000006D7F0D00007000BEE39000"));
        smartcard = new SmartCard(list);

        assertEquals(1, smartcard.getRemainingRides());


        list.set(2, hexStringToByteArray("05012060014020014030014040014050012010012070012180000000009000"));
        list.set(11, hexStringToByteArray("0501000000216DA6820004F800002A00000000006DA68200008000F9249000"));
        list.set(12, hexStringToByteArray("0501000000216D9B2C0004F800002A00000000006D9B2C000060008D809000"));
        list.set(13, hexStringToByteArray("0501000000216D959E0004F800002A00000000006D959E00001000674E9000"));
        smartcard = new SmartCard(list);

        assertEquals(0, smartcard.getRemainingRides());
    }
}