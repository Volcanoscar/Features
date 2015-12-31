package com.malata.superclean.model;

import android.graphics.drawable.Drawable;

/**
 * Created by xuxiantao on 2015/9/15.
 */
public class AppInfo {

    private Drawable appIcon;
    private String appName;
    private String packName;
    private String version;
    private long pkgSize;
    private int uid;
    private boolean inRom;
    private boolean userApp;

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public boolean isInRom() {
        return inRom;
    }

    public String getPackName() {
        return packName;
    }

    public long getPkgSize() {
        return pkgSize;
    }

    public int getUid() {
        return uid;
    }

    public boolean isUserApp() {
        return userApp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setInRom(boolean inRom) {
        this.inRom = inRom;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public void setPkgSize(long pkgSize) {
        this.pkgSize = pkgSize;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUserApp(boolean userApp) {
        this.userApp = userApp;
    }
}
