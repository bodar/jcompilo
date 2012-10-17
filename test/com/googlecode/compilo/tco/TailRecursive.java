package com.googlecode.compilo.tco;

import com.googlecode.totallylazy.annotations.tailrec;

public class TailRecursive {
    @tailrec
    public TailRecursive top() {
        if (isTop()) return this;
        return up().top();
    }

    private TailRecursive up() {
        return this;
    }

    public boolean isTop() {
        return false;
    }
}
