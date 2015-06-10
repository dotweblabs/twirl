package com.textquo.twist.types;

public class Cursor {
    private String webSafeString = null;

    public Cursor(){}

    public Cursor(String webSafeString){
        if(webSafeString != null && !webSafeString.isEmpty()){
            this.webSafeString = webSafeString;
        }
    }

    public String getWebSafeString() {
        return webSafeString;
    }

    public void setWebSafeString(String webSafeString) {
        if(webSafeString != null && !webSafeString.isEmpty()){
            this.webSafeString = webSafeString;
        }
    }
}
