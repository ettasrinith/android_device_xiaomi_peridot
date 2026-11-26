#!/system/bin/sh
# Boot debug logger: stream kernel and Android logs to /metadata so a boot
# that hangs at the splash can be diagnosed after a reboot/recovery.
BASE=/metadata/peridot-debug
mkdir -p $BASE
rm -rf $BASE/boot4
for n in 3 2 1 0; do
  [ -d "$BASE/boot$n" ] && mv "$BASE/boot$n" "$BASE/boot$((n + 1))"
done
DIR=$BASE/boot0
mkdir -p $DIR

echo 1 > /proc/sys/kernel/sysrq 2>/dev/null
echo on > /proc/sys/kernel/printk_devkmsg 2>/dev/null

# Live kernel log, survives even if the ramoops region is reset.
cat /dev/kmsg > $DIR/kmsg.txt 2>&1 &

i=0
while [ $i -lt 120 ]; do
  echo "peridot-debug: tick $i" > /dev/kmsg
  echo w > /proc/sysrq-trigger 2>/dev/null
  logcat -d -b all -v threadtime -t 20000 > $DIR/logcat.$i.txt 2>&1 &
  dmesg > $DIR/dmesg.$i.txt 2>&1 &
  getprop > $DIR/props.$i.txt 2>&1 &
  i=$((i + 1))
  sleep 10
done
