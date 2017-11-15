package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.OutputStream;

class OutWindow {
    private byte[] buffer;
    private int pos;
    private int windowSize = 0;
    private int streamPos;
    private OutputStream outputStream;

    OutWindow(int windowSize) {
        if (buffer == null || this.windowSize != windowSize) {
            buffer = new byte[windowSize];
        }
        this.windowSize = windowSize;
        pos = 0;
        streamPos = 0;
    }

    void setStream(OutputStream stream) throws IOException {
        releaseStream();
        outputStream = stream;
    }

    void releaseStream() throws IOException {
        flush();
        outputStream = null;
    }

    void flush() throws IOException {
        int size = pos - streamPos;
        if (size == 0) {
            return;
        }
        outputStream.write(buffer, streamPos, size);
        if (pos >= windowSize) {
            pos = 0;
        }
        streamPos = pos;
    }

    void copyBlock(int distance, int len) throws IOException {
        int pos = this.pos - distance - 1;
        if (pos < 0) {
            pos += windowSize;
        }
        for (; len != 0; len--) {
            if (pos >= windowSize) {
                pos = 0;
            }
            buffer[this.pos++] = buffer[pos++];
            if (this.pos >= windowSize) {
                flush();
            }
        }
    }

    void putByte(byte b) throws IOException {
        buffer[pos++] = b;
        if (pos >= windowSize) {
            flush();
        }
    }

    byte getByte(int distance) {
        int pos = this.pos - distance - 1;
        if (pos < 0) {
            pos += windowSize;
        }
        return buffer[pos];
    }
}
