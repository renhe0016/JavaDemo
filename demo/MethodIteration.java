package com.baidu.bugbye.bca.demo;

public class MethodIteration {
    public void miteration(String tttttt) {
        tttttt.equals("1");
        tttttt.equals("2");
        tttttt.equals("3");
        miteration(tttttt);
    }

    public String left(String m) {
        m.equals("1");
        right(m);
        m.equals("2");
        return m;
    }

    public String right(String n) {
        n.equals("A");
        left(n);
        n.equals("B");
        return n;
    }

}
