package com.vegazsdev.bobobot.core;

public class CustomConfigFileObj {

    private String confName;
    private String confDefValue;

    public CustomConfigFileObj(String confName, String confDefValue) {
        this.confName = confName;
        this.confDefValue = confDefValue;
    }

    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public String getConfDefValue() {
        return confDefValue;
    }

    public void setConfDefValue(String confDefValue) {
        this.confDefValue = confDefValue;
    }
}
