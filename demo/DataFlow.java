package com.baidu.bugbye.bca.demo;

public class DataFlow {

    public void demo4() {
        Object target = new Object();
        target = null;
        int a = 4;
        switch (a) {
            case 1:
                target.equals("c1");
                break;
            case 2:
                target.equals("c2");
                target.equals("c2if");
                if (null != target) {
                }
                break;
            default:
                target.equals("default");
        }
    }

    public void demo5() {
        Object target = new Object();
        target.equals("1");
        int i = 0;
        while (i < 5) {
            i++;
            if (target != "cond1") {
                continue;
            }
            while (i < 5) {
                if (target != "cond2") {
                    continue;
                }
                target.equals("forfor1");
            }
            target.equals("for1");
            target.equals("for2");
        }
        target.equals("2");
    }

    public void demo6() {
        Object target = new Object();
        target.equals("1");
        int i = 0;
        while (i < 5) {
            i++;
            target.equals("for1");
            target.equals("for2");
        }
        target.equals("2");
    }

    public void demo7() {
        Object obj = new Object();
        obj.equals("1");
        for (int i = 0; i < 5; i++) {
            obj.equals("for1");
            obj.equals("for2");
        }
        obj.equals("2");
    }

    public void demo8() {
        int a = 0;
        {
            int b = 0;
        }
        Object obj = null;
        switch (a) {
            case 1:
                obj.equals("1");
                break;
            case 2: {
                obj.equals("2a");
            }
            obj.equals("2b");
            case 3: {
                obj.equals("3a");
            }
            {
                obj.equals("3b");
            }
            default:
                obj.equals("default");
        }
    }


    public void demo9() {
        Object obj = null;
        if (obj.equals("if1") || obj.equals("if2")) {
            obj.equals("s1");
            obj.equals("s2");
            Object object = null;
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
    }

    public void demo10() {
        Object object = null;
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

    public void demo0() {
        Object obj = null;
        if (obj.equals("if1") || obj.equals("if2")) {
            obj.equals("s1");
            obj.equals("s2");
            if (obj.equals("if3") || obj.equals("if4")) {
                obj.equals("s3");
                obj.equals("s4");
            }
        }
    }

    public void demo1() {
        Object obj = null;
        if (obj.equals("if1") && obj.equals("if2") || obj.equals("if3")) {
            obj.equals("s1");
            obj.equals("s2");
        } else if (obj.equals("elseif1") || obj.equals("elseif2")) {
            obj.equals("s3");
            obj.equals("s4");
        } else {
            obj.equals("s5");
        }
    }

    public void demo2() {
        boolean b = true;
        int i = 0;
        Object obj = null;
        if (!(!((b) || ++i > 0 || obj == null) && obj.toString() == "") && obj.toString() != null) {
            obj.toString();
            obj.toString();
        }

    }

    public void demo3() {
        Object object = new Object();
        object.equals("1");
        try {
            object.equals("t1");
            object.equals("t2");
            Object inner = new Object();
            try {
                inner.equals("inner_t1");
                inner.equals("inner_t2");
            } catch (NullPointerException e) {
                inner.equals("inner_cA1");
                inner.equals("inner_cA2");
            } catch (Exception e2) {
                object.equals("inner_cB1");
                object.equals("inner_cB2");
            } finally {
                object.equals("inner_f1");
                object.equals("inner_f2");
            }
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
        object.equals("2");
    }


}
