package com.malata.superclean.bean;

/**
 * Created by xuxiantao on 2015/9/15.
 */
public class ProcessInfo {

    public String uid;
    public String processName;
    public int pid;
    public long memory;
    public String cpu;
    public String status;
    public String threadsCount;

    public ProcessInfo() {
        super();
    }

    public ProcessInfo(String processName, int pid) {
        super();
        this.processName = processName;
        this.pid = pid;
    }

}
