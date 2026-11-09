/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.touch

import android.util.Log

/** JNI bridge to /dev/xiaomi-touch and /dev/mi_display/disp_feature. */
object TouchUtils {
    private const val TAG = "TouchUtils"

    init {
        System.loadLibrary("xiaomiparts_jni")
    }

    // touch_mode indices (include/uapi/linux/xiaomi_touch.h)
    const val MODE_REPORT_RATE = 5
    const val MODE_EDGE_FILTER = 11
    const val MODE_PANEL_ORIENTATION = 12
    const val MODE_EXPERT_MODE = 13
    const val MODE_GAME_MODE = 14

    // mi_disp feature ids: panel-dependent; if an id is not supported by
    // the panel's feature table the driver rejects the request (EAGAIN)
    // and the toggle simply does nothing. Verify on device with:
    //   cat /sys/devices/virtual/mi_display/disp_feature/disp-DSI-0/*
    const val FEATURE_ID_DC_DIMMING = 1
    const val FEATURE_ID_HBM = 4

    external fun nativeSetTouchMode(mode: Int, value: Int): Int
    external fun nativeGetTouchMode(mode: Int): Int
    external fun nativeSetDispFeature(featureId: Int, value: Int): Int
    external fun nativeGetDispFeature(featureId: Int): Int

    fun setTouchMode(mode: Int, value: Int): Boolean {
        val ret = runCatching { nativeSetTouchMode(mode, value) }
            .getOrElse { e ->
                Log.e(TAG, "setTouchMode($mode, $value) failed", e)
                Int.MIN_VALUE
            }
        if (ret != 0) Log.e(TAG, "setTouchMode($mode, $value) returned $ret")
        return ret == 0
    }

    fun getTouchMode(mode: Int): Int =
        runCatching { nativeGetTouchMode(mode) }.getOrElse {
            Log.e(TAG, "getTouchMode($mode) failed", it)
            -1
        }

    fun setDispFeature(featureId: Int, value: Int): Boolean {
        val ret = runCatching { nativeSetDispFeature(featureId, value) }
            .getOrElse { e ->
                Log.e(TAG, "setDispFeature($featureId, $value) failed", e)
                Int.MIN_VALUE
            }
        if (ret != 0) Log.e(TAG, "setDispFeature($featureId, $value) returned $ret")
        return ret == 0
    }

    fun getDispFeature(featureId: Int): Int =
        runCatching { nativeGetDispFeature(featureId) }.getOrElse {
            Log.e(TAG, "getDispFeature($featureId) failed", it)
            -1
        }
}
