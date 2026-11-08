#
# SPDX-FileCopyrightText: The Paranoid Android Project
# SPDX-License-Identifier: Apache-2.0
#

ifeq (aospa_peridot,$(TARGET_PRODUCT))

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit_only.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)

# Inherit from the device configuration.
$(call inherit-product, device/xiaomi/peridot/device.mk)

# Inherit from the AOSPA configuration.
$(call inherit-product, vendor/aospa/target/product/aospa-target.mk)

PRODUCT_BRAND := POCO
PRODUCT_DEVICE := peridot
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_MODEL := 24069PC21G

PRODUCT_NAME := aospa_peridot

PRODUCT_BUILD_PROP_OVERRIDES += \
    DeviceName=peridot \
    DeviceProduct=peridot \
    SystemDevice=peridot \
    SystemName=peridot_global

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

endif
