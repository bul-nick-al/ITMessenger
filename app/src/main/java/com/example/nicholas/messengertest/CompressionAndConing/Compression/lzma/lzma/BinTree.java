package com.example.nicholas.messengertest.CompressionAndConing.Compression.lzma.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class BinTree extends BinTreeInWindow {
    private int cyclicBufferPos;
    private int cyclicBufferSize = 0;
    private int matchMaxLen;
    private int[] son;
    private int[] hash;
    private int cutValue = 0xFF;
    private int hashMask;
    private int hashSizeSum = 0;
    private final int NUM_HASH_DIRECT_BYTES;
    private final int MIN_MATCH_CHECK;
    private final int FIX_HASH_SIZE;

    private final boolean HASH_ARRAY;
    private static final int HASH_2_SIZE = 1 << 10;
    private static final int HASH_3_SIZE = 1 << 16;
    private static final int BT_2_HASH_SIZE = 1 << 16;
    private static final int START_MAX_LEN = 1;
    private static final int HASH_3_OFFSET = HASH_2_SIZE;
    private static final int EMPTY_HASH_VALUE = 0;
    private static final int MAX_VAL_FOR_NORMALIZE = (1 << 30) - 1;

    public void init() throws IOException {
        super.init();
        Arrays.fill(hash, 0, hashSizeSum, EMPTY_HASH_VALUE);
        cyclicBufferPos = 0;
        reduceOffsets(-1);
    }

    public void movePos() throws IOException {
        if (++cyclicBufferPos >= cyclicBufferSize) {
            cyclicBufferPos = 0;
        }
        super.movePos();
        if (pos == MAX_VAL_FOR_NORMALIZE) {
            normalize();
        }
    }

    BinTree(InputStream inputStream, int numHashBytes, int historySize, int keepAddBufferBefore, int matchMaxLen, int keepAddBufferAfter) {
        this.stream = inputStream;
        HASH_ARRAY = (numHashBytes > 2);
        if (HASH_ARRAY) {
            NUM_HASH_DIRECT_BYTES = 0;
            MIN_MATCH_CHECK = 4;
            FIX_HASH_SIZE = HASH_2_SIZE + HASH_3_SIZE;
        } else {
            NUM_HASH_DIRECT_BYTES = 2;
            MIN_MATCH_CHECK = 2 + 1;
            FIX_HASH_SIZE = 0;
        }
        if (historySize > MAX_VAL_FOR_NORMALIZE - 256) {
            throw new RuntimeException("LZMA internal error");
        }
        cutValue = 16 + (matchMaxLen >> 1);

        int windowReserveSize = (historySize + keepAddBufferBefore +
                matchMaxLen + keepAddBufferAfter) / 2 + 256;

        super.create(historySize + keepAddBufferBefore, matchMaxLen + keepAddBufferAfter, windowReserveSize);

        this.matchMaxLen = matchMaxLen;

        int cyclicBufferSize = historySize + 1;
        if (this.cyclicBufferSize != cyclicBufferSize) {
            son = new int[(this.cyclicBufferSize = cyclicBufferSize) * 2];
        }

        int hs = BT_2_HASH_SIZE;

        if (HASH_ARRAY) {
            hs = historySize - 1;
            hs |= (hs >> 1);
            hs |= (hs >> 2);
            hs |= (hs >> 4);
            hs |= (hs >> 8);
            hs >>= 1;
            hs |= 0xFFFF;
            if (hs > (1 << 24)) {
                hs >>= 1;
            }
            hashMask = hs;
            hs++;
            hs += FIX_HASH_SIZE;
        }
        if (hs != hashSizeSum) {
            hash = new int[hashSizeSum = hs];
        }
    }

    int getMatches(int[] distances) throws IOException {
        int lenLimit;
        if (pos + matchMaxLen <= streamPos) {
            lenLimit = matchMaxLen;
        } else {
            lenLimit = streamPos - pos;
            if (lenLimit < MIN_MATCH_CHECK) {
                movePos();
                return 0;
            }
        }

        int offset = 0;
        int matchMinPos = (pos > cyclicBufferSize) ? (pos - cyclicBufferSize) : 0;
        int cur = bufferOffset + pos;
        int maxLen = START_MAX_LEN; // to avoid items for len < hashSize;
        int hashValue, hash2Value = 0, hash3Value = 0;

        if (HASH_ARRAY) {
            int temp = CrcTable[bufferBase[cur] & 0xFF] ^ (bufferBase[cur + 1] & 0xFF);
            hash2Value = temp & (HASH_2_SIZE - 1);
            temp ^= ((bufferBase[cur + 2] & 0xFF) << 8);
            hash3Value = temp & (HASH_3_SIZE - 1);
            hashValue = (temp ^ (CrcTable[bufferBase[cur + 3] & 0xFF] << 5)) & hashMask;
        } else {
            hashValue = ((bufferBase[cur] & 0xFF) ^ ((bufferBase[cur + 1] & 0xFF) << 8));
        }

        int curMatch = hash[FIX_HASH_SIZE + hashValue];
        if (HASH_ARRAY) {
            int curMatch2 = hash[hash2Value];
            int curMatch3 = hash[HASH_3_OFFSET + hash3Value];
            hash[hash2Value] = pos;
            hash[HASH_3_OFFSET + hash3Value] = pos;
            if (curMatch2 > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch2] == bufferBase[cur]) {
                    distances[offset++] = maxLen = 2;
                    distances[offset++] = pos - curMatch2 - 1;
                }
            }
            if (curMatch3 > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch3] == bufferBase[cur]) {
                    if (curMatch3 == curMatch2) {
                        offset -= 2;
                    }
                    distances[offset++] = maxLen = 3;
                    distances[offset++] = pos - curMatch3 - 1;
                    curMatch2 = curMatch3;
                }
            }
            if (offset != 0 && curMatch2 == curMatch) {
                offset -= 2;
                maxLen = START_MAX_LEN;
            }
        }

        hash[FIX_HASH_SIZE + hashValue] = pos;

        int ptr0 = (cyclicBufferPos << 1) + 1;
        int ptr1 = (cyclicBufferPos << 1);

        int len0, len1;
        len0 = len1 = NUM_HASH_DIRECT_BYTES;

        if (NUM_HASH_DIRECT_BYTES != 0) {
            if (curMatch > matchMinPos) {
                if (bufferBase[bufferOffset + curMatch + NUM_HASH_DIRECT_BYTES] !=
                        bufferBase[cur + NUM_HASH_DIRECT_BYTES]) {
                    distances[offset++] = maxLen = NUM_HASH_DIRECT_BYTES;
                    distances[offset++] = pos - curMatch - 1;
                }
            }
        }

        int count = cutValue;

        while (true) {
            if (curMatch <= matchMinPos || count-- == 0) {
                son[ptr0] = son[ptr1] = EMPTY_HASH_VALUE;
                break;
            }
            int delta = pos - curMatch;
            int cyclicPos = ((delta <= cyclicBufferPos) ?
                    (cyclicBufferPos - delta) :
                    (cyclicBufferPos - delta + cyclicBufferSize)) << 1;

            int pby1 = bufferOffset + curMatch;
            int len = Math.min(len0, len1);
            if (bufferBase[pby1 + len] == bufferBase[cur + len]) {
                while (++len != lenLimit) {
                    if (bufferBase[pby1 + len] != bufferBase[cur + len]) {
                        break;
                    }
                }
                if (maxLen < len) {
                    distances[offset++] = maxLen = len;
                    distances[offset++] = delta - 1;
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos];
                        son[ptr0] = son[cyclicPos + 1];
                        break;
                    }
                }
            }
            if ((bufferBase[pby1 + len] & 0xFF) < (bufferBase[cur + len] & 0xFF)) {
                son[ptr1] = curMatch;
                ptr1 = cyclicPos + 1;
                curMatch = son[ptr1];
                len1 = len;
            } else {
                son[ptr0] = curMatch;
                ptr0 = cyclicPos;
                curMatch = son[ptr0];
                len0 = len;
            }
        }
        movePos();
        return offset;
    }

    void skip(int num) throws IOException {
        do {
            int lenLimit;
            if (pos + matchMaxLen <= streamPos) {
                lenLimit = matchMaxLen;
            } else {
                lenLimit = streamPos - pos;
                if (lenLimit < MIN_MATCH_CHECK) {
                    movePos();
                    continue;
                }
            }

            int matchMinPos = (pos > cyclicBufferSize) ? (pos - cyclicBufferSize) : 0;
            int cur = bufferOffset + pos;

            int hashValue;

            if (HASH_ARRAY) {
                int temp = CrcTable[bufferBase[cur] & 0xFF] ^ (bufferBase[cur + 1] & 0xFF);
                int hash2Value = temp & (HASH_2_SIZE - 1);
                hash[hash2Value] = pos;
                temp ^= ((bufferBase[cur + 2] & 0xFF) << 8);
                int hash3Value = temp & (HASH_3_SIZE - 1);
                hash[HASH_3_OFFSET + hash3Value] = pos;
                hashValue = (temp ^ (CrcTable[bufferBase[cur + 3] & 0xFF] << 5)) & hashMask;
            } else {
                hashValue = ((bufferBase[cur] & 0xFF) ^ ((bufferBase[cur + 1] & 0xFF) << 8));
            }

            int curMatch = hash[FIX_HASH_SIZE + hashValue];
            hash[FIX_HASH_SIZE + hashValue] = pos;

            int ptr0 = (cyclicBufferPos << 1) + 1;
            int ptr1 = (cyclicBufferPos << 1);

            int len0, len1;
            len0 = len1 = NUM_HASH_DIRECT_BYTES;

            int count = cutValue;
            while (true) {
                if (curMatch <= matchMinPos || count-- == 0) {
                    son[ptr0] = son[ptr1] = EMPTY_HASH_VALUE;
                    break;
                }

                int delta = pos - curMatch;
                int cyclicPos = ((delta <= cyclicBufferPos) ?
                        (cyclicBufferPos - delta) :
                        (cyclicBufferPos - delta + cyclicBufferSize)) << 1;

                int pby1 = bufferOffset + curMatch;
                int len = Math.min(len0, len1);
                if (bufferBase[pby1 + len] == bufferBase[cur + len]) {
                    while (++len != lenLimit) {
                        if (bufferBase[pby1 + len] != bufferBase[cur + len]) {
                            break;
                        }
                    }
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos];
                        son[ptr0] = son[cyclicPos + 1];
                        break;
                    }
                }
                if ((bufferBase[pby1 + len] & 0xFF) < (bufferBase[cur + len] & 0xFF)) {
                    son[ptr1] = curMatch;
                    ptr1 = cyclicPos + 1;
                    curMatch = son[ptr1];
                    len1 = len;
                } else {
                    son[ptr0] = curMatch;
                    ptr0 = cyclicPos;
                    curMatch = son[ptr0];
                    len0 = len;
                }
            }
            movePos();
        }
        while (--num != 0);
    }

    private void normalizeLinks(int[] items, int numItems, int subValue) {
        for (int i = 0; i < numItems; i++) {
            int value = items[i];
            if (value <= subValue) {
                value = EMPTY_HASH_VALUE;
            } else {
                value -= subValue;
            }
            items[i] = value;
        }
    }

    private void normalize() {
        int subValue = pos - cyclicBufferSize;
        normalizeLinks(son, cyclicBufferSize * 2, subValue);
        normalizeLinks(hash, hashSizeSum, subValue);
        reduceOffsets(subValue);
    }

    private static final int[] CrcTable = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int r = i;
            for (int j = 0; j < 8; j++) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ 0xEDB88320;
                } else {
                    r >>>= 1;
                }
            }
            CrcTable[i] = r;
        }
    }
}
