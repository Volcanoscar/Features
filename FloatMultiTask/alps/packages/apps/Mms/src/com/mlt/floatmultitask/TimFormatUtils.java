package com.mlt.floatmultitask;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by laiyang on 15-7-20.
 */
public class TimFormatUtils {

    public static String getTimeinMills(String pattern, long mills) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date(mills));
    }
}
