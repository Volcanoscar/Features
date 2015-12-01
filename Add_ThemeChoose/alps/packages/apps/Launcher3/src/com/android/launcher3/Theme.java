package com.android.launcher3;


public interface Theme {

	public static final String INTENT_THEME_CHOOSER_ACTIVITY_SWITCH_THEME = "theme_chooser_activity_switch_theme";
	public final static String THEME = "theme";
	public final static String CURRENT_THEME = "current_theme";
    public static final String ICON_CONFIG = "icon_config";
    public static final String DEFAULT = "default";
    
	public final static int[] PREVIEWS = { R.drawable.preview_1,
				R.drawable.preview_2, R.drawable.preview_3,
				R.drawable.preview_4 ,R.drawable.preview_5};
	public final static int[] WALLPAPERS = { R.drawable.wallpaper_1,
				R.drawable.wallpaper_2, R.drawable.wallpaper_3,
				R.drawable.wallpaper_4 ,R.drawable.wallpaper_5};
    
	public final static String[] ICON_CONFIGS = { "malata_theme_1",
				"malata_theme_2", "malata_theme_3", "malata_theme_4" ,"malata_theme_5"};
}