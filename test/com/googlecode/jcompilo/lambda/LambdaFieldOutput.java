package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Function1;

import static com.googlecode.totallylazy.lambda.Lambdas.n;
import static com.googlecode.totallylazy.lambda.Lambdas.λ;

public class LambdaFieldOutput {
    public static Function1<Number, Integer> intValue = new Function1<Number, Integer>() {
        @Override
        public Integer call(final Number n) throws Exception {
            return n.intValue();
        }
    };
}
