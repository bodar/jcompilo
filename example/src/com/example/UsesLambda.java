package com.example;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.annotations.lambda;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.s;
import static com.googlecode.totallylazy.lambda.Lambdas.λ;

public class UsesLambda {
    @lambda
    public Sequence<Character> lambdaExample(){
        return sequence("dan", "matt").map(λ(s, s.charAt(0)));
    }
}
