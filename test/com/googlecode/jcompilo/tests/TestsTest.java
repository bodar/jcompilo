package com.googlecode.jcompilo.tests;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestsTest {
    @Test
    public void checkedTestsWillRun() throws Exception {
        assertThat(Tests.enabled(), is(true));
    }
}
