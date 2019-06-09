package com.wyhwl.bangnote.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class noteFileOutputStream extends FileOutputStream {
    private byte        m_byKey = 'b';

    public noteFileOutputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public noteFileOutputStream(String name, boolean append) throws FileNotFoundException {
        super(name, append);
    }

    public noteFileOutputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public noteFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file, append);
    }

    public void write(byte b[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++){
            b[i] = (byte)(b[i] ^ m_byKey);
        }
        super.write(b, off, len);
    }
}
