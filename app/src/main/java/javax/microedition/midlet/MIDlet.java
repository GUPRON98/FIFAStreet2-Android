package javax.microedition.midlet;

import java.util.HashMap;

public abstract class MIDlet {
    public static MIDlet instance;
    public static HashMap<String, String> appProperties = new HashMap<>();

    static {
        appProperties.put("MIDlet-Version", "1.0.0");
        appProperties.put("MIDlet-Name", "God of War");
        appProperties.put("MIDlet-Vendor", "Glu Mobile LTD");
        appProperties.put("cheats.enable", "true");
    }

    public MIDlet() {
        instance = this;
    }

    public abstract void startApp() throws MIDletStateChangeException;
    public abstract void pauseApp();
    public abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    public void notifyDestroyed() {}
    public void notifyPaused() {}

    public boolean platformRequest(String url) {
        return false;
    }

    public String getAppProperty(String key) {
        if ("cheats.enable".equalsIgnoreCase(key)) return "true";
        String val = appProperties.get(key);
        return val != null ? val : "";
    }
}
