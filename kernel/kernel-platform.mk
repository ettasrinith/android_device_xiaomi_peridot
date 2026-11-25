#
# Copyright (C) 2025 The Android Open Source Project
#
# SPDX-License-Identifier: Apache-2.0
#

KERNEL_PATH := device/xiaomi/peridot-prebuilt

# Dtb/o
BOARD_PREBUILT_DTBOIMAGE := $(KERNEL_PATH)/dtbo.img
BOARD_PREBUILT_DTBIMAGE_DIR := $(KERNEL_PATH)/dtb

TARGET_NO_KERNEL_OVERRIDE := true
TARGET_KERNEL_SOURCE := $(KERNEL_PATH)/kernel-headers
PRODUCT_COPY_FILES += 	$(KERNEL_PATH)/kernel:kernel

# Kernel modules
# NOTE: peridot's modules.load files list bare names (no .ko), unlike
# fuxi/ishtar whose lists carry .ko suffixes -- so the addprefix pattern
# would install nothing. Install by wildcard; the LOAD lists (bare names)
# are used by the build to generate modules.load in the images.
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_LOAD := $(strip $(shell tr -d '\r' < $(KERNEL_PATH)/vendor_ramdisk/modules.load))
BOARD_VENDOR_RAMDISK_KERNEL_MODULES := $(wildcard $(KERNEL_PATH)/vendor_ramdisk/*.ko)
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_BLOCKLIST_FILE := $(KERNEL_PATH)/vendor_ramdisk/modules.blocklist

BOARD_VENDOR_RAMDISK_RECOVERY_KERNEL_MODULES_LOAD := $(strip $(shell tr -d '\r' < $(KERNEL_PATH)/vendor_ramdisk/modules.load.recovery))

BOARD_VENDOR_KERNEL_MODULES_LOAD := $(strip $(shell tr -d '\r' < $(KERNEL_PATH)/vendor_dlkm/modules.load))
BOARD_VENDOR_KERNEL_MODULES := $(wildcard $(KERNEL_PATH)/vendor_dlkm/*.ko)
BOARD_VENDOR_KERNEL_MODULES_BLOCKLIST_FILE := $(KERNEL_PATH)/vendor_dlkm/modules.blocklist

# system_dlkm keeps its kernel-release sublayout from the proven build
PRODUCT_COPY_FILES +=     $(call find-copy-subdir-files,*,$(KERNEL_PATH)/system_dlkm/lib/modules/6.1.138-android14-11-g0c3d559bcd85-ab14529422,$(TARGET_COPY_OUT_SYSTEM_DLKM)/lib/modules/6.1.138-android14-11-g0c3d559bcd85-ab14529422)
