package com.example.adirahmad.notarychat31;

import android.support.v7.app.AppCompatActivity;

public class Converter extends AppCompatActivity {

    public static String static_byteArrayToString(byte[] data) {
        String res = "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int n = (int) data[i];
            if (n < 0) {
                n += 256;
            }
            sb.append((char) n);
        }
        res = sb.toString();
        return res;
    }

    public static byte[] static_stringToByteArray(String s) {
        byte[] temp = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            temp[i] = (byte) s.charAt(i);
        }
        return temp;
    }
}
