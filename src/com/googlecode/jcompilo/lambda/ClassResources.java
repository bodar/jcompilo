package com.googlecode.jcompilo.lambda;

import com.googlecode.jcompilo.Resource;
import com.googlecode.jcompilo.Resources;
import com.googlecode.totallylazy.Option;

class ClassResources implements Resources {
    @Override
    public Option<Resource> get(final String name) {
        try {
            return Option.some(Resource.constructors.resource(Class.forName(name)));
        } catch (ClassNotFoundException e) {
            return Option.none();
        }
    }
}
