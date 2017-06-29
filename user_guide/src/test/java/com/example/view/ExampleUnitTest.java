package com.example.view;

import android.graphics.Color;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void colorC() {
//        int colorStart = 0xc6c6c6;
//        int colorEnd = 0xffffff;
//        int c = 0;
//        for (int i = colorStart; i < colorEnd; i++) {
////            System.out.println("color : " + Integer.toHexString(i));
//            c = i;
//        }
//        System.out.println(Integer.toHexString(c));
        BigInteger c = BigInteger.valueOf(0xFFFF0000);
        System.out.println("c:" + c);
    }
}