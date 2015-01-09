package com.textquo.twist.types;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ListResult<T> {
    private String websafeCursor;
    private List<T> list;

    public ListResult(String websafeCursor, List<T> list) {
        this.websafeCursor = websafeCursor;
        this.list = list;
    }

    public List<T> getList() {
        if(list == null){
            list = new LinkedList<T>();
        }
        return list;
    }

    public String getWebsafeCursor() {
        return websafeCursor;
    }
}
