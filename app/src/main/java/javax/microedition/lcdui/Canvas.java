package javax.microedition.lcdui;

import android.graphics.Bitmap;

public abstract class Canvas extends Displayable {
    public static final int UP = 1;
    public static final int DOWN = 6;
    public static final int LEFT = 2;
    public static final int RIGHT = 5;
    public static final int FIRE = 8;
    public static final int GAME_A = 9;
    public static final int GAME_B = 10;
    public static final int GAME_C = 11;
    public static final int GAME_D = 12;

    public static final int KEY_NUM0 = 48;
    public static final int KEY_NUM1 = 49;
    public static final int KEY_NUM2 = 50;
    public static final int KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52;
    public static final int KEY_NUM5 = 53;
    public static final int KEY_NUM6 = 54;
    public static final int KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56;
    public static final int KEY_NUM9 = 57;
    public static final int KEY_STAR = 42;
    public static final int KEY_POUND = 35;

    public static volatile Canvas currentInstance;

    protected Image backBufferImage;
    protected Graphics bufferGraphics;
    protected Bitmap frontBitmap;
    protected android.graphics.Canvas frontCanvas;

    public static volatile Bitmap displayBitmap;
    public static volatile boolean frameReady = false;
    public static final Object renderLock = new Object();

    public Canvas() {
        currentInstance = this;
        try {
            backBufferImage = Image.createImage(128, 160);
            bufferGraphics = backBufferImage.getGraphics();

            frontBitmap = Bitmap.createBitmap(128, 160, Bitmap.Config.ARGB_8888);
            frontCanvas = new android.graphics.Canvas(frontBitmap);

            displayBitmap = frontBitmap;
        } catch (Throwable ignored) {}
    }

    public int getWidth() {
        return 128;
    }

    public int getHeight() {
        return 160;
    }

    public boolean hasPointerEvents() {
        return true;
    }

    public boolean hasPointerMotionEvents() {
        return true;
    }

    public void setFullScreenMode(boolean mode) {}

    public void repaint() {
        serviceRepaints();
    }

    public void repaint(int x, int y, int width, int height) {
        serviceRepaints();
    }

    public void serviceRepaints() {
        if (bufferGraphics != null) {
            paint(bufferGraphics);
            synchronized (renderLock) {
                if (backBufferImage != null && frontCanvas != null) {
                    Bitmap bmp = backBufferImage.getBitmap();
                    if (bmp != null) {
                        frontCanvas.drawBitmap(bmp, 0, 0, null);
                        displayBitmap = frontBitmap;
                        frameReady = true;
                    }
                }
            }
        }
    }

    public abstract void paint(Graphics g);

    public void keyPressed(int keyCode) {}
    public void keyReleased(int keyCode) {}
    public void keyRepeated(int keyCode) {}

    public void pointerPressed(int x, int y) {}
    public void pointerReleased(int x, int y) {}
    public void pointerDragged(int x, int y) {}

    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case KEY_NUM2: return UP;
            case KEY_NUM8: return DOWN;
            case KEY_NUM4: return LEFT;
            case KEY_NUM6: return RIGHT;
            case KEY_NUM5: return FIRE;
            case KEY_NUM7: return GAME_A;
            case KEY_NUM9: return GAME_B;
            case KEY_STAR: return GAME_C;
            case KEY_POUND: return GAME_D;
            default: return 0;
        }
    }

    public int getKeyCode(int gameAction) {
        switch (gameAction) {
            case UP: return KEY_NUM2;
            case DOWN: return KEY_NUM8;
            case LEFT: return KEY_NUM4;
            case RIGHT: return KEY_NUM6;
            case FIRE: return KEY_NUM5;
            case GAME_A: return KEY_NUM7;
            case GAME_B: return KEY_NUM9;
            case GAME_C: return KEY_STAR;
            case GAME_D: return KEY_POUND;
            default: return 0;
        }
    }

    public String getKeyName(int keyCode) {
        return "KEY_" + keyCode;
    }

    public void showNotify() {}
    public void hideNotify() {}
}
