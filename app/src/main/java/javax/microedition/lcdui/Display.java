package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;

public class Display {
    private static Display instance = new Display();
    private Displayable current;

    public static Display getDisplay(MIDlet m) {
        return instance;
    }

    public void setCurrent(Displayable d) {
        if (this.current != null && this.current != d) {
            this.current.hideNotify();
        }
        this.current = d;
        if (d != null) {
            d.showNotify();
        }
    }

    public Displayable getCurrent() {
        return current;
    }

    public boolean vibrate(int duration) {
        return true;
    }

    public boolean flashBacklight(int duration) {
        return true;
    }
}
