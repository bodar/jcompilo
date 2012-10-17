package com.example;

import com.googlecode.totallylazy.annotations.tailrec;

public class Gcd {
    @tailrec
    public static int gcd(int x, int y) {
        if (y == 0) return x;
        return gcd(y, x % y);
    }
}
