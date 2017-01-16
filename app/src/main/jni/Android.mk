LOCAL_PATH := $(call my-dir)

include /Users/monikawojtasik/Documents/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

include $(CLEAR_VARS)
LOCAL_SRC_FILES := com_example_monikawojtasik_setupopencbv_NativeClass.cpp

LOCAL_C_INCLUDES := /Users/monikawojtasik/Documents/OpenCV-android-sdk/sdk/native/jni/include/opencv2/core/core.hpp
LOCAL_C_INCLUDES += /Users/monikawojtasik/Documents/OpenCV-android-sdk/sdk/native/jni/include
LOCAL_LDLIBS += -llog -landroid
LOCAL_MODULE := MyCLib


include $(BUILD_SHARED_LIBRARY)