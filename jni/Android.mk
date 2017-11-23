LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := locSDK3
LOCAL_SRC_FILES := prebuilt/liblocSDK3.so 
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libbdpush_V2_0
LOCAL_SRC_FILES := prebuilt/libbdpush_V2_2.so 
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := nettest
LOCAL_SRC_FILES := edu_bupt_nettest_Latency.c edu_bupt_nettest_Upload.c edu_bupt_nettest_Download.c edu_bupt_nettest_ThirdDownload.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
