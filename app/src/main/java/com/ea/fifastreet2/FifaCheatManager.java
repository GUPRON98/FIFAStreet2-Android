package com.ea.fifastreet2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FifaCheatManager {

    private static int getSkillPointsMemory() {
        try {
            Class<?> bClass = Class.forName("b");
            Field f = bClass.getDeclaredField("aG");
            f.setAccessible(true);
            return f.getInt(null);
        } catch (Throwable t) {
            return -1;
        }
    }

    public static boolean setSkillPointsMemory(int amount) {
        try {
            Class<?> bClass = Class.forName("b");
            Field f = bClass.getDeclaredField("aG");
            f.setAccessible(true);
            f.setInt(null, amount);

            // Trigger save
            Method m = bClass.getDeclaredMethod("e");
            m.setAccessible(true);
            m.invoke(null);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static boolean unlockAllVenuesMemory() {
        try {
            Class<?> bClass = Class.forName("b");
            Field f = bClass.getDeclaredField("av");
            f.setAccessible(true);
            byte[] av = (byte[]) f.get(null);
            if (av != null) {
                for (int i = 0; i < av.length; i++) {
                    av[i] = (byte) 65; // Unlocked flag
                }
            }

            // Trigger save
            Method m = bClass.getDeclaredMethod("e");
            m.setAccessible(true);
            m.invoke(null);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static boolean maxPlayerStatsMemory() {
        try {
            Class<?> bClass = Class.forName("b");
            Field f = bClass.getDeclaredField("aC");
            f.setAccessible(true);
            byte[][] aC = (byte[][]) f.get(null);
            if (aC != null) {
                for (int i = 0; i < aC.length; i++) {
                    if (aC[i] != null) {
                        for (int j = 0; j < aC[i].length; j++) {
                            aC[i][j] = (byte) 99; // Max 99
                        }
                    }
                }
            }

            // Trigger save
            Method m = bClass.getDeclaredMethod("e");
            m.setAccessible(true);
            m.invoke(null);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static boolean instantGamebreakerMemory() {
        try {
            Class<?> dClass = Class.forName("d");
            Field fay = dClass.getDeclaredField("ay");
            fay.setAccessible(true);
            int[][] ay = (int[][]) fay.get(null);

            Field fg = dClass.getDeclaredField("g");
            fg.setAccessible(true);
            int threshold = fg.getInt(null);

            if (ay != null && ay.length > 0 && ay[0] != null && ay[0].length > 7) {
                ay[0][7] = Math.max(threshold * 2, 999999);
            }

            // Try trigger Gamebreaker directly if in match
            try {
                Method mc = dClass.getDeclaredMethod("c", int.class);
                mc.setAccessible(true);
                mc.invoke(null, 256);
            } catch (Throwable ignored) {}

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    // Direct RMS file patcher as persistent backup
    public static void patchRmsFile(Context context, int skillPoints, boolean unlockVenues, boolean maxStats) {
        try {
            File rmsDir = new File(context.getFilesDir(), "rms/rmsData");
            File recFile = new File(rmsDir, "1.rec");
            if (!recFile.exists()) return;

            byte[] data = new byte[(int) recFile.length()];
            FileInputStream fis = new FileInputStream(recFile);
            int read = 0;
            while (read < data.length) {
                int r = fis.read(data, read, data.length - read);
                if (r < 0) break;
                read += r;
            }
            fis.close();

            if (data.length >= 688) {
                // Offset 12..23: av (12 bytes)
                if (unlockVenues) {
                    for (int i = 0; i < 12; i++) {
                        data[12 + i] = (byte) 65;
                    }
                }

                // Offset 360..639: aC (280 bytes)
                if (maxStats) {
                    for (int i = 0; i < 280; i++) {
                        data[360 + i] = (byte) 99;
                    }
                }

                // Offset 684: aG (4 bytes big-endian)
                if (skillPoints >= 0) {
                    data[684] = (byte) ((skillPoints >> 24) & 0xFF);
                    data[685] = (byte) ((skillPoints >> 16) & 0xFF);
                    data[686] = (byte) ((skillPoints >> 8) & 0xFF);
                    data[687] = (byte) (skillPoints & 0xFF);
                }

                FileOutputStream fos = new FileOutputStream(recFile);
                fos.write(data);
                fos.flush();
                fos.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void showCheatDialog(final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    int memPoints = getSkillPointsMemory();
                    final int currentPoints = (memPoints >= 0) ? memPoints : 2000;

                    AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);

                    // Root Container
                    LinearLayout root = new LinearLayout(context);
                    root.setOrientation(LinearLayout.VERTICAL);
                    root.setPadding(35, 25, 35, 25);
                    root.setBackgroundColor(Color.parseColor("#121418")); // Street Dark Charcoal

                    // Title
                    TextView title = new TextView(context);
                    title.setText("⚽ FIFA STREET 2\n⚡ CHEAT & CURRENCY ENGINE ⚡");
                    title.setTextColor(Color.parseColor("#FF5500")); // Neon Street Orange
                    title.setTextSize(17);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setGravity(Gravity.CENTER);
                    title.setPadding(0, 0, 0, 20);
                    root.addView(title);

                    ScrollView scroll = new ScrollView(context);
                    LinearLayout content = new LinearLayout(context);
                    content.setOrientation(LinearLayout.VERTICAL);

                    // Currency Section
                    TextView currencyLabel = new TextView(context);
                    currencyLabel.setText("💰 SKILL POINTS (CURRENCY):");
                    currencyLabel.setTextColor(Color.parseColor("#00E5FF")); // Electric Cyan
                    currencyLabel.setTextSize(14);
                    currencyLabel.setTypeface(null, Typeface.BOLD);
                    content.addView(currencyLabel);

                    final EditText pointsInput = new EditText(context);
                    pointsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                    pointsInput.setText(String.valueOf(currentPoints > 0 ? currentPoints : 999999));
                    pointsInput.setTextColor(Color.WHITE);
                    pointsInput.setTextSize(18);
                    pointsInput.setPadding(20, 20, 20, 20);

                    GradientDrawable inputBg = new GradientDrawable();
                    inputBg.setColor(Color.parseColor("#262D38"));
                    inputBg.setStroke(2, Color.parseColor("#FF5500"));
                    inputBg.setCornerRadius(10);
                    pointsInput.setBackground(inputBg);
                    content.addView(pointsInput);

                    // Buttons
                    String[] cheatTitles = {
                        "⚡ +100.000 Skill Points",
                        "🌟 MAX Skill Points (999.999)",
                        "🗺️ Buka Semua Venues / Pitches (12 Lapangan Dunia)",
                        "🚀 Instant Gamebreaker (Meter Penuh Seketika)",
                        "🥇 Max Player Stats (Semua Atribut 99)",
                        "🔥 ALL-IN-ONE STREET GOD MODE (Aktifkan Semua!)"
                    };

                    String[] cheatColors = {
                        "#FF9100", "#FF5500", "#00B0FF", "#FFD700", "#E040FB", "#FF1744"
                    };

                    for (int i = 0; i < cheatTitles.length; i++) {
                        final int idx = i;
                        Button btn = new Button(context);
                        btn.setText(cheatTitles[i]);
                        btn.setTextColor(Color.WHITE);
                        btn.setTextSize(13);
                        btn.setTypeface(null, Typeface.BOLD);

                        GradientDrawable bg = new GradientDrawable();
                        bg.setColor(Color.parseColor(cheatColors[i]));
                        bg.setCornerRadius(12);
                        btn.setBackground(bg);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        lp.setMargins(0, 16, 0, 0);
                        btn.setLayoutParams(lp);

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                switch (idx) {
                                    case 0: { // +100k
                                        int cur = getSkillPointsMemory();
                                        int next = (cur > 0 ? cur : 0) + 100000;
                                        setSkillPointsMemory(next);
                                        patchRmsFile(context, next, false, false);
                                        pointsInput.setText(String.valueOf(next));
                                        Toast.makeText(context, "💰 +100.000 Skill Points Berhasil!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 1: { // 999.999
                                        int max = 999999;
                                        setSkillPointsMemory(max);
                                        patchRmsFile(context, max, false, false);
                                        pointsInput.setText(String.valueOf(max));
                                        Toast.makeText(context, "🌟 Skill Points Di-set ke 999.999!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 2: { // Venues
                                        unlockAllVenuesMemory();
                                        patchRmsFile(context, -1, true, false);
                                        Toast.makeText(context, "🗺️ Semua 12 Lapangan Dunia Terbuka!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 3: { // Instant Gamebreaker
                                        instantGamebreakerMemory();
                                        Toast.makeText(context, "🚀 Instant Gamebreaker Aktif!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 4: { // Max Stats
                                        maxPlayerStatsMemory();
                                        patchRmsFile(context, -1, false, true);
                                        Toast.makeText(context, "🥇 Semua Atribut Player Maksimal 99!", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 5: { // All in one
                                        int max = 999999;
                                        setSkillPointsMemory(max);
                                        unlockAllVenuesMemory();
                                        maxPlayerStatsMemory();
                                        instantGamebreakerMemory();
                                        patchRmsFile(context, max, true, true);
                                        pointsInput.setText(String.valueOf(max));
                                        Toast.makeText(context, "🔥 STREET GOD MODE AKTIF: SEMUA CHEAT BERHASIL!", Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                }
                            }
                        });
                        content.addView(btn);
                    }

                    scroll.addView(content);
                    root.addView(scroll);
                    builder.setView(root);

                    builder.setPositiveButton("💾 SIMPAN SKILL POINTS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                int val = Integer.parseInt(pointsInput.getText().toString());
                                setSkillPointsMemory(val);
                                patchRmsFile(context, val, false, false);
                                Toast.makeText(context, "✅ Skill Points Berhasil Disimpan: " + val, Toast.LENGTH_LONG).show();
                            } catch (Throwable t) {
                                Toast.makeText(context, "❌ Format angka tidak valid!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    builder.setNegativeButton("TUTUP", null);

                    AlertDialog dlg = builder.create();
                    dlg.show();

                } catch (Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
