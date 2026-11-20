package javax.microedition.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public interface HttpConnection extends InputConnection, OutputConnection {
    String GET = "GET";
    String POST = "POST";
    String HEAD = "HEAD";
    int HTTP_OK = 200;

    long getLength();
    String getType();
    long getDate() throws IOException;
    int getResponseCode() throws IOException;
    String getResponseMessage() throws IOException;
    String getHeaderField(String name) throws IOException;
    void setRequestMethod(String method) throws IOException;
    void setRequestProperty(String key, String value) throws IOException;
}
