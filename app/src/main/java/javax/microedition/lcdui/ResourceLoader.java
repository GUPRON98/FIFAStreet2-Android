package javax.microedition.lcdui;

import android.content.Context;
import java.io.InputStream;
import java.io.IOException;

public class ResourceLoader {
    public static Context appContext;

    public static InputStream open(String path) {
        if (path == null) return null;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        InputStream is = null;
        if (appContext != null) {
            try {
                is = appContext.getAssets().open(path);
            } catch (IOException ignored) {}
        }
        if (is == null) {
            is = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
        }
        if (is == null) {
            is = ResourceLoader.class.getResourceAsStream("/" + path);
        }
        return is;
    }
}
