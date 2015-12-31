
package com.android.mms.util;

import android.os.SystemProperties;

public final class FeatureOption {

    /**
     * check if GEMINI is turned on or not
     */
    public static final boolean MTK_GEMINI_SUPPORT = SystemProperties.get("ro.mtk_gemini_support").equals("1");

    /**
     * check if MTK_WAPPUSH_SUPPORT is turned on or not
     */
    public static final boolean MTK_WAPPUSH_SUPPORT = SystemProperties.get("ro.mtk_wappush_support").equals("1");

    /**
     * check if MTK_VT3G324M_SUPPORT is turned on or not
     */
    public static final boolean MTK_VT3G324M_SUPPORT = SystemProperties.get("ro.mtk_vt3g324m_support").equals("1");

    /**
     * check if MTK_DRM_APP is turned on or not
     */
    public static final boolean MTK_DRM_APP = SystemProperties.get("ro.mtk_oma_drm_support").equals("1");

    /**
     * check if MTK_GEMINI_3G_SWITCH is turned on or not
     */
    public static final boolean MTK_GEMINI_3G_SWITCH = SystemProperties.get("ro.mtk_gemini_3g_switch").equals("1");

    /**
     * check if MTK_BRAZIL_CUSTOMIZATION_CLARO is turned on or not
     */
    public static final boolean MTK_BRAZIL_CUSTOMIZATION_CLARO = SystemProperties.get("ro.brazil_cust_claro").equals("1");

    public static final boolean MTK_SEND_RR_SUPPORT = SystemProperties.get("ro.mtk_send_rr_support").equals("1");

    public static final boolean EVDO_DT_SUPPORT = SystemProperties.get("ro.evdo_dt_support").equals("1");

    public static final boolean MTK_ONLY_OWNER_SIM_SUPPORT = SystemProperties.get("ro.mtk_owner_sim_support").equals("1");

    /**
     * check if LCA project or not
     */
    public static final boolean MTK_LCA_ROM_OPTIMIZE = SystemProperties.get("ro.mtk_lca_rom_optimize").equals("1");


    public static final boolean MTK_VOLTE_SUPPORT = false;

	//aoran add 20150227
    public static final boolean MALATA_SALES_TRACKING = SystemProperties.get("ro.malata_sales_tracking").equals("1");
	
}
