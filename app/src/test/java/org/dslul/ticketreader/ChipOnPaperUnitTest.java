package org.dslul.ticketreader;

import android.util.Log;

import org.dslul.ticketreader.util.HelperFunctions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        ticket1.add(HelperFunctions.hexStringToByteArray("057D6292"));
        ticket1.add(HelperFunctions.hexStringToByteArray("AD2954E9"));
        ticket1.add(HelperFunctions.hexStringToByteArray("3915F203"));
        ticket1.add(HelperFunctions.hexStringToByteArray("07FFFFF0"));
        ticket1.add(HelperFunctions.hexStringToByteArray("01040000"));
        ticket1.add(HelperFunctions.hexStringToByteArray("020102BE"));
        ticket1.add(HelperFunctions.hexStringToByteArray("68970000"));
        ticket1.add(HelperFunctions.hexStringToByteArray("00AE10A7"));
        ticket1.add(HelperFunctions.hexStringToByteArray("0200645C"));
        ticket1.add(HelperFunctions.hexStringToByteArray("397D91B4"));
        ticket1.add(HelperFunctions.hexStringToByteArray("68A4F900"));
        ticket1.add(HelperFunctions.hexStringToByteArray("04F80000"));
        ticket1.add(HelperFunctions.hexStringToByteArray("68A4F900"));
        ticket1.add(HelperFunctions.hexStringToByteArray("00050004"));
        ticket1.add(HelperFunctions.hexStringToByteArray("F8AE1079"));
        ticket1.add(HelperFunctions.hexStringToByteArray("9E1291E4"));
        ChipOnPaper chip = new ChipOnPaper(ticket1);
        assertEquals(4, chip.getRemainingRides());
        assertEquals(0, chip.getRemainingMinutes());

    }
}