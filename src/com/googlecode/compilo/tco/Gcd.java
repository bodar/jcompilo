package com.googlecode.compilo.tco;

public class Gcd {
    @tailrec
    public static int gcd(int x, int y) {
        if (y == 0) return x;
        return gcd(y, x % y);
    }

    public static int iterativeGcd(int x, int y) {
        int varX = x;
        int varY = y;
        while (true) {
            if (varY == 0) return varX;
            int currentY = varY;
            varY = varX % varY;
            varX = currentY;
        }
    }
}
