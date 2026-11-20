package javax.microedition.media;

import javax.microedition.media.control.Control;

public interface Player {
    int UNREALIZED = 100;
    int REALIZED = 200;
    int PREFETCHED = 300;
    int STARTED = 400;
    int CLOSED = 0;
    long TIME_UNKNOWN = -1;

    void realize();
    void prefetch();
    void start();
    void stop();
    void deallocate();
    void close();
    int getState();
    void setLoopCount(int count);
    long setMediaTime(long now);
    long getMediaTime();
    void addPlayerListener(PlayerListener playerListener);
    void removePlayerListener(PlayerListener playerListener);
    Control getControl(String controlType);
    Control[] getControls();
}
