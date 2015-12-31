LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, ext/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ext2/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ext3/src)

appcompat_dir := ../../../prebuilts/sdk/current/support/v7/appcompat/res

res_dir := res $(appcompat_dir)

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res_ext

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat:android.support.v7.recyclerview

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 libnineoldandroids
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview

LOCAL_DEX_PREOPT := false

LOCAL_PACKAGE_NAME := superClean

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libbutterknife:libs/butterknife.jar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libnineoldandroids:libs/nineoldandroids.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
