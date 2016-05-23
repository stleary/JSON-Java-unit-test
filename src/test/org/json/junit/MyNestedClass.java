package org.json.junit;

public class MyNestedClass {
    String nestedStr;
    Integer nestedInt;
    public MyNestedClass(String nestedStr, Integer nestedInt) {
        this.nestedStr = nestedStr;
        this.nestedInt = nestedInt;
    }
    public String getNestedStr() {
        return nestedStr;
    }
    public void setNestedStr(String nestedStr) {
        this.nestedStr = nestedStr;
    }
    public Integer getNestedInt() {
        return nestedInt;
    }
    public void setNestedInt(Integer nestedInt) {
        this.nestedInt = nestedInt;
    }
}
