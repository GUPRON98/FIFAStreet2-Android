# ⚽ FIFA Street 2 - Android Native Remaster

Port Android Native dari game legendaris **FIFA Street 2 (EA Sports BIG)** dengan tampilan **Portrait Retro HUD Gamepad** (terinspirasi dari layout Diamond Rush), **In-Engine Cheat & Currency Engine**, dan **Zero Permissions**.

---

## 🌟 Fitur Utama

- 📱 **Portrait Retro HUD Gamepad**:
  - Layar game di bagian atas dengan rendering tajam 60 FPS dan double-buffering anti-tearing.
  - Panel kontrol tactile di bagian bawah dengan styling street neon orange & gold.
  - **2 Mode Kontrol**:
    - **Mode 0 (🔢 NUM)**: Full 3x4 Street Phone Keypad (1..9, *, 0, #).
    - **Mode 1 (🎮 DPAD)**: 8-Way D-Pad di kiri + Tombol Aksi besar di kanan (SHOOT, TRICK, BREAKER, TACKLE).
  - Sentuhan langsung pada layar game berfungsi sebagai tombol tembak/oper (SHOOT/PASS).
- ⚡ **In-Engine Cheat & Currency Engine (Modal Overlay)**:
  - 💰 **+100.000 Skill Points**: Tambah mata uang jalanan secara instan.
  - 🌟 **MAX Skill Points (999.999)**: Maksimalkan poin upgrade tanpa batas.
  - 🗺️ **Buka Semua 12 Lapangan Dunia**: Main di London, Amsterdam, Rio de Janeiro, New York, Marseille, dll.
  - 🚀 **Instant Gamebreaker**: Penuhi meter trick dan aktifkan Gamebreaker seketika.
  - 🥇 **Max Player Stats**: Maksimalkan seluruh atribut pemain tim ke 99.
  - 🔥 **ALL-IN-ONE STREET GOD MODE**: Aktifkan seluruh cheat sekaligus dalam satu sentuhan!
- 📳 **Haptic Feedback**: Getaran responsif pada setiap tombol.
- 🔒 **Zero Permissions**: 100% aman tanpa popup izin penyimpanan eksternal (`MANAGE_EXTERNAL_STORAGE` dihapus).
- 🎨 **Official HD Icon**: Ikon resmi FIFA Street 2 beresolusi tinggi di semua density layar.

---

## 🎮 Kontrol Permainan

| Tombol | Fungsi Gameplay |
|---|---|
| **2 (UP)** / **D-Pad UP** | Lari ke Atas |
| **8 (DOWN)** / **D-Pad DOWN** | Lari ke Bawah |
| **4 (LEFT)** / **D-Pad LEFT** | Lari ke Kiri |
| **6 (RIGHT)** / **D-Pad RIGHT** | Lari ke Kanan |
| **1, 3, 7, 9** | Pergerakan Diagonal |
| **5 (SHOOT)** / Layar Game | Tembak / Oper / Slide Tackle |
| **# (TRICK)** | Trik Juggling Bola |
| *** (BREAKER)** | Aktivasi GAMEBREAKER |
| **0 (TACKLE)** | Rebut Bola / Tackle |
| **MENU (LSK)** | Buka Menu / Opsi |
| **BACK (RSK)** | Jeda / Kembali |
| **⚡ CHEAT** | Buka In-Engine Cheat Modal |
| **🎮 MODE** | Ganti Mode Keypad (Numpad / D-Pad) |

---

## 🛠️ Cara Build APK di Termux / Linux

1. Clone repositori:
   ```bash
   git clone https://github.com/GUPRON98/FIFAStreet2-Android.git
   cd FIFAStreet2-Android
   ```
2. Jalankan build script:
   ```bash
   chmod +x build_apk.sh
   ./build_apk.sh
   ```
3. APK yang sudah di-align dan di-sign akan tersedia di:
   - `/sdcard/ai/FIFAStreet2.apk`
   - `/sdcard/Download/FIFAStreet2.apk`

---

## 🙏 Credits & Referensi

- **Inspirasi Porting & Runtime**: [SupaRj/gow-betrayal-remastered-Android-Native-port](https://github.com/SupaRj/gow-betrayal-remastered-Android-Native-port)
- **Original Game**: EA Sports / EA BIG (2006)
- **Android Port & Cheat System**: GUPRON98 & Antigravity Assistant
