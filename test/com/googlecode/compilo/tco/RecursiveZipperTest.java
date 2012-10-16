package com.googlecode.compilo.tco;

import org.junit.Test;

import static com.googlecode.compilo.tco.RecursiveZipper.zipper;
import static com.googlecode.totallylazy.collections.ImmutableList.constructors.list;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RecursiveZipperTest {
    @Test
    public void canGoToTop() {
        RecursiveZipper<String> zipper = zipper(list("A", "B", "C", "D"));
        final RecursiveZipper<String> newZipper = zipper.next().next();
        assertThat(newZipper.top(), is(zipper));
    }
}
