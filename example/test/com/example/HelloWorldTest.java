package com.example;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class HelloWorldTest {
    @Test
    public void speaks() throws Exception {
        assertThat(new HelloWorld().speak(), is("Hello World"));
    }
}
