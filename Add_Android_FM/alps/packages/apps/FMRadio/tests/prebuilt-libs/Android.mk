
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libannotations:mtkatannotations.jar

include $(BUILD_MULTI_PREBUILT)
