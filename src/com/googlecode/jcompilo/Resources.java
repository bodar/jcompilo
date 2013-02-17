package com.googlecode.jcompilo;

import com.googlecode.totallylazy.Option;

public interface Resources {
    Option<Resource> get(String name);
}
