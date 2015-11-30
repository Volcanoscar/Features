# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    Baidu_MobAds_SDK
#LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

LOCAL_PACKAGE_NAME := TorchLight

#LOCAL_DEX_PREOPT := false

LOCAL_CERTIFICATE := platform

#LOCAL_MODULE_PATH := $(TARGET_OUT)/vendor/operator/app

include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := Baidu_MobAds_SDK:libs/Baidu_MobAds_SDK_3.2.jar
include $(BUILD_MULTI_PREBUILT)