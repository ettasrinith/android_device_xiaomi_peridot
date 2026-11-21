# Neotic OS · Xiaomi POCO F6 (peridot)

Unofficial **Neotic OS** build target for the Xiaomi POCO F6 (`peridot`, Qualcomm `pineapple` / SM8635 family), targeting the Neotic **`cnb`** branch (Android 16, `cp2a` release config).

This is a full **LineageOS 24.0 → Neotic cnb conversion** of the peridot device tree, with vendor blobs re-extracted fresh from Xiaomi HyperOS **OS3.0.304.0.WNPMIXM**, following the same conventions Neotic's own Xiaomi trees (e.g. aurora) use.

---

## What changed vs the stock LOS tree

| Area | Change |
|---|---|
| Product makefile | `peridot.mk` inherits `vendor/neoteric/target/product/neoteric-target.mk`, fingerprint bumped to `OS3.0.304.0.WNPMIXM` |
| Kernel | **Prebuilt** via `kernel/kernel-platform.mk` (fuxi/ishtar cnb pattern) — no kernel source build. Sources stay pinned for reference via `neoteric.dependencies` |
| Audio | 100% blobs (AGM/PAL/ar-pal stack from the firmware). `TARGET_COMMON_QTI_COMPONENTS` deliberately excludes `audio` — the `vendor/qcom/common` audio component needs CAF source-built AGM which Neotic trees don't ship |
| CAF display | Neotic `android_hardware_qcom_display` `cnb-8650` at `hardware/qcom-caf/sm8650/display` (`QCOM_SOONG_NAMESPACE`), namespace imports + `PRODUCT_SOONG_NAMESPACES` wired for it |
| Blobs | Fresh extraction from HyperOS OS3.0.304.0; list reconciled against the LOS peridot list — everything `vendor/qcom/common` provides was swept out, device-specific blobs restored, symbol closure verified (zero unresolved DT_NEEDED) |
| Reference | Cross-checked against the CLO-style peridot port (`peridot-clo`, Project Mia) for framework-matrix coverage, CLO-system sepolicy domains, and FS/OTA policy |
| VINTF | `configs/vintf/compatibility_matrix.lineage.xml` shipped as a **product VINTF fragment** via the `peridot_compat_matrix_lineage` `prebuilt_etc` soong module (see below) |
| QTI components | `TARGET_COMMON_QTI_COMPONENTS := av bt perf telephony usb wfd wlan` (no audio), `TARGET_USE_AIDL_QTI_BT_AUDIO := true`, `TARGET_FWK_SUPPORTS_FULL_VALUEADDS := true` |

### Why a product VINTF fragment instead of `DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE`

Neotic ships its compatibility matrix as `DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE`, which lands as `compatibility_matrix.device.xml`. On CLO-based trees libvintf **ignores** that file (and the canonical `compatibility_matrix.xml`) — it only merges *numbered/dated fragments* (`compatibility_matrix.7.xml`, `compatibility_matrix.202404.xml`, …) from any partition. So the two extra HALs this device needs in the framework matrix (`vendor.lineage.health@1/2` for `IChargingControl`/`IFastCharge`, `vendor.qti.hardware.qconfig@1-5`) are declared here with **per-version `<version>` tags** (AIDL matrix entries silently drop ranges like `1-2` — HIDL-only syntax) and shipped as a product VINTF fragment:

```
Android.bp:  prebuilt_etc { name: "peridot_compat_matrix_lineage",
                            src: "configs/vintf/compatibility_matrix.lineage.xml",
                            relative_install_path: "vintf", product_specific: true }
device.mk:   PRODUCT_PACKAGES += peridot_compat_matrix_lineage
```

Verified `COMPATIBLE` with a manual `checkvintf` run against the built images.

---

## Vendor blobs

