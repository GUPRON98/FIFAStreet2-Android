package com.ea.fifastreet2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.lcdui.ResourceLoader;
import javax.microedition.midlet.MIDlet;

public class MainActivity extends Activity {
    private static final String TAG = "FIFAStreet2";
    private GameView gameView;
    private MIDlet midlet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen & Keep Screen On
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ResourceLoader.appContext = getApplicationContext();

        // Cleanup any 0-byte corrupted RMS files from previous runs
        try {
            java.io.File rmsDir = new java.io.File(getFilesDir(), "rms");
            if (rmsDir.exists()) {
                java.io.File[] stores = rmsDir.listFiles();
                if (stores != null) {
                    for (java.io.File store : stores) {
                        java.io.File rec1 = new java.io.File(store, "1.rec");
                        if (rec1.exists() && rec1.length() == 0) {
                            rec1.delete();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        gameView = new GameView(this);
        setContentView(gameView);

        // Launch Game in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?> midletClass = Class.forName("FIFAStreet2");
                    midlet = (MIDlet) midletClass.getDeclaredConstructor().newInstance();
                    midlet.startApp();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, "FifaInitThread").start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (midlet != null) {
            try { midlet.pauseApp(); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (midlet != null) {
            try { midlet.startApp(); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (midlet != null) {
            try { midlet.destroyApp(true); } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        javax.microedition.lcdui.Canvas c = javax.microedition.lcdui.Canvas.currentInstance;
        if (c != null) {
            int j2meKey = mapPhysicalKey(keyCode);
            if (j2meKey != 0) {
                c.keyPressed(j2meKey);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        javax.microedition.lcdui.Canvas c = javax.microedition.lcdui.Canvas.currentInstance;
        if (c != null) {
            int j2meKey = mapPhysicalKey(keyCode);
            if (j2meKey != 0) {
                c.keyReleased(j2meKey);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private int mapPhysicalKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_NUMPAD_0:
                return 48; // '0' Tackle
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_NUMPAD_1:
                return 49; // '1' UP-LEFT
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_NUMPAD_2:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                return 50; // '2' UP
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_NUMPAD_3:
                return 51; // '3' UP-RIGHT
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_NUMPAD_4:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                return 52; // '4' LEFT
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_NUMPAD_5:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
                return 53; // '5' SHOOT/PASS
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_NUMPAD_6:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                return 54; // '6' RIGHT
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_NUMPAD_7:
                return 55; // '7' DOWN-LEFT
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_NUMPAD_8:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                return 56; // '8' DOWN
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_NUMPAD_9:
                return 57; // '9' DOWN-RIGHT
            case KeyEvent.KEYCODE_STAR:
                return 42; // '*' BREAKER
            case KeyEvent.KEYCODE_POUND:
                return 35; // '#' TRICK
            case KeyEvent.KEYCODE_MENU:
                return 255; // Menu
            case KeyEvent.KEYCODE_BACK:
                return 252; // Back
            default:
                return 0;
        }
    }

    // =========================================================================
    // GameView: SurfaceView with Portrait Canvas & Street Gamepad HUD
    // =========================================================================
    public static class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private SurfaceHolder holder;
        private Thread renderThread;
        private volatile boolean running = false;

        // Render paints
        private Paint bitmapPaint;
        private Paint frameBorderPaint;
        private Paint panelBgPaint;

        // Button paints
        private Paint btnNormalPaint;
        private Paint btnPressedPaint;
        private Paint btnActionPressedPaint;
        private Paint btnBorderPaint;
        private Paint btnBorderGlowPaint;
        private Paint textPaint;
        private Paint subTextPaint;
        private Paint iconPaint;
        private Paint iconAccentPaint;
        private Paint cheatModalPaint;
        private Paint cheatItemPaint;
        private Vibrator vibrator;

        // Control mode: 0 = Full 3x4 Numpad, 1 = Street D-Pad + Quick Buttons
        private int controlMode = 0;

        // Cheat Menu State
        private boolean showCheatModal = false;
        private String cheatNotification = null;
        private long cheatNotificationTime = 0;

        // Bounding boxes
        private RectF rectGame = new RectF();
        private RectF rectPanel = new RectF();
        private RectF rectModeSwitch = new RectF();
        private RectF rectCheatBtn = new RectF();

        // Cheat Modal Buttons
        private RectF rectCheat100k = new RectF();
        private RectF rectCheatMaxPoints = new RectF();
        private RectF rectCheatVenues = new RectF();
        private RectF rectCheatBreaker = new RectF();
        private RectF rectCheatStats = new RectF();
        private RectF rectCheatGodMode = new RectF();
        private RectF rectCheatClose = new RectF();
        private RectF rectCheatModalBox = new RectF();

        // Icons
        private static final int ICON_NONE = 0;
        private static final int ICON_UP = 1;
        private static final int ICON_DOWN = 2;
        private static final int ICON_LEFT = 3;
        private static final int ICON_RIGHT = 4;
        private static final int ICON_UP_LEFT = 5;
        private static final int ICON_UP_RIGHT = 6;
        private static final int ICON_BALL = 7;
        private static final int ICON_TRICK = 8;
        private static final int ICON_BREAKER = 9;
        private static final int ICON_TACKLE = 10;
        private static final int ICON_MENU = 11;
        private static final int ICON_BACK = 12;

        public static class HudButton {
            public int keyCode;
            public String label;
            public String subLabel;
            public int iconType;
            public RectF rect = new RectF();
            public boolean isPressed = false;

            public HudButton(int keyCode, String label, String subLabel, int iconType) {
                this.keyCode = keyCode;
                this.label = label;
                this.subLabel = subLabel;
                this.iconType = iconType;
            }
        }

        private List<HudButton> buttons = new ArrayList<>();

        // Buttons
        private HudButton btn1 = new HudButton(49, "1", "UL", ICON_UP_LEFT);
        private HudButton btn2 = new HudButton(50, "2", "UP", ICON_UP);
        private HudButton btn3 = new HudButton(51, "3", "UR", ICON_UP_RIGHT);
        private HudButton btn4 = new HudButton(52, "4", "LEFT", ICON_LEFT);
        private HudButton btn5 = new HudButton(53, "5", "SHOOT", ICON_BALL);
        private HudButton btn6 = new HudButton(54, "6", "RIGHT", ICON_RIGHT);
        private HudButton btn7 = new HudButton(55, "7", "DL", ICON_NONE);
        private HudButton btn8 = new HudButton(56, "8", "DOWN", ICON_DOWN);
        private HudButton btn9 = new HudButton(57, "9", "DR", ICON_NONE);
        private HudButton btnStar = new HudButton(42, "*", "BREAKER", ICON_BREAKER);
        private HudButton btn0 = new HudButton(48, "0", "TACKLE", ICON_TACKLE);
        private HudButton btnPound = new HudButton(35, "#", "TRICK", ICON_TRICK);

        private HudButton btnSoftLeft = new HudButton(255, "MENU", "LSK", ICON_MENU);
        private HudButton btnSoftRight = new HudButton(252, "BACK", "RSK", ICON_BACK);

        // Virtual Screen Tap Action
        private boolean isScreenTapPressed = false;

        public GameView(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);

            bitmapPaint = new Paint();
            bitmapPaint.setFilterBitmap(false);

            frameBorderPaint = new Paint();
            frameBorderPaint.setColor(0xFFFF5500); // Neon Street Orange Border
            frameBorderPaint.setStyle(Paint.Style.STROKE);
            frameBorderPaint.setStrokeWidth(4f);

            panelBgPaint = new Paint();
            panelBgPaint.setColor(0x66101622); // Semi-transparent street dark slate
            panelBgPaint.setStyle(Paint.Style.FILL);

            btnNormalPaint = new Paint();
            btnNormalPaint.setColor(0x66222E42);
            btnNormalPaint.setStyle(Paint.Style.FILL);

            btnPressedPaint = new Paint();
            btnPressedPaint.setColor(0xAAFF5500); // Glowing Street Orange
            btnPressedPaint.setStyle(Paint.Style.FILL);

            btnActionPressedPaint = new Paint();
            btnActionPressedPaint.setColor(0xAA00E5FF); // Electric Cyan
            btnActionPressedPaint.setStyle(Paint.Style.FILL);

            btnBorderPaint = new Paint();
            btnBorderPaint.setColor(0xCCFF5500);
            btnBorderPaint.setStyle(Paint.Style.STROKE);
            btnBorderPaint.setStrokeWidth(3f);

            btnBorderGlowPaint = new Paint();
            btnBorderGlowPaint.setColor(0xFFFFD700); // Street Gold Glow
            btnBorderGlowPaint.setStyle(Paint.Style.STROKE);
            btnBorderGlowPaint.setStrokeWidth(5f);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setFakeBoldText(true);

            subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            subTextPaint.setColor(0xFFFFB300);
            subTextPaint.setTextAlign(Paint.Align.CENTER);
            subTextPaint.setTextSize(18f);

            iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            iconPaint.setColor(0xFFFFB300);
            iconPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            iconPaint.setStrokeWidth(3f);

            iconAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            iconAccentPaint.setColor(0xFF00E5FF);
            iconAccentPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            cheatModalPaint = new Paint();
            cheatModalPaint.setColor(0xF0121824); // Frosted street modal
            cheatModalPaint.setStyle(Paint.Style.FILL);

            cheatItemPaint = new Paint();
            cheatItemPaint.setColor(0x88243048);
            cheatItemPaint.setStyle(Paint.Style.FILL);

            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            buttons.add(btnSoftLeft);
            buttons.add(btnSoftRight);
            buttons.add(btn1);
            buttons.add(btn2);
            buttons.add(btn3);
            buttons.add(btn4);
            buttons.add(btn5);
            buttons.add(btn6);
            buttons.add(btn7);
            buttons.add(btn8);
            buttons.add(btn9);
            buttons.add(btnStar);
            buttons.add(btn0);
            buttons.add(btnPound);
        }

        @Override
        public void surfaceCreated(SurfaceHolder h) {
            running = true;
            renderThread = new Thread(this, "FIFAStreet2RenderThread");
            renderThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
            layoutControls(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder h) {
            running = false;
            try {
                if (renderThread != null) renderThread.join();
            } catch (Exception ignored) {}
        }

        private void layoutControls(int w, int h) {
            if (w <= 0 || h <= 0) return;

            // Target aspect ratio for game: 128x160 (0.80)
            int origW = 128;
            int origH = 160;

            // Top ~48% of portrait screen for the game canvas
            float maxGameH = h * 0.48f;
            float scale = Math.min((float) (w * 0.96f) / origW, maxGameH / origH);

            float renderW = origW * scale;
            float renderH = origH * scale;
            float gameLeft = (w - renderW) / 2f;
            float gameTop = 14f;

            rectGame.set(gameLeft, gameTop, gameLeft + renderW, gameTop + renderH);

            // Controller panel sits below the game screen
            float panelTop = gameTop + renderH + 8f;
            float panelBottom = h - 8f;
            rectPanel.set(8f, panelTop, w - 8f, panelBottom);

            float panelW = rectPanel.width();
            float panelH = rectPanel.height();

            // Header row in HUD (4 Items):
            // [ ⚽ MENU ]  [ ⚡ CHEAT ]  [ 🎮 MODE ]  [ ⏸ BACK ]
            float headerH = Math.min(panelH * 0.125f, 62f);
            float gap = 6f;
            float colW = (panelW - (gap * 5)) / 4f;
            float headerY = panelTop + 6f;

            btnSoftLeft.rect.set(rectPanel.left + gap, headerY, rectPanel.left + gap + colW, headerY + headerH);
            rectCheatBtn.set(rectPanel.left + gap * 2 + colW, headerY, rectPanel.left + gap * 2 + colW * 2, headerY + headerH);
            rectModeSwitch.set(rectPanel.left + gap * 3 + colW * 2, headerY, rectPanel.left + gap * 3 + colW * 3, headerY + headerH);
            btnSoftRight.rect.set(rectPanel.left + gap * 4 + colW * 3, headerY, rectPanel.left + gap * 4 + colW * 4, headerY + headerH);

            // Keypad area below header
            float gridTop = headerY + headerH + 8f;
            float gridH = panelBottom - gridTop;
            float gridW = panelW;

            if (controlMode == 0) {
                // ==========================================
                // MODE 0: FULL 3x4 STREET NUMPAD
                // ==========================================
                float gapX = 8f;
                float gapY = 8f;
                float cellW = (gridW - (gapX * 4)) / 3f;
                float cellH = (gridH - (gapY * 5)) / 4f;

                HudButton[][] grid = new HudButton[][] {
                    { btn1, btn2, btn3 },
                    { btn4, btn5, btn6 },
                    { btn7, btn8, btn9 },
                    { btnStar, btn0, btnPound }
                };

                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 3; c++) {
                        float left = rectPanel.left + gapX + c * (cellW + gapX);
                        float top = gridTop + gapY + r * (cellH + gapY);
                        grid[r][c].rect.set(left, top, left + cellW, top + cellH);
                    }
                }
            } else {
                // ==========================================
                // MODE 1: STREET D-PAD + QUICK BUTTONS
                // ==========================================
                float dpadW = gridW * 0.52f;
                float btnSize = Math.min(dpadW / 3.1f, gridH / 3.2f);
                float dpadCenterX = rectPanel.left + dpadW * 0.48f;
                float dpadCenterY = gridTop + gridH * 0.52f;

                btn2.rect.set(dpadCenterX - btnSize / 2, dpadCenterY - btnSize * 1.5f,
                              dpadCenterX + btnSize / 2, dpadCenterY - btnSize * 0.5f);
                btn8.rect.set(dpadCenterX - btnSize / 2, dpadCenterY + btnSize * 0.5f,
                              dpadCenterX + btnSize / 2, dpadCenterY + btnSize * 1.5f);
                btn4.rect.set(dpadCenterX - btnSize * 1.5f, dpadCenterY - btnSize / 2,
                              dpadCenterX - btnSize * 0.5f, dpadCenterY + btnSize / 2);
                btn6.rect.set(dpadCenterX + btnSize * 0.5f, dpadCenterY - btnSize / 2,
                              dpadCenterX + btnSize * 1.5f, dpadCenterY + btnSize / 2);

                btn1.rect.set(dpadCenterX - btnSize * 1.5f, dpadCenterY - btnSize * 1.5f,
                              dpadCenterX - btnSize * 0.55f, dpadCenterY - btnSize * 0.55f);
                btn3.rect.set(dpadCenterX + btnSize * 0.55f, dpadCenterY - btnSize * 1.5f,
                              dpadCenterX + btnSize * 1.5f, dpadCenterY - btnSize * 0.55f);
                btn7.rect.set(dpadCenterX - btnSize * 1.5f, dpadCenterY + btnSize * 0.55f,
                              dpadCenterX - btnSize * 0.55f, dpadCenterY + btnSize * 1.5f);
                btn9.rect.set(dpadCenterX + btnSize * 0.55f, dpadCenterY + btnSize * 0.55f,
                              dpadCenterX + btnSize * 1.5f, dpadCenterY + btnSize * 1.5f);

                float rightW = gridW - dpadW;
                float actionCenterX = rectPanel.left + dpadW + rightW * 0.48f;
                float actionCenterY = dpadCenterY + btnSize * 0.15f;
                float actionR = btnSize * 1.15f;
                btn5.rect.set(actionCenterX - actionR, actionCenterY - actionR,
                              actionCenterX + actionR, actionCenterY + actionR);

                float toolW = (rightW - 16f) / 3f;
                float toolH = btnSize * 0.65f;
                float toolY = gridTop + 4f;

                btnStar.rect.set(actionCenterX - toolW * 1.5f - 4f, toolY, actionCenterX - toolW * 0.5f - 4f, toolY + toolH);
                btnPound.rect.set(actionCenterX - toolW * 0.5f, toolY, actionCenterX + toolW * 0.5f, toolY + toolH);
                btn0.rect.set(actionCenterX + toolW * 0.5f + 4f, toolY, actionCenterX + toolW * 1.5f + 4f, toolY + toolH);
            }

            // Layout Cheat Modal
            float modalW = Math.min(w * 0.92f, 520f);
            float modalH = Math.min(h * 0.75f, 620f);
            float modalLeft = (w - modalW) / 2f;
            float modalTop = (h - modalH) / 2f;
            rectCheatModalBox.set(modalLeft, modalTop, modalLeft + modalW, modalTop + modalH);

            float itemH = (modalH - 120f) / 7f;
            float curY = modalTop + 55f;
            float padX = 20f;
            float curW = modalW - padX * 2;

            rectCheat100k.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatMaxPoints.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatVenues.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatBreaker.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatStats.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatGodMode.set(modalLeft + padX, curY, modalLeft + padX + curW, curY + itemH - 8f); curY += itemH;
            rectCheatClose.set(modalLeft + padX * 2, modalTop + modalH - 45f, modalLeft + modalW - padX * 2, modalTop + modalH - 10f);
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas();
                    if (c != null) {
                        renderGame(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        try { holder.unlockCanvasAndPost(c); } catch (Exception ignored) {}
                    }
                }

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException ignored) {}
            }
        }

        private void renderGame(Canvas c) {
            c.drawColor(0xFF0F1218); // Dark street asphalt backdrop

            int screenW = getWidth();
            int screenH = getHeight();
            if (rectGame.isEmpty() && screenW > 0 && screenH > 0) {
                layoutControls(screenW, screenH);
            }

            // Draw game screen backing & Neon Street Orange border
            Paint blackPaint = new Paint();
            blackPaint.setColor(0xFF000000);
            c.drawRect(rectGame, blackPaint);
            c.drawRect(rectGame, frameBorderPaint);

            // Double-buffer thread-safe copy: eliminates tearing & flickering 100%
            synchronized (javax.microedition.lcdui.Canvas.renderLock) {
                Bitmap gameBmp = javax.microedition.lcdui.Canvas.displayBitmap;
                if (gameBmp != null) {
                    Rect srcRect = new Rect(0, 0, gameBmp.getWidth(), gameBmp.getHeight());
                    c.drawBitmap(gameBmp, srcRect, rectGame, bitmapPaint);
                }
            }

            // Draw HUD Panel background
            c.drawRoundRect(rectPanel, 16, 16, panelBgPaint);

            // Draw Cheat Button (⚡ CHEAT)
            drawHeaderButton(c, rectCheatBtn, "⚡ CHEAT", 0xFFFFD700);

            // Draw Mode Switch Button (🔢 NUMPAD / 🎮 D-PAD)
            String modeStr = (controlMode == 0) ? "🔢 NUM" : "🎮 DPAD";
            drawHeaderButton(c, rectModeSwitch, modeStr, 0xFF00E5FF);

            // Draw All Active Touch Buttons
            for (HudButton b : buttons) {
                if (!b.rect.isEmpty()) {
                    drawHudButton(c, b);
                }
            }

            // Draw Cheat Notification Toast if active
            if (cheatNotification != null && System.currentTimeMillis() - cheatNotificationTime < 2500) {
                Paint toastBg = new Paint();
                toastBg.setColor(0xEEFF5500);
                RectF toastRect = new RectF(rectGame.centerX() - 190, rectGame.top + 20, rectGame.centerX() + 190, rectGame.top + 75);
                c.drawRoundRect(toastRect, 14, 14, toastBg);
                Paint toastText = new Paint(Paint.ANTI_ALIAS_FLAG);
                toastText.setColor(0xFFFFFFFF);
                toastText.setTextSize(26);
                toastText.setFakeBoldText(true);
                toastText.setTextAlign(Paint.Align.CENTER);
                c.drawText(cheatNotification, toastRect.centerX(), toastRect.centerY() + 9, toastText);
            }

            // Draw Cheat Modal Overlay if open
            if (showCheatModal) {
                drawCheatModal(c);
            }
        }

        private void drawHeaderButton(Canvas c, RectF r, String label, int accentColor) {
            c.drawRoundRect(r, 12, 12, btnNormalPaint);
            Paint border = new Paint(btnBorderPaint);
            border.setColor(accentColor);
            c.drawRoundRect(r, 12, 12, border);

            textPaint.setTextSize(Math.max(18f, r.height() * 0.38f));
            float ty = r.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f;
            c.drawText(label, r.centerX(), ty, textPaint);
        }

        private void drawCheatModal(Canvas c) {
            // Dark scrim background
            c.drawColor(0xCC000000);

            // Modal Box
            c.drawRoundRect(rectCheatModalBox, 20, 20, cheatModalPaint);
            c.drawRoundRect(rectCheatModalBox, 20, 20, frameBorderPaint);

            // Title
            textPaint.setTextSize(30f);
            textPaint.setColor(0xFFFF5500);
            c.drawText("⚽ CHEAT & CURRENCY ENGINE", rectCheatModalBox.centerX(), rectCheatModalBox.top + 38f, textPaint);
            textPaint.setColor(0xFFFFFFFF);

            // Cheat Options
            drawCheatOption(c, rectCheat100k, "💰 +100.000 SKILL POINTS", false);
            drawCheatOption(c, rectCheatMaxPoints, "🌟 MAX SKILL POINTS (999.999)", false);
            drawCheatOption(c, rectCheatVenues, "🗺️ BUKA SEMUA 12 VENUES / PITCHES", false);
            drawCheatOption(c, rectCheatBreaker, "🚀 INSTANT GAMEBREAKER (METER PENUH)", false);
            drawCheatOption(c, rectCheatStats, "🥇 MAX PLAYER STATS (SEMUA 99)", false);
            drawCheatOption(c, rectCheatGodMode, "🔥 ALL-IN-ONE STREET GOD MODE", true);

            // Close button
            drawHeaderButton(c, rectCheatClose, "✖️ TUTUP MENU", 0xFFFF5252);
        }

        private void drawCheatOption(Canvas c, RectF r, String label, boolean goldGlow) {
            Paint fill = new Paint(cheatItemPaint);
            if (goldGlow) fill.setColor(0xAAFF5500);
            c.drawRoundRect(r, 10, 10, fill);
            c.drawRoundRect(r, 10, 10, goldGlow ? btnBorderGlowPaint : btnBorderPaint);

            textPaint.setTextSize(r.height() * 0.42f);
            float ty = r.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f;
            c.drawText(label, r.centerX(), ty, textPaint);
        }

        private void drawHudButton(Canvas c, HudButton b) {
            RectF r = b.rect;
            if (r.isEmpty()) return;

            boolean isPressed = b.isPressed;
            boolean isAction = (b.keyCode == 53); // Shoot/Pass

            // Paint fill
            Paint fillPaint;
            if (isPressed) {
                fillPaint = isAction ? btnActionPressedPaint : btnPressedPaint;
            } else {
                fillPaint = btnNormalPaint;
            }

            c.drawRoundRect(r, 14, 14, fillPaint);
            c.drawRoundRect(r, 14, 14, isPressed ? btnBorderGlowPaint : btnBorderPaint);

            // Draw decorative street corner accents
            drawCornerAccents(c, r);

            // Draw Icon & Labels
            float cx = r.centerX();
            float cy = r.centerY();
            float iconSize = Math.min(r.width(), r.height()) * 0.45f;

            switch (b.iconType) {
                case ICON_BALL:
                    drawSoccerBall(c, cx, cy, iconSize * 1.15f, isPressed);
                    break;
                case ICON_UP:
                    drawArrowRune(c, cx, cy - 4f, iconSize, 0);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize);
                    break;
                case ICON_DOWN:
                    drawArrowRune(c, cx, cy - 4f, iconSize, 180);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize);
                    break;
                case ICON_LEFT:
                    drawArrowRune(c, cx, cy - 4f, iconSize, 270);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize);
                    break;
                case ICON_RIGHT:
                    drawArrowRune(c, cx, cy - 4f, iconSize, 90);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize);
                    break;
                case ICON_UP_LEFT:
                    drawArrowRune(c, cx, cy - 4f, iconSize * 0.85f, 315);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize * 0.85f);
                    break;
                case ICON_UP_RIGHT:
                    drawArrowRune(c, cx, cy - 4f, iconSize * 0.85f, 45);
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize * 0.85f);
                    break;
                case ICON_MENU:
                case ICON_BACK:
                    textPaint.setTextSize(Math.max(16f, r.height() * 0.38f));
                    float ty = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
                    c.drawText(b.label, cx, ty, textPaint);
                    break;
                default:
                    drawButtonLabels(c, r, b.label, b.subLabel, iconSize * 0.7f);
                    break;
            }
        }

        private void drawSoccerBall(Canvas c, float cx, float cy, float r, boolean pressed) {
            Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballPaint.setColor(pressed ? 0xFF00E5FF : 0xFFFF5500);
            ballPaint.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, r * 0.45f, ballPaint);

            Paint ballBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballBorder.setColor(Color.WHITE);
            ballBorder.setStyle(Paint.Style.STROKE);
            ballBorder.setStrokeWidth(3f);
            c.drawCircle(cx, cy, r * 0.45f, ballBorder);

            textPaint.setTextSize(r * 0.32f);
            c.drawText("SHOOT", cx, cy + 6f, textPaint);
        }

