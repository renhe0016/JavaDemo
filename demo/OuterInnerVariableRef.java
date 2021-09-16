package com.baidu.bugbye.bca.demo;

public class OuterInnerVariableRef {

    public void outerouter1() {
        String aaa = null;
        aaa.equals("A");
        outer(aaa);
        aaa.equals("B");
    }

    public void outerouter2() {
        String aaa = null;
        aaa.equals("M");
        outer(aaa);
        aaa.equals("N");
    }


    public void outer(String aaa) {
        aaa.equals("a");
        temp(aaa, "b");
        aaa.equals("c");
    }

    public String getReturn() {
        String rrr = temp("a", "b");
        rrr = rrr + "rrr";
        return rrr;
    }

    public String temp(String targetaaa, String tag) {
        targetaaa.equals("2");
        int aaaaaa;
        targetaaa.equals("3");
        targetaaa.equals("mmmm");
        targetaaa = "ffff";
        targetaaa.equals("nnnn");

        if (true) {
            return targetaaa;
        }

        inner(targetaaa);
        targetaaa.equals("7");
        targetaaa.equals("8");
        return null;
    }

    public void inner(String aaa) {
        aaa.equals("4.1");
        aaa.equals("4.4");
    }

}
