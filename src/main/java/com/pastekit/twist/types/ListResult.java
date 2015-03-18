package com.pastekit.twist.types;

import java.util.LinkedList;
import java.util.List;

public class ListResult<T> {
    private String websafeCursor;
    private Cursor cursor;
    private List<T> list = new LinkedList<>();

    public ListResult(){}

    public ListResult(String websafeCursor, List<T> list) {
        this.cursor = new Cursor(websafeCursor);
        this.websafeCursor = websafeCursor;
        this.list = list;
    }

    public List<T> getList() {
        if(list == null){
            list = new LinkedList<T>();
        }
        return list;
    }

    public void setWebsafeCursor(String websafeCursor){
        this.cursor = new Cursor(websafeCursor);
        this.websafeCursor = websafeCursor;
    }

    public String getWebsafeCursor() {
        return websafeCursor;
    }

    public Cursor getCursor() {
        if(websafeCursor == null){
            return null;
        }
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        this.websafeCursor = cursor.getWebSafeString();
    }
}
