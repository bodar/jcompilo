package com.googlecode.compilo.tco;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Functions;
import com.googlecode.totallylazy.collections.ImmutableList;

import static com.googlecode.totallylazy.collections.ImmutableList.functions;

public class RecursiveZipper<T> {
    public final ImmutableList<T> focus;
    public final ImmutableList<T> breadcrumbs;

    private RecursiveZipper(ImmutableList<T> focus, ImmutableList<T> breadcrumbs) {
        this.focus = focus;
        this.breadcrumbs = breadcrumbs;
    }

    public static <T> RecursiveZipper<T> zipper(ImmutableList<T> focus) {
        return zipper(focus, ImmutableList.constructors.<T>empty());
    }

    private static <T> RecursiveZipper<T> zipper(ImmutableList<T> focus, ImmutableList<T> breadcrumbs) {
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

    public RecursiveZipper<T> modify(Callable1<? super ImmutableList<T>, ? extends ImmutableList<T>> callable) {
        return zipper(Functions.call(callable, focus), breadcrumbs);
    }

    public ImmutableList<T> toList() {
        return top().focus;
    }

    public RecursiveZipper<T> insert(T instance) {
        return modify(functions.cons(instance));
    }

    public RecursiveZipper<T> remove() {
        return delete();
    }

    public RecursiveZipper<T> delete() {
        return modify(functions.<T>tail());
    }

    public T current() {
        return focus.head();
    }

    public boolean isBottom() {
        return focus.isEmpty();
    }

    public boolean isTop() {
        return breadcrumbs.isEmpty();
    }
}