package com.mlt.floatmultitask;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * filename:TimFormatUtils.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-20
 * author: laiyang
 *
 * use to format time
 *
 * Modification History
 * -----------------------------------
 *
 * -----------------------------------
 */
public class TimFormatUtils {
    /**
     *
     * @param pattern time format pattern
     * @param mills time in mills
     * @return string of time
     */
    public static String getTimeinMills(String pattern, long mills) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date(mills));
    }
}
