package com.baidu.bugbye.bca.demo;

public class ObjectGenerator {

    public static Object getObject() {
        double random = Math.random();
        if (random > 100) {
            return new Object();
        } else {
            return null;
        }
    }

    public static Object getNullableObject() {
        double random = Math.random();
        if (random > 100) {
            return new Object();
        } else {
            return null;
        }
    }
}
