package org.dslul.ticketreader.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class HelperFunctions {

    public static String byteArrayToHexString(byte[] inarray) {
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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static long getBytesFromPage(byte[] page, int offset, int bytesnum) {
        byte[] bytes = new byte[bytesnum];
        System.arraycopy(page, offset, bytes, 0, bytesnum);

        long value = 0;
        for (byte tmp : bytes)
            value = (value << 8) + (tmp & 0xff);

        return value;

    }

    public static List<byte[]> loadFromFile(String filename, Context context) {
        List<byte[]> array = new ArrayList<>();

        BufferedReader reader;

        try{
            InputStream file = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                line = reader.readLine();
                array.add(hexStringToByteArray(line));
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }

        return array;
    }

}
