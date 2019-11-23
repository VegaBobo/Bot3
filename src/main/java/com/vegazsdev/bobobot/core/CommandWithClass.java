package com.vegazsdev.bobobot.core;

public class CommandWithClass {

    private Class clazz;
    private String alias;
    private String commandInfo;

    public CommandWithClass(Class clazz, String alias, String commandInfo) {
        this.clazz = clazz;
        this.alias = alias;
        this.commandInfo = commandInfo;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCommandInfo() {
        return commandInfo;
    }

    public void setCommandInfo(String commandInfo) {
        this.commandInfo = commandInfo;
    }
}