Blobs live in the **private** repo [`ettasrinith/android_vendor_xiaomi_peridot`](https://github.com/ettasrinith/android_vendor_xiaomi_peridot) (Xiaomi proprietary content). The two modem images are tracked with git-lfs.

### How they were extracted (HyperOS → blobs)

1. Download `peridot` HyperOS stable **OS3.0.304.0.WNPMIXM** fastboot package (`.tgz`), MD5-verified
2. The package ships a single `super.img` (no separate `system.img`) → `lpunpack` to get `system / vendor / odm / product / system_ext / vendor_dlkm / system_dlkm` partitions
3. HyperOS 3 packs partitions as **EROFS** → extracted with `fsck.erofs --extract` (built erofs-utils from source; 7-Zip has no EROFS support)
4. Blob list (`proprietary-files.txt`) reconciled against the peridot LOS list for this base:
   - blobs already provided by `vendor/qcom/common` components → removed
   - device-specific blobs (audio, qspm, sound_trigger, libdisplaydebug, camera/odm extras) → kept
5. `extract-files.py` (Neotic extract-utils rewrite: `blob_fixups` with `add_needed`/`replace_needed`/`remove_needed`/`sig_replace`) generates the vendor tree

Key blob fixups include: `libtinyxml2 → libtinyxml2-v34` swaps, `libultrahdr → libultrahdr_prebuilt`, tensorflow vendor shims, qcril sig patch, `libdisplaydebug` composer3 `-V2 → -V3` over-link fix, qspa rc sanitization.

---

## Building

```bash
# 1. Init Neotic cnb
repo init -u https://github.com/Neoteric-OS/android_manifest.git -b cnb

# 2. Local manifest — save as .repo/local_manifests/peridot.xml
#    (content below)

# 3. Sync
repo sync -c --force-sync --no-clone-bundle --no-tags -j$(nproc)

# 4. Vendor blobs — sync the private vendor repo to vendor/xiaomi/peridot,
#    or stage the extracted firmware yourself and run ./extract-files.py

# 5. Build
. build/envsetup.sh
lunch peridot-userdebug        # Neotic release defaults to cp2a
m -j$(nproc)
```

Output: `out/target/product/peridot/Neoteric-OS_peridot-5.0-<date>.zip`

### `.repo/local_manifests/peridot.xml`

```xml
<manifest>
  <remote name="ett" fetch="https://github.com/ettasrinith" />
  <remote name="los" fetch="https://github.com/LineageOS" revision="lineage-24.0" />
  <remote name="neohal" fetch="https://github.com/Neoteric-OS" />

  <project path="device/xiaomi/peridot" name="android_device_xiaomi_peridot" remote="ett" revision="cnb" />
  <project path="device/xiaomi/peridot-prebuilt" name="android_device_xiaomi_peridot-prebuilt" remote="ett" revision="main" />
  <project path="kernel/xiaomi/sm8635" name="android_kernel_xiaomi_sm8635" remote="ett" revision="pure-24.0" />
  <project path="kernel/xiaomi/sm8635-modules" name="android_kernel_xiaomi_sm8635-modules" remote="los" />
  <project path="kernel/xiaomi/sm8635-devicetrees" name="android_kernel_xiaomi_sm8635-devicetrees" remote="los" />
  <project path="hardware/xiaomi" name="android_hardware_xiaomi" remote="los" />

  <project path="hardware/qcom-caf/sm8650/display" name="android_hardware_qcom_display" remote="neohal" revision="cnb-8650" />
</manifest>
```

### `neoteric.dependencies`

Repos this device pulls in through Neotic's dependency system:

| Repo | Path | Revision |
|---|---|---|
| `ettasrinith/android_kernel_xiaomi_sm8635` | `kernel/xiaomi/sm8635` | `pure-24.0` |
| `LineageOS/android_kernel_xiaomi_sm8635-modules` | `kernel/xiaomi/sm8635-modules` | `lineage-24.0` |
| `LineageOS/android_kernel_xiaomi_sm8635-devicetrees` | `kernel/xiaomi/sm8635-devicetrees` | `lineage-24.0` |
| `LineageOS/android_hardware_xiaomi` | `hardware/xiaomi` | `lineage-24.0` |
| `ettasrinith/android_device_xiaomi_peridot-prebuilt` | `device/xiaomi/peridot-prebuilt` | `main` |
| `Neoteric-OS/android_hardware_qcom_display` | `hardware/qcom-caf/sm8650/display` | `cnb-8650` |

---

## Companion trees

- **[`android_device_xiaomi_peridot-prebuilt`](https://github.com/ettasrinith/android_device_xiaomi_peridot-prebuilt)** — prebuilt kernel, DTB/DTBO, vendor_dlkm / vendor_ramdisk / system_dlkm modules, kernel-headers (see its README)
- `interfaces/` — `vendor.lineage.touch` AIDL interface + NFC mink/peripheralstate headers, inlined aurora-style
- **[`android_vendor_xiaomi_peridot`](https://github.com/ettasrinith/android_vendor_xiaomi_peridot)** *(private)* — vendor blobs

## Credits

- **Neotic OS team** — manifest, vendor, target, extract-utils
- **LineageOS / crDroid** peridot trees — device tree base this was converted from
- **aurora (Neotic Xiaomi trees)** — conventions this port follows
- **CodeLinaro (CLO/QSSI)** — HAL sources the cnb manifest ships
