package com.wyhwl.bangnote.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class noteFileInputStream extends FileInputStream {
    private byte        m_byKey = 'b';

    public noteFileInputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public int read(byte b[], int off, int len) throws IOException {
        int nRead = super.read(b, off, len);
        for (int i = 0; i < nRead; i++) {
            b[i] = (byte)(b[i] ^ m_byKey);
        }
        return nRead;
    }
}
