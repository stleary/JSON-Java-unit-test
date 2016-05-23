package org.json.junit;

public class MyNestingClass {
    private String str;
    private MyNestedClass myNestedClass;
    public MyNestingClass(String str, MyNestedClass myNestedClass) {
        this.str = str;
        this.myNestedClass = myNestedClass;
    }
    public String getStr() {
        return str;
    }
    public void setStr(String str) {
        this.str = str;
    }
    public MyNestedClass getMyNestedClass() {
        return myNestedClass;
    }
    public void setMyNestedClass(MyNestedClass myNestedClass) {
        this.myNestedClass = myNestedClass;
    }

}
