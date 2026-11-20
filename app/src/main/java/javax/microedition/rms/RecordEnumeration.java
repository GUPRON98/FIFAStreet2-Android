package javax.microedition.rms;

public interface RecordEnumeration {
    void destroy();
    boolean hasNextElement();
    boolean hasPreviousElement();
    byte[] nextRecord() throws RecordStoreException;
    int nextRecordId() throws RecordStoreException;
    int numRecords();
    byte[] previousRecord() throws RecordStoreException;
    int previousRecordId() throws RecordStoreException;
    void rebuild();
    void reset();
    void keepUpdated(boolean keepUpdated);
    boolean isKeptUpdated();
}
