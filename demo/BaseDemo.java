package com.baidu.bugbye.bca.demo;

public class BaseDemo {

    public void forWhile() {
        Object obj = ObjectGenerator.getObject();
        obj.equals("1");
        for (int i = 0; i < 5; i++) {
            obj.equals("for1");
            obj.equals("for2");
        }
        obj.equals("2");
    }

    public void tryCatch1() {
        Object object = ObjectGenerator.getObject();
        try {
            object.equals("t1");
            object.equals("t2");
        } catch (NullPointerException e) {
            object.equals("cA1");
            object.equals("cA2");
        } catch (Exception e2) {
            object.equals("cB1");
            object.equals("cB2");
        } finally {
            object.equals("f1");
            object.equals("f2");
        }
    }

    public void strange() {
        Object one = ObjectGenerator.getObject();
        Object two = ObjectGenerator.getObject();
        if (null != one && null == two) {
            one = new Object();
        } else if (null != two) {
            one = new Object();
        }
        one.toString();
    }

}

