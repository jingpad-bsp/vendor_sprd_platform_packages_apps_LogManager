LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SE_FILE :=$(LOCAL_PATH)/sprd_logmanager_app.te
LOCAL_DEVICE_SEPOLICY_DIR :=$(PLATDIR)/common/sepolicy/
$(shell cp -rf $(LOCAL_SE_FILE) $(LOCAL_DEVICE_SEPOLICY_DIR))
$(warning "PLATDIR is $(PLATDIR)")
$(warning "$(LOCAL_SE_FILE) copied to $(LOCAL_DEVICE_SEPOLICY_DIR)")

ifeq "$(TARGET_BUILD_VARIANT)" "user"
    LOCAL_MANIFEST_FILE := user/AndroidManifest.xml
else
    LOCAL_MANIFEST_FILE := userdebug/AndroidManifest.xml
endif
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_MODULE_TAGS := optional
LOCAL_VENDOR_MODULE := true
LOCAL_USE_AAPT2 := true
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := LogManager
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SDK_VERSION := system_current

LOCAL_STATIC_ANDROID_LIBRARIES := \
	$(ANDROID_SUPPORT_DESIGN_TARGETS)

	
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
