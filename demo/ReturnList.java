package com.baidu.bugbye.bca.demo;

public class ReturnList {

    public String start() {
        if (true) {
            return "start";
        }
        if (true) {
            return m1();
        }
        return null;
    }

    public String m1() {
        if (true) {
            return "m1";
        }
        return m2();

    }

    public String m2() {
        if (true) {
            return "m2";
        }
        return null;
    }
}
