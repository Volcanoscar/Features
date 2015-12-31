package com.malata.superclean.model;

import android.graphics.drawable.Drawable;

/**
 * Created by xuxiantao on 2015/9/15.
 */
public class CacheInfo {

    private String name;
    private String packageName;
    private Drawable icon;
    private String codeSize;
    private String dataSize;
    private String cacheSize;

    public String getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getCodeSize() {
        return codeSize;
    }

    public void setCodeSize(String codeSize) {
        this.codeSize = codeSize;
    }

    public String getDataSize() {
        return dataSize;
    }

    public void setDataSize(String dataSize) {
        this.dataSize = dataSize;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
