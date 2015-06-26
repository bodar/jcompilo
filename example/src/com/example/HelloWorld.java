package com.example;

import com.googlecode.totallylazy.Strings;

public class HelloWorld {
    public String speak() {
        return Strings.toString(getClass().getResourceAsStream("resource.txt"));
    }

    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        System.out.println(helloWorld.speak());
    }
}
