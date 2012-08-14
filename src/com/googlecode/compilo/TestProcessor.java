package com.googlecode.compilo;

import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Source;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;

public class TestProcessor implements Processor {
    private final List<String> tests = new ArrayList<String>();

    @Override
    public String name() {
        return "Testing";
    }

    @Override
    public Integer call(Source source, Destination destination) throws Exception {
        return source.sources().size();
    }

    @Override
    public boolean matches(String other) {
        boolean matched = other.endsWith("Test.java");
        if (matched) tests.add(other);
        return matched;
    }

    public Sequence<String> tests() {
        return sequence(tests);
    }
}
