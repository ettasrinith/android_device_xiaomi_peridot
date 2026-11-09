/*
 * XiaomiParts JNI bridge for /dev/xiaomi-touch and
 * /dev/mi_display/disp_feature.
 * SPDX-License-Identifier: Apache-2.0
 */

#include <errno.h>
#include <fcntl.h>
#include <jni.h>
#include <linux/ioctl.h>
#include <string.h>
#include <sys/ioctl.h>
#include <unistd.h>

#include "mi_disp.h"
#include "xiaomi_touch.h"

#define TOUCH_DEV_PATH "/dev/xiaomi-touch"
#define DISP_FEATURE_PATH "/dev/mi_display/disp_feature"

static int open_dev(const char *path) {
	int fd = open(path, O_RDWR);
	return fd;
}

JNIEXPORT jint JNICALL
Java_com_xiaomi_settings_touch_TouchUtils_nativeSetTouchMode(
	JNIEnv *env, jclass clazz, jint mode, jint value)
{
	struct touch_mode_request req = { .mode = mode, .value = value };
	int fd = open_dev(TOUCH_DEV_PATH);
	int ret;

	if (fd < 0)
		return -errno;

	ret = ioctl(fd, TOUCH_IOC_SET_CUR_VALUE, &req);
	close(fd);
	return ret == 0 ? 0 : -errno;
}

JNIEXPORT jint JNICALL
Java_com_xiaomi_settings_touch_TouchUtils_nativeGetTouchMode(
	JNIEnv *env, jclass clazz, jint mode)
{
	struct touch_mode_request req = { .mode = mode, .value = 0 };
	int fd = open_dev(TOUCH_DEV_PATH);
	int ret;

	if (fd < 0)
		return -errno;

	ret = ioctl(fd, TOUCH_IOC_GET_CUR_VALUE, &req);
	close(fd);
	return ret == 0 ? req.value : -errno;
}

static int disp_feature_ioctl(unsigned long cmd, jint feature_id,
			      jint feature_val)
{
	struct disp_feature_req req;
	int fd = open_dev(DISP_FEATURE_PATH);
	int ret;

	if (fd < 0)
		return -errno;

	memset(&req, 0, sizeof(req));
	req.base.disp_id = MI_DISP_PRIMARY;
	req.feature_id = (unsigned int)feature_id;
	req.feature_val = (int)feature_val;

	ret = ioctl(fd, cmd, &req);
	close(fd);
	return ret == 0 ? 0 : -errno;
}

JNIEXPORT jint JNICALL
Java_com_xiaomi_settings_touch_TouchUtils_nativeSetDispFeature(
	JNIEnv *env, jclass clazz, jint feature_id, jint value)
{
	return disp_feature_ioctl(MI_DISP_IOCTL_SET_FEATURE, feature_id, value);
}

JNIEXPORT jint JNICALL
Java_com_xiaomi_settings_touch_TouchUtils_nativeGetDispFeature(
	JNIEnv *env, jclass clazz, jint feature_id)
{
	return disp_feature_ioctl(MI_DISP_IOCTL_GET_FEATURE, feature_id, 0);
}