        private void drawCornerAccents(Canvas c, RectF r) {
            float len = 8f;
            Paint p = btnBorderPaint;
            c.drawLine(r.left + 3, r.top + 7, r.left + 3 + len, r.top + 7, p);
            c.drawLine(r.left + 7, r.top + 3, r.left + 7, r.top + 3 + len, p);
            c.drawLine(r.right - 3 - len, r.top + 7, r.right - 3, r.top + 7, p);
            c.drawLine(r.right - 7, r.top + 3, r.right - 7, r.top + 3 + len, p);
            c.drawLine(r.left + 3, r.bottom - 7, r.left + 3 + len, r.bottom - 7, p);
            c.drawLine(r.left + 7, r.bottom - 3 - len, r.left + 7, r.bottom - 3, p);
            c.drawLine(r.right - 3 - len, r.bottom - 7, r.right - 3, r.bottom - 7, p);
            c.drawLine(r.right - 7, r.bottom - 3 - len, r.right - 7, r.bottom - 3, p);
        }

        private void drawArrowRune(Canvas c, float cx, float cy, float size, float angle) {
            c.save();
            c.rotate(angle, cx, cy);
            Path p = new Path();
            float half = size * 0.38f;
            p.moveTo(cx, cy - half);
            p.lineTo(cx + half * 0.8f, cy + half * 0.6f);
            p.lineTo(cx, cy + half * 0.15f);
            p.lineTo(cx - half * 0.8f, cy + half * 0.6f);
            p.close();
            c.drawPath(p, iconPaint);
            c.restore();
        }

