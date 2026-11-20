#!/data/data/com.termux/files/usr/bin/bash
set -e

PROJECT_DIR="/data/data/com.termux/files/home/FIFAStreet2-Android"
cd "$PROJECT_DIR"

echo "🔥 [1/6] Menyiapkan direktori build..."
rm -rf build
mkdir -p build/gen build/obj build/apk

echo "🔨 [2/6] Generate R.java via aapt..."
aapt package -f -m \
  -J build/gen \
  -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -I android.jar \
  --min-sdk-version 21 \
  --target-sdk-version 33

echo "☕ [3/6] Mengompilasi seluruh Java source (J2ME Engine + Controls + Cheat)..."
javac -cp "android.jar:build/gen" \
  -d build/obj \
  $(find build/gen -name "*.java") \
  $(find app/src/main/java -name "*.java")

echo "⚡ [4/6] Menghasilkan classes.dex via d8..."
d8 --lib android.jar \
   --output build/apk \
   $(find build/obj -name "*.class") \
   $(find game_classes -name "*.class")

echo "📦 [5/6] Mengemas APK dengan Assets di root dan assets/ folder..."
aapt package -f \
  -M app/src/main/AndroidManifest.xml \
  -S app/src/main/res \
  -A app/src/main/assets \
  -I android.jar \
  --min-sdk-version 21 \
  --target-sdk-version 33 \
  -F build/base.apk

python3 - << 'EOF'
import zipfile, os

base_apk = "build/base.apk"
final_unaligned = "build/unaligned.apk"
classes_dex = "build/apk/classes.dex"
assets_dir = "app/src/main/assets"

uncompressed_exts = {'.so', '.arsc', '.wav', '.png', '.jpg'}

with zipfile.ZipFile(base_apk, 'r') as zin, zipfile.ZipFile(final_unaligned, 'w') as zout:
    # Copy all files from base.apk
    for item in zin.infolist():
        data = zin.read(item.filename)
        ext = os.path.splitext(item.filename)[1].lower()
        cm = zipfile.ZIP_STORED if ext in uncompressed_exts else zipfile.ZIP_DEFLATED
        zout.writestr(item, data)

    # Add classes.dex
    with open(classes_dex, 'rb') as f:
        zout.writestr("classes.dex", f.read(), compress_type=zipfile.ZIP_DEFLATED)

    # Add all assets to root of APK as well (for Class.getResourceAsStream)
    for root, dirs, files in os.walk(assets_dir):
        for file in files:
            path = os.path.join(root, file)
            rel_name = os.path.relpath(path, assets_dir)
            if rel_name not in zin.namelist():
                ext = os.path.splitext(file)[1].lower()
                cm = zipfile.ZIP_STORED if ext in uncompressed_exts else zipfile.ZIP_DEFLATED
                with open(path, 'rb') as f:
                    zout.writestr(rel_name, f.read(), compress_type=cm)
EOF

echo "📐 [6/6] Zipalign & Signing APK..."
zipalign -v -p 4 build/unaligned.apk build/aligned.apk > /dev/null

KEYSTORE="$HOME/FlappySawit/flappysawit.keystore"
mkdir -p /sdcard/ai /sdcard/Download

apksigner sign \
  --ks "$KEYSTORE" \
  --ks-key-alias flappysawit \
  --ks-pass pass:sawit123 \
  --key-pass pass:sawit123 \
  --out /sdcard/ai/FIFAStreet2.apk \
  build/aligned.apk

cp -f /sdcard/ai/FIFAStreet2.apk /sdcard/Download/FIFAStreet2.apk
cp -f /sdcard/ai/FIFAStreet2.apk "$PROJECT_DIR/FIFAStreet2.apk"

echo "🎉 BERHASIL! APK FIFA Street 2 berhasil dibuat dan ditandatangani:"
ls -lh /sdcard/ai/FIFAStreet2.apk
ls -lh /sdcard/Download/FIFAStreet2.apk
