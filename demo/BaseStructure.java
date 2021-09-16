package com.baidu.bugbye.bca.demo;

import com.baidu.bugbye.bca.supporter.Student;

import java.util.ArrayList;

public class BaseStructure {
    public String f1 = "f1value";

    public BaseStructure() {
        this.f1 = "aa";
    }

    public String m1(String p1) {
        if (true || true || true && false) {

        }

        int intint = 4;
        new Integer(intint);
        String a = "a";
        String target = "resultvalue",temp="temp";
        target = m2(target);
        String target2 = m2(target);
        System.out.println(target2);
        Student s = new Student();
        s.setAge(4);
        s.say();
        s.say();
        if (target == "somestring") {
            target = "cc";
        } else if (target == "otherstring") {
            target = "dd";
        } else if(target == "ff") {
            target = "ff";
        } else {
            target = target;
        }
        target = this.f1;
        m2(target);
        ArrayList<String> stringString = new ArrayList<String>();
        stringString.remove(target);
        int index = stringString.indexOf("v");
        return target;
    }

    public String m2(String p1) {
        String r = p1;
        return r;
    }

    public String m3(String p1) {
        String r = p1;
        return r;
    }
}
