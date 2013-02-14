package com.googlecode.jcompilo.lambda;

import com.googlecode.totallylazy.Function1;

import static com.googlecode.totallylazy.lambda.Lambdas.n;
import static com.googlecode.totallylazy.lambda.Lambdas.λ;

public class LambdaFieldInput {
    public static Function1<Number, Integer> intValue = λ(n, n.intValue());
}
