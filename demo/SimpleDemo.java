package com.baidu.bugbye.bca.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleDemo {

    public void base(boolean trueOrFalse) {
        String str = "normal";
        if (trueOrFalse) {
            System.out.println("into true");
        } else {
            System.out.println("into false");
        }
    }

    public void ifElse(Object object) {
        Object one = ObjectGenerator.getNullableObject();
        Object two = ObjectGenerator.getNullableObject();
        one.equals("1");
        two.equals("2");
        if (null != one
                || null == two) {
            one.equals("3");
        } else {
            two.equals("4");
        }
        one.equals("5");
        two.equals("6");
    }

    public void switchCase(int a) {
        switch (a) {
            case 1:
                System.out.println("case1");
                break;
            case 2:
                System.out.println("case2 without break");
            case 3:
                System.out.println("case3");
                break;
            default:
                System.out.println("default");
        }
    }

    public void tryCatch(Object object) {
        String filePath = "/path/to/file/file.txt";
        File file = new File(filePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            String str = object.toString();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            System.out.println("into finally");
        }
    }

    public void tryCatchAndIf() {
        Object object = ObjectGenerator.getNullableObject();
        String filePath = "/path/to/file/file.txt";
        File file = new File(filePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            String str = object.toString();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // structure
    private String field;
    public void selfAssignment(String field) {
        field = field;
    }

    public boolean equals(SimpleDemo comp) {
        if (this.field == comp.field) {
            return true;
        } else {
            return false;
        }
    }

    public void repeatCondition() {
        int i = Math.round(5);
        int j = Math.round(5);
        if (i == 0 || j == 0 || i == 0) {
            return;
        }
    }

    protected final AtomicBoolean started = new AtomicBoolean(false);
    public boolean synchronizedBadObject() {
        synchronized (started) {
            return true;
        }
    }

    // business
    public void stringFormat() {
        String.format("%s%s", "str");
    }

    public void numberCompition(List list) {
        if (list.size() >= 0) {
            return;
        }
    }

    public void regularBadArgs(String str) {
        str.replaceAll("|", "||");
        str.replaceAll(".", "||");
    }


}
