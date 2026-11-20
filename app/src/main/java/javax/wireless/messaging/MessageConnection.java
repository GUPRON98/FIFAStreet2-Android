package javax.wireless.messaging;

import javax.microedition.io.Connection;
import java.io.IOException;

public interface MessageConnection extends Connection {
    Message newMessage(String type);
    Message newMessage(String type, String address);
    void send(Message msg) throws IOException;
    void close() throws IOException;
}
