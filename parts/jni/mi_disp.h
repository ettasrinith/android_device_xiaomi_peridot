/* Vendored subset of display-drivers include/uapi/display/drm/mi_disp.h
 * (GPL-2.0, Qualcomm Innovation Center) */
#ifndef _MI_DISP_UAPI_H
#define _MI_DISP_UAPI_H

#include <linux/ioctl.h>
#include <linux/types.h>

#define MI_DISP_PRIMARY 0

struct disp_base {
	__u32 flag;
	__u32 disp_id;
};

struct disp_feature_req {
	struct disp_base base;
	__u32 feature_id;
	__s32 feature_val;
	__u32 tx_len;
	__u64 tx_ptr;
	__u32 rx_len;
	__u64 rx_ptr;
};

#define MI_DISP_IOCTL_SET_FEATURE _IOWR('D', 0x01, struct disp_feature_req)
#define MI_DISP_IOCTL_GET_FEATURE _IOWR('D', 0x0F, struct disp_feature_req)

#endif
