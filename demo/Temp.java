package com.baidu.bugbye.bca.demo;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Temp {

    @Test
    public void test4() {
        String filePaht = "C:\\Users\\zhangquan07\\Desktop\\test";
        File file = new File(filePaht);
        File[] listFiles = FileUtils.listFiles(file, new String[]{"html"}, true).toArray(new File[]{});
        // String f = getFileName(a, "2f6eeb8d44a3fe793d6c96e998412ab4", "test", "2440");
        int b = 0;
    }

    @Test
    public void test3() {
        Boolean obj1 = null;
        if (true && obj1) {
            return;
        }
    }

    @Test
    public void test2() {
        Boolean obj1 = null;
        String a = "aa" + obj1;
        System.out.println(a);
    }

    @Test
    public void test1() throws CloneNotSupportedException {
        Boolean obj1 = null;
        String a = "aa" + obj1;
        if (!obj1) {
            obj1.toString();
        }
        Dog st = new Dog();
        Object obj = st.clone();
        System.out.println(st);
        System.out.println(obj);
    }

    public static class Dog implements Cloneable {
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public static class SubTest extends Dog {
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }


    public static class Deom {

        public static List<String> regester = new ArrayList<>();
        private static String aa;

        public static String getAa() {
            if (aa == null) {
                aa = "aaa";
            }
            regester.add(aa);
            return aa;
        }
    }

}
