package com.example;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.annotations.lambda;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.parameters.s;
import static com.googlecode.totallylazy.lambda.Lambdas.λ;

public class UsesLambda {
    private Sequence<String> sequence;
    private int myIndex = 0;

    public UsesLambda() {
        sequence = sequence("dan", "matt");
    }

    @lambda
    public Sequence<Character> singleLambda() {
        return sequence.map(λ(s, s.charAt(0)));
    }

    @lambda
    public Function1<String, Character> duplicateLambda() {
        return λ(s, s.charAt(0));
    }

    @lambda
    public Sequence<Character> multipleLambdas() {
        return sequence.
                map(λ(s, s.toUpperCase())).
                map(λ(s, s.charAt(0)));
    }

    @lambda
    public Sequence<Character> index(int index) {
        return sequence.map(λ(s, s.charAt(index)));
    }

    //    @lambda
    public Sequence<Character> fieldIndex() {
        return sequence.map(λ(s, s.charAt(myIndex)));
    }

}
