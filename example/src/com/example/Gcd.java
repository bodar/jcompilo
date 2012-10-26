package com.example;

import com.googlecode.totallylazy.annotations.tailrec;

public class Gcd {
    @tailrec
    public static int gcdInt(int x, int y) {
        if (y == 0) return x;
        return gcdInt(y, x % y);
    }

    public static int gcdIntIterative(int x, int y) {
        while (true) {
            if (y == 0) return x;
            int remainder = x % y;
            x = y;
            y = remainder;
        }
    }

    @tailrec
    public static long gcdLong(long x, long y) {
        if (y == 0) return x;
        return gcdLong(y, x % y);
    }

    public static long gcdLongIterative(long x, long y) {
        while (true) {
            if (y == 0) return x;
            long remainder = x % y;
            x = y;
            y = remainder;
        }
    }

    @tailrec
    public static int gcdWithLocalVariable(int x, int y) {
        if (y == 0) return x;
        int remainder = x % y;
        return gcdWithLocalVariable(y, remainder);
    }



}
