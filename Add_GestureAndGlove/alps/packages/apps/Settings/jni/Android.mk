LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := libcom_android_settings_jni
LOCAL_SRC_FILES := com_android_settings_Goodix.c
LOCAL_PACKAGE_NAME := com.android.settings
LOCAL_CERTIFICATE := platform
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) \
		$(MTK_PATH_SOURCE)/kernel/drivers/video \
		$(TOP)/frameworks/base/include/media	
		
LOCAL_SHARED_LIBRARIES := \
	  libnativehelper \
	  libandroid_runtime \
	  libutils \
	  libmedia
include $(BUILD_SHARED_LIBRARY)