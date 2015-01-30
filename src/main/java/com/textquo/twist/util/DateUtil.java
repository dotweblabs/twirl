package com.textquo.twist.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kerby on 1/16/15.
 */
@Deprecated
public class DateUtil {
    private static final List<SimpleDateFormat> knownPatterns
            = new ArrayList<SimpleDateFormat>();
    static {
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    public static Date parseDate(String date){
        for (SimpleDateFormat pattern : knownPatterns) {
            try {
                // Take a try
                return new Date(pattern.parse(date).getTime());
            } catch (ParseException pe) {
                // Loop on
            }
        }
        return null;
    }

}
