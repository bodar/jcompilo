package com.example;

import com.googlecode.totallylazy.annotations.tailrec;
import com.googlecode.totallylazy.collections.PersistentList;

public class RecursiveZipper<T> {
    public final PersistentList<T> focus;
    public final PersistentList<T> breadcrumbs;

    private RecursiveZipper(PersistentList<T> focus, PersistentList<T> breadcrumbs) {
        this.focus = focus;
        this.breadcrumbs = breadcrumbs;
    }

    public static <T> RecursiveZipper<T> zipper(PersistentList<T> focus) {
        return zipper(focus, PersistentList.constructors.<T>empty());
    }

    public static <T> RecursiveZipper<T> zipper(PersistentList<T> focus, PersistentList<T> breadcrumbs) {
        return new RecursiveZipper<T>(focus, breadcrumbs);
    }

    public RecursiveZipper<T> next() {
        return zipper(focus.tail(), breadcrumbs.cons(focus.head()));
    }

    public RecursiveZipper<T> previous() {
        return zipper(focus.cons(breadcrumbs.head()), breadcrumbs.tail());
    }

    @tailrec
    public RecursiveZipper<T> top() {
        if (isTop()) return this;
        return previous().top();
    }

    @Override
    public int hashCode() {
        return focus.hashCode() * breadcrumbs.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RecursiveZipper && (((RecursiveZipper) other).focus.equals(focus) && ((RecursiveZipper) other).breadcrumbs.equals(breadcrumbs));
    }

    @Override
    public String toString() {
        return String.format("focus(%s), breadcrumbs(%s)", focus, breadcrumbs);
    }

    public boolean isTop() {
        return breadcrumbs.isEmpty();
    }
}