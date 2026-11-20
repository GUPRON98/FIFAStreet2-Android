package javax.microedition.rms;

import javax.microedition.lcdui.ResourceLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RecordStore {
    private String name;
    private ArrayList<byte[]> records = new ArrayList<>();
    private File storeDir;
    private boolean open = true;

    private RecordStore(String name, File storeDir) {
        this.name = name;
        this.storeDir = storeDir;
        loadRecords();
    }

    private static File getBaseDir() {
        if (ResourceLoader.appContext != null) {
            return new File(ResourceLoader.appContext.getFilesDir(), "rms");
        }
        File fallback = new File("/sdcard/ai/rms_fifa");
        fallback.mkdirs();
        return fallback;
    }

    public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException {
        File baseDir = getBaseDir();
        File storeDir = new File(baseDir, recordStoreName);
        boolean exists = storeDir.exists() && new File(storeDir, "1.rec").exists();
        if (!exists) {
            if (!createIfNecessary) {
                throw new RecordStoreNotFoundException("Record store not found: " + recordStoreName);
            }
            storeDir.mkdirs();
        }
        return new RecordStore(recordStoreName, storeDir);
    }

    private void loadRecords() {
        records.clear();
        int id = 1;
        while (true) {
            File f = new File(storeDir, id + ".rec");
            if (!f.exists()) break;
            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] data = new byte[(int) f.length()];
                int read = 0;
                while (read < data.length) {
                    int r = fis.read(data, read, data.length - read);
                    if (r < 0) break;
                    read += r;
                }
                fis.close();
                records.add(data);
            } catch (IOException e) {
                break;
            }
            id++;
        }
    }

    public void closeRecordStore() throws RecordStoreException {
        open = false;
    }

    public int getNumRecords() {
        return records.size();
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return 1;
    }

    public long getLastModified() {
        return System.currentTimeMillis();
    }

    public int getSize() {
        int sz = 0;
        for (byte[] b : records) {
            if (b != null) sz += b.length;
        }
        return sz;
    }

    public int getSizeAvailable() {
        return 1024 * 1024;
    }

    public int getNextRecordID() {
        return records.size() + 1;
    }

    public byte[] getRecord(int recordId) throws RecordStoreException {
        int idx = recordId - 1;
        if (idx < 0 || idx >= records.size()) {
            throw new InvalidRecordIDException("Record ID " + recordId + " does not exist (total " + records.size() + ")");
        }
        byte[] original = records.get(idx);
        if (original == null) {
            throw new InvalidRecordIDException("Record is null for ID: " + recordId);
        }
        byte[] copy = new byte[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }

    public int getRecord(int recordId, byte[] buffer, int offset) throws RecordStoreException {
        byte[] rec = getRecord(recordId);
        System.arraycopy(rec, 0, buffer, offset, rec.length);
        return rec.length;
    }

    public int getRecordSize(int recordId) throws RecordStoreException {
        return getRecord(recordId).length;
    }

    public void setRecord(int recordId, byte[] newData, int offset, int numBytes) throws RecordStoreException {
        int idx = recordId - 1;
        if (idx < 0) throw new InvalidRecordIDException("Invalid ID: " + recordId);
        byte[] copy = new byte[numBytes];
        if (newData != null && numBytes > 0) {
            System.arraycopy(newData, offset, copy, 0, numBytes);
        }

        while (records.size() <= idx) {
            records.add(new byte[0]);
        }
        records.set(idx, copy);
        saveRecordFile(recordId, copy);
    }

    public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreException {
        byte[] copy = new byte[numBytes];
        if (data != null && numBytes > 0) {
            System.arraycopy(data, offset, copy, 0, numBytes);
        }
        records.add(copy);
        int newId = records.size();
        saveRecordFile(newId, copy);
        return newId;
    }

    public void deleteRecord(int recordId) throws RecordStoreException {
        int idx = recordId - 1;
        if (idx >= 0 && idx < records.size()) {
            records.set(idx, null);
            File f = new File(storeDir, recordId + ".rec");
            if (f.exists()) f.delete();
        }
    }

    private void saveRecordFile(int recordId, byte[] data) {
        try {
            storeDir.mkdirs();
            File f = new File(storeDir, recordId + ".rec");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.close();
        } catch (IOException ignored) {}
    }

    public static void deleteRecordStore(String recordStoreName) throws RecordStoreException {
        File baseDir = getBaseDir();
        File storeDir = new File(baseDir, recordStoreName);
        if (storeDir.exists()) {
            File[] files = storeDir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            storeDir.delete();
        }
    }

    public static String[] listRecordStores() {
        File baseDir = getBaseDir();
        if (!baseDir.exists()) return new String[0];
        File[] files = baseDir.listFiles();
        if (files == null) return new String[0];
        ArrayList<String> list = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) list.add(f.getName());
        }
        return list.toArray(new String[0]);
    }

    public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) {
        return new RecordEnumeration() {
            private int currentId = 1;

            @Override
            public void destroy() {}

            @Override
            public boolean hasNextElement() {
                return currentId <= records.size();
            }

            @Override
            public boolean hasPreviousElement() {
                return currentId > 1;
            }

            @Override
            public byte[] nextRecord() throws RecordStoreException {
                if (!hasNextElement()) throw new InvalidRecordIDException("No next record");
                return getRecord(currentId++);
            }

            @Override
            public int nextRecordId() throws RecordStoreException {
                if (!hasNextElement()) throw new InvalidRecordIDException("No next record");
                return currentId++;
            }

            @Override
            public int numRecords() {
                return records.size();
            }

            @Override
            public byte[] previousRecord() throws RecordStoreException {
                if (!hasPreviousElement()) throw new InvalidRecordIDException("No previous record");
                return getRecord(--currentId);
            }

            @Override
            public int previousRecordId() throws RecordStoreException {
                if (!hasPreviousElement()) throw new InvalidRecordIDException("No previous record");
                return --currentId;
            }

            @Override
            public void rebuild() {
                reset();
            }

            @Override
            public void reset() {
                currentId = 1;
            }

            @Override
            public void keepUpdated(boolean keepUpdated) {}

            @Override
            public boolean isKeptUpdated() {
                return false;
            }
        };
    }
}
