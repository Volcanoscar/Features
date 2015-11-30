package com.mlt.filemanager;

public abstract class GlobalConsts {
    public static final String KEY_BASE_SD = "key_base_sd";

    public static final String KEY_SHOW_CATEGORY = "key_show_category";

    public static final String INTENT_EXTRA_TAB = "TAB";

    public static final String ROOT_PATH = "/";

    public static final String SDCARD_PATH = ROOT_PATH + "sdcard";

    // Menu id
    public static final int MENU_NEW_FOLDER = 100;//创建新文件
    public static final int MENU_FAVORITE = 101;//收藏
    public static final int MENU_COPY = 104;//复制
    public static final int MENU_PASTE = 105;//粘贴
    public static final int MENU_MOVE = 106;//剪切
    //public static final int MENU_SHOWHIDE = 117; //显示隐藏文件
    public static final int MENU_COPY_PATH = 118; //复制文件路径

    public static final int OPERATION_UP_LEVEL = 3;
}
