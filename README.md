# Neoteric OS · Xiaomi POCO F6 (peridot)

Unofficial **Neoteric OS** build target for the Xiaomi POCO F6 (`peridot`, Qualcomm `pineapple` / SM8635 family), targeting the Neoteric **`cnb`** branch (Android 17).

This is a **LineageOS 24.0 → Neoteric cnb** conversion of the peridot device tree, with vendor blobs extracted from Xiaomi HyperOS **OS3.0.303.0.WNPINXM (India)** and conventions cross-checked against Neoteric's Xiaomi trees.

---

## What changed vs the stock LOS tree

| Area | Change |
|---|---|
| Product makefile | `peridot.mk` inherits `vendor/neoteric/target/product/neoteric-target.mk`; fingerprint bumped to `OS3.0.303.0.WNPINXM (India)` |
| Kernel | **Prebuilt** via `kernel/kernel-platform.mk`; no kernel source build. Sources stay pinned for reference via `neoteric.dependencies` |
| Audio | Core primary HAL, AGM, PAL, and implementation libraries are prebuilt from the matching HyperOS firmware. Helper modules are requested at device level; the SM8650 audio path is not fetched and does not select a source-built HAL |
| CAF display | Neoteric `android_hardware_qcom_display` `cnb-8650` at `hardware/qcom-caf/sm8650/display`, with namespace imports and `PRODUCT_SOONG_NAMESPACES` entries |
| Blobs | Extracted from HyperOS OS3.0.303.0 (India) and reconciled against the LOS peridot list; vendor Blueprint dependency closure was checked for ARM64 shared objects |
| Reference | Cross-checked against the CLO-style peridot port for framework-matrix coverage, sepolicy domains, and filesystem/OTA policy |
| VINTF | `configs/vintf/compatibility_matrix.lineage.xml` is installed as a product VINTF fragment by the `peridot_compat_matrix_lineage` `prebuilt_etc` module |
| QTI components | `TARGET_COMMON_QTI_COMPONENTS := av bt perf telephony usb wfd wlan`; core audio is supplied by vendor prebuilts |

### Product VINTF fragment

The fragment adds framework-matrix entries for device-specific HALs such as Lineage Health and QTI qconfig.
It is installed under `product/etc/vintf`; libvintf discovers product VINTF files without requiring a numbered
filename. The matrix `version="8.0"` is the VINTF schema/meta-version, not the framework compatibility
target level. Because the fragment omits `level`, libvintf fills it from the device level.

Run `checkvintf` against the completed images after a full build.

---

## Vendor blobs

Blobs live in the private repository [`ettasrinith/android_vendor_xiaomi_peridot`](https://github.com/ettasrinith/android_vendor_xiaomi_peridot).
The two modem images are tracked with git-lfs.

### Extraction summary

1. Download the `peridot` HyperOS stable **OS3.0.303.0.WNPINXM (India)** fastboot package and verify its checksum.
2. Unpack `super.img` into `system`, `vendor`, `odm`, `product`, `system_ext`, `vendor_dlkm`, and `system_dlkm`.
3. Extract the EROFS partition images.
4. Reconcile `proprietary-files.txt` against the peridot LOS list, removing blobs supplied by common QCOM components while retaining device-specific blobs.
5. Run `extract-files.py` to generate the vendor tree and Blueprint files.

Key fixups include `libtinyxml2` compatibility swaps, `libultrahdr` prebuilt selection, TensorFlow vendor shims,
qcril symbol patching, the `libdisplaydebug` composer dependency correction, and QSPA rc sanitization.

---

## Building

```bash
repo init -u https://github.com/Neoteric-OS/android_manifest.git -b cnb

# Save the manifest below as .repo/local_manifests/peridot.xml
repo sync -c --force-sync --no-clone-bundle --no-tags -j$(nproc)

# Sync the private vendor repository to vendor/xiaomi/peridot,
# or stage the extracted firmware and run ./extract-files.py

. build/envsetup.sh
lunch peridot-userdebug
m -j$(nproc)
```

Outputs are written under `out/target/product/peridot/`.

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

The dependency is upstream LineageOS `lineage-24.0`. The supplied
`ettasrinith/android_hardware_xiaomi` checkout on `lineage-23.2` is a separate older fork; do not substitute
it unless you intentionally pin and validate that branch.

### `neoteric.dependencies`

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

- [`android_device_xiaomi_peridot-prebuilt`](https://github.com/ettasrinith/android_device_xiaomi_peridot-prebuilt) — prebuilt kernel, DTBs/DTBO, module sets, and kernel headers
- `interfaces/` — `vendor.lineage.touch` AIDL interface and NFC headers inlined in the device tree
- [`android_vendor_xiaomi_peridot`](https://github.com/ettasrinith/android_vendor_xiaomi_peridot) *(private)* — vendor blobs

## Credits

- Neoteric OS — manifest, vendor target, and extract-utils
- LineageOS and community peridot trees — device-tree base
- aurora and other Neoteric Xiaomi trees — integration conventions
- CodeLinaro QSSI/CLO sources — HAL sources shipped by the `cnb` manifest
