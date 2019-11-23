package com.vegazsdev.bobobot.db;

import com.vegazsdev.bobobot.utils.XMLs;

public class PrefObj {
    private double id;
    private String lang;
    private String hotkey;

    public PrefObj(double id, String lang, String hotkey) {
        this.id = id;
        this.lang = lang;
        this.hotkey = hotkey;
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getString(String value) {
        if (XMLs.getFromStringsXML(this.getLang(), value) == null) {
            return XMLs.getFromStringsXML("strings-en.xml", value);
        } else {
            return XMLs.getFromStringsXML(this.getLang(), value);
        }
    }

    public String getHotkey() {
        return hotkey;
    }

    public void setHotkey(String hotkey) {
        this.hotkey = hotkey;
    }
}
