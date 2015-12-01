
package com.mediatek.videoplayer;

import android.os.SystemProperties;

public final class FeatureOption {
    /**
     * check if MTK_DRM_APP is turned on or not
     */
    public static final boolean MTK_DRM_APP = SystemProperties.get("ro.mtk_oma_drm_support").equals("1");


}
