package com.textquo.twist.types;

public class Cursor {
    private String webSafeString;

    public Cursor(String webSafeString){
        this.webSafeString = webSafeString;
    }

    public String getWebSafeString() {
        return webSafeString;
    }

    public void setWebSafeString(String webSafeString) {
        this.webSafeString = webSafeString;
    }
}
