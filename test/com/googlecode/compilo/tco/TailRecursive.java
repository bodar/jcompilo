package com.googlecode.compilo.tco;

public class TailRecursive {

    @tailrec
    public TailRecursive top() {
        if (isTop()) return this;
        return up().top();
    }

    public TailRecursive iterativeTop() {
        TailRecursive tailRecursive = this;
        while (true) {
            if (tailRecursive.isTop()) return tailRecursive;
            tailRecursive = tailRecursive.up();
        }
    }

    private TailRecursive up() {
        return this;
    }

    public boolean isTop() {
        return false;
    }
}
