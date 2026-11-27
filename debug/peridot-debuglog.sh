#!/system/bin/sh
#
# Copyright (C) 2026 Paranoid Android
#
# SPDX-License-Identifier: Apache-2.0
#

# Snapshot the boot logs to /metadata every few seconds for ten minutes,
# so a boot that hangs can be debugged from recovery.

DIR=/metadata/peridot-debug

mkdir -p $DIR

i=0
while [ $i -lt 120 ]; do
    dmesg > $DIR/dmesg.txt 2>&1
    logcat -d -b all -v threadtime -t 30000 > $DIR/logcat.txt 2>&1
    getprop > $DIR/props.txt 2>&1
    ps -A -o PID,PPID,STAT,NAME > $DIR/ps.txt 2>&1
    sync
    sleep 5
    i=$((i + 1))
done
