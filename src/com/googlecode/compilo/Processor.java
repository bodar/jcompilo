package com.googlecode.compilo;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Destination;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Source;

public interface Processor extends Predicate<String>, Callable2<Source, Destination, String>{
}