        private void drawButtonLabels(Canvas c, RectF r, String num, String sub, float iconSize) {
            textPaint.setTextSize(r.height() * 0.42f);
            float ty = r.centerY() + (iconSize > 0 ? iconSize * 0.35f : 0f);
            c.drawText(num, r.centerX(), ty, textPaint);

            if (sub != null && sub.length() > 0) {
                subTextPaint.setTextSize(Math.max(12f, r.height() * 0.22f));
                c.drawText(sub, r.centerX(), r.bottom - 6f, subTextPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int pointerCount = event.getPointerCount();

            // Handle Cheat Modal touches first if open
            if (showCheatModal) {
                if (action == MotionEvent.ACTION_DOWN) {
                    float tx = event.getX();
                    float ty = event.getY();

                    if (rectCheat100k.contains(tx, ty)) {
                        int cur = FifaCheatManager.setSkillPointsMemory(-1) ? 0 : 2000;
                        FifaCheatManager.setSkillPointsMemory(100000);
                        FifaCheatManager.patchRmsFile(getContext(), 100000, false, false);
                        triggerVibrate();
                        showToast("💰 +100.000 SKILL POINTS!");
                        return true;
                    } else if (rectCheatMaxPoints.contains(tx, ty)) {
                        FifaCheatManager.setSkillPointsMemory(999999);
                        FifaCheatManager.patchRmsFile(getContext(), 999999, false, false);
                        triggerVibrate();
                        showToast("🌟 SKILL POINTS 999.999!");
                        return true;
                    } else if (rectCheatVenues.contains(tx, ty)) {
                        FifaCheatManager.unlockAllVenuesMemory();
                        FifaCheatManager.patchRmsFile(getContext(), -1, true, false);
                        triggerVibrate();
                        showToast("🗺️ 12 LAPANGAN TERBUKA!");
                        return true;
                    } else if (rectCheatBreaker.contains(tx, ty)) {
                        FifaCheatManager.instantGamebreakerMemory();
                        triggerVibrate();
                        showToast("🚀 INSTANT GAMEBREAKER!");
                        return true;
                    } else if (rectCheatStats.contains(tx, ty)) {
                        FifaCheatManager.maxPlayerStatsMemory();
                        FifaCheatManager.patchRmsFile(getContext(), -1, false, true);
                        triggerVibrate();
                        showToast("🥇 MAX STATS 99!");
                        return true;
                    } else if (rectCheatGodMode.contains(tx, ty)) {
                        FifaCheatManager.setSkillPointsMemory(999999);
                        FifaCheatManager.unlockAllVenuesMemory();
                        FifaCheatManager.maxPlayerStatsMemory();
                        FifaCheatManager.instantGamebreakerMemory();
                        FifaCheatManager.patchRmsFile(getContext(), 999999, true, true);
                        triggerVibrate();
                        showToast("🔥 STREET GOD MODE AKTIF!");
                        showCheatModal = false;
                        return true;
                    } else if (rectCheatClose.contains(tx, ty) || !rectCheatModalBox.contains(tx, ty)) {
                        showCheatModal = false;
                        triggerVibrate();
                        return true;
                    }
                }
                return true;
            }

            javax.microedition.lcdui.Canvas c = javax.microedition.lcdui.Canvas.currentInstance;
            if (c == null) return true;

            // Check Mode switch & Cheat button on ACTION_DOWN
            if (action == MotionEvent.ACTION_DOWN) {
                float tx = event.getX();
                float ty = event.getY();
                if (rectCheatBtn.contains(tx, ty)) {
                    showCheatModal = true;
                    triggerVibrate();
                    return true;
                }
                if (rectModeSwitch.contains(tx, ty)) {
                    controlMode = (controlMode == 0) ? 1 : 0;
                    layoutControls(getWidth(), getHeight());
                    triggerVibrate();
                    return true;
                }
            }

            // Reset touch states for all active pointers
            boolean[] nextPressed = new boolean[buttons.size()];
            boolean nextScreenTap = false;

            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
                for (int i = 0; i < pointerCount; i++) {
                    float px = event.getX(i);
                    float py = event.getY(i);

                    // Touch on game screen = Action 5 (Shoot / Pass)
                    if (rectGame.contains(px, py)) {
                        nextScreenTap = true;
                    }

                    // Touch on HUD buttons
                    for (int bIdx = 0; bIdx < buttons.size(); bIdx++) {
                        HudButton btn = buttons.get(bIdx);
                        if (!btn.rect.isEmpty() && btn.rect.contains(px, py)) {
                            nextPressed[bIdx] = true;
                        }
                    }
                }
            }

            // Dispatch Game Screen Tap as Key 5
            handleKeyChange(isScreenTapPressed, nextScreenTap, 53);
            isScreenTapPressed = nextScreenTap;

            // Dispatch Button key changes
            for (int bIdx = 0; bIdx < buttons.size(); bIdx++) {
                HudButton btn = buttons.get(bIdx);
                boolean wasPressed = btn.isPressed;
                boolean isNowPressed = nextPressed[bIdx];

                if (wasPressed != isNowPressed) {
                    btn.isPressed = isNowPressed;
                    handleKeyChange(wasPressed, isNowPressed, btn.keyCode);
                }
            }

            return true;
        }

        private void showToast(String msg) {
            cheatNotification = msg;
            cheatNotificationTime = System.currentTimeMillis();
        }

        private void triggerVibrate() {
            if (vibrator != null) {
                try { vibrator.vibrate(30); } catch (Exception ignored) {}
            }
        }

        private void handleKeyChange(boolean oldState, boolean newState, int keyCode) {
            javax.microedition.lcdui.Canvas c = javax.microedition.lcdui.Canvas.currentInstance;
            if (c == null) return;
            if (!oldState && newState) {
                c.keyPressed(keyCode);
                if (vibrator != null) {
                    try { vibrator.vibrate(25); } catch (Exception ignored) {}
                }
            } else if (oldState && !newState) {
                c.keyReleased(keyCode);
            }
        }
    }
}
