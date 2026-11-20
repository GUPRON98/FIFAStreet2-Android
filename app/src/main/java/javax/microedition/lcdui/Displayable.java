package javax.microedition.lcdui;

public abstract class Displayable {
    private String title = "";
    private CommandListener commandListener;

    public int getWidth() {
        return 480;
    }

    public int getHeight() {
        return 800;
    }

    public boolean isShown() {
        return true;
    }

    public void showNotify() {}
    public void hideNotify() {}

    public void addCommand(Command cmd) {}
    public void removeCommand(Command cmd) {}
    public void setCommandListener(CommandListener l) {
        this.commandListener = l;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        this.title = s != null ? s : "";
    }
}
