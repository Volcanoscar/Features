package com.malata.superclean.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by xuxiantao on 2015/9/15.
 */
public class AppProcessInfo implements Comparable<AppProcessInfo> {

    public String appName;
    public String processName;
    public int pid;
    public int uid;
    public Drawable icon;
    public long memory;
    public String cpu;
    public String status;
    public String threadsCount;
    public boolean checked = true;
    public boolean isSystem;

    public AppProcessInfo() {
        super();
    }

    public AppProcessInfo(String processName, int pid, int uid) {
        super();
        this.processName = processName;
        this.pid = pid;
        this.uid = uid;
    }

    @Override
    public int compareTo(AppProcessInfo another) {
        if(this.processName.compareTo(another.processName) == 0) {
            if (this.memory < another.memory) {
                return 1;
            } else if (this.memory == another.memory) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return this.processName.compareTo(another.processName);
        }
    }
}
