package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.InputStream;

class BinTreeInWindow {
    byte[] bufferBase; // pointer to buffer with data
    InputStream stream;
    private int posLimit;  // offset (from _buffer) of first byte when new block reading must be done
    private boolean streamEndWasReached; // if (true) then streamPos shows real end of stream
    private int pointerToLastSafePosition;
    private int blockSize;  // Size of Allocated memory block
    private int keepSizeBefore;  // how many BYTEs must be kept in buffer before pos
    private int keepSizeAfter;   // how many BYTEs must be kept buffer after pos
    int bufferOffset;
    int pos;             // offset (from _buffer) of current byte
    int streamPos;   // offset (from _buffer) of first not read byte from Stream

    private void moveBlock() {
        int offset = bufferOffset + pos - keepSizeBefore;
        // we need one additional byte, since movePos moves on 1 byte.
        if (offset > 0) {
            offset--;
        }

        int numBytes = bufferOffset + streamPos - offset;

        // check negative offset ????
        System.arraycopy(bufferBase, offset, bufferBase, 0, numBytes);
        bufferOffset -= offset;
    }

    private void readBlock() throws IOException {
        if (streamEndWasReached) {
            return;
        }
        while (true) {
            int size = (0 - bufferOffset) + blockSize - streamPos;
            if (size == 0) {
                return;
            }
            int numReadBytes = stream.read(bufferBase, bufferOffset + streamPos, size);
            if (numReadBytes == -1) {
                posLimit = streamPos;
                int pointerToPostion = bufferOffset + posLimit;
                if (pointerToPostion > pointerToLastSafePosition) {
                    posLimit = pointerToLastSafePosition - bufferOffset;
                }

                streamEndWasReached = true;
                return;
            }
            streamPos += numReadBytes;
            if (streamPos >= pos + keepSizeAfter) {
                posLimit = streamPos - keepSizeAfter;
            }
        }
    }

    private void free() {
        bufferBase = null;
    }

    void create(int keepSizeBefore, int keepSizeAfter, int keepSizeReserv) {
        this.keepSizeBefore = keepSizeBefore;
        this.keepSizeAfter = keepSizeAfter;
        int blockSize = keepSizeBefore + keepSizeAfter + keepSizeReserv;
        if (bufferBase == null || this.blockSize != blockSize) {
            free();
            this.blockSize = blockSize;
            bufferBase = new byte[this.blockSize];
        }
        pointerToLastSafePosition = this.blockSize - keepSizeAfter;
    }

    void init() throws IOException {
        bufferOffset = 0;
        pos = 0;
        streamPos = 0;
        streamEndWasReached = false;
        readBlock();
    }

    void movePos() throws IOException {
        pos++;
        if (pos > posLimit) {
            int pointerToPosition = bufferOffset + pos;
            if (pointerToPosition > pointerToLastSafePosition) {
                moveBlock();
            }
            readBlock();
        }
    }

    byte getIndexByte(int index) {
        return bufferBase[bufferOffset + pos + index];
    }

    // index + limit have not to exceed keepSizeAfter;
    int getMatchLen(int index, int distance, int limit) {
        if (streamEndWasReached) {
            if ((pos + index) + limit > streamPos) {
                limit = streamPos - (pos + index);
            }
        }
        distance++;
        // Byte *pby = _buffer + (size_t)pos + index;
        int pby = bufferOffset + pos + index;

        int i;
        i = 0;
        while (i < limit && bufferBase[pby + i] == bufferBase[pby + i - distance]) {
            i++;
        }
        return i;
    }

    int getNumAvailableBytes() {
        return streamPos - pos;
    }

    void reduceOffsets(int subValue) {
        bufferOffset += subValue;
        posLimit -= subValue;
        pos -= subValue;
        streamPos -= subValue;
    }
}
